package com.seal.contracts.generator.launch;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.threading.AbstractDataProducer;
import com.seal.contracts.threading.DataConsumer;
import com.seal.contracts.threading.DataProducer;
import com.seal.contracts.threading.Threadable;
import com.seal.contracts.ws.client.seal.Constants;
import com.seal.contracts.ws.client.seal.Fields;
import com.seal.contracts.ws.client.seal.SealService;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraj on 22.08.2017.
 */
@Service
@Slf4j
public class MetadataUpdaterHelper {

    @Value("${seal.update.migrationready.enabled}")
    private boolean enabled;

    @Value("${seal.update.migrationready.file}")
    private String filePath;

    private enum STATUS {READY, LOCKED, PROCESSED, PROCESSED_ERROR}

    @Autowired
    private SealService sealService;

    private DataProducer producer;

    @PostConstruct
    private void init() {
        producer = new RecordsProducer(sealService);
        producer.init();
    }

    private class RecordsProducer extends AbstractDataProducer<Record, Boolean, MetadataPublisher> {

        private final SealService sealService;
        private final File FILE = Paths.get(filePath).toFile();
        private final CSVLoader<Record> LOADER = new CSVLoader(FILE, Record.class);

        @Getter
        private final List<Record> DB = Lists.newArrayList();

        private RecordsProducer(SealService sealService) {
            this.sealService = sealService;
        }

        private final AtomicInteger numberProcessed = new AtomicInteger();

        @Override
        public void processed(Boolean result, Record input) throws Exception {
            input.unlock(result);
            int i = numberProcessed.incrementAndGet();
            log.info("{} items processed, {} left", i, DB.size() - i);
        }

        @Override
        public synchronized List<Record> retrieve() {
            List<Record> returnValue = Lists.newArrayList();
            if (!enabled) {
                log.info("Feature disabled --> skipping", this);
                return returnValue;
            }
            try {
                returnValue.addAll(LOADER.load(new Predicate<Record>() {
                    @Override
                    public boolean apply(Record record) {
                        boolean isReady = record.status == STATUS.READY;
                        boolean isInDB = FluentIterable.from(DB).anyMatch(new Predicate<Record>() {
                            @Override
                            public boolean apply(Record dbRecord) {
                                return dbRecord.getHash().equals(record.getHash());
                            }
                        });
                        return isReady && !isInDB;
                    }
                }));
                FluentIterable.from(returnValue).transform(new Function<Record, Record>() {
                    @Override
                    public Record apply(Record record) {
                        record.lock();
                        return record;
                    }
                });
                DB.addAll(returnValue);
            } catch (IOException e) {
                log.error(String.format("Exception occurred when retrieving the records -> %s", e));
            }
            return returnValue;
        }

        @Override
        public MetadataPublisher createNewConsumer() throws Exception {
            return new MetadataPublisher(this, sealService);
        }

        @Override
        public int poolSize() {
            return 10;
        }

        @Override
        public void error(Exception exception, Record item) {
            log.error("Exception occurred - >{}", exception);
        }

    }

    private class MetadataPublisher extends DataConsumer<Record, Boolean> {

        private final SealService sealService;

        protected MetadataPublisher(DataProducer<Record, Boolean> producer, SealService sealService) {
            super(producer);
            this.sealService = sealService;
        }

        @Override
        public Boolean process(Record item) throws Exception {
            ResponseEntity<Object> result = sealService.updateMetadata(item.getHash(), Fields.MIGRATION_READY, Constants.YES, String.class);
//            ResponseEntity<Object> result = new ResponseEntity<Object>(HttpStatus.OK);
            if (result.getStatusCode().is2xxSuccessful()) {
                log.info(String.format("Metadata for Document {%s} successfully updated", item.getHash()));
                return Boolean.TRUE;
            }
            log.info(String.format("Metadata update for Document {%s} finished with error", item.getHash(), result.getStatusCode().toString()));
            return Boolean.FALSE;
        }
    }

    @Getter
    public static final class Record implements Threadable {

        @Parsed(field = "ContractId")
        private String hash;

        private STATUS status = STATUS.READY;

        @Override
        public void lock() {
            status = STATUS.LOCKED;
        }

        @Override
        public void unlock(boolean success) {
            if (success) {
                status = STATUS.PROCESSED;
            } else {
                status = STATUS.PROCESSED_ERROR;
            }
        }
    }
}
