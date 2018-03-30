package com.seal.contracts.ws.client.seal.pull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.*;
import com.seal.contracts.generator.csv.enums.seal.ReviewType;
import com.seal.contracts.generator.csv.service.*;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.threading.AbstractDataProducer;
import com.seal.contracts.ws.client.seal.Constants;
import com.seal.contracts.ws.client.seal.SealService;
import com.seal.contracts.ws.client.seal.meta.Item;
import com.seal.contracts.ws.client.seal.meta.SealContractsResponse;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import static com.seal.contracts.ws.client.seal.Constants.DATE_FORMAT;

/**
 * Created by jantonak on 19/07/17.
 */
@Slf4j
@Service
public class SealPullProducer extends AbstractDataProducer<SimpleExports, ContractImportItem, SealPullConsumer> {

    private static final int NUM_OF_THREADS = 1;

    @Value("${seal.pull.enabled}")
    private boolean enabled;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ContractImportItemRepository itemsRepository;

    @Autowired
    private ContractImportDocumentRepository documentsRepository;

    @Autowired
    private SealService sealService;

    @Autowired
    private FolderMappingService folderMappingService;

    @Getter
    private final List<SimpleExport> DB = Lists.newArrayList();

    private Configuration configuration;

    @Override
    public void processed(ContractImportItem result, SimpleExports input) throws Exception {
//        docRepository.save(result);
//        repository.save(input);
    }


    @Override
    public synchronized List<SimpleExports> retrieve() {
        if (!enabled) {
            Lists.newArrayList();
        }
        List<SimpleExports> returnValue = Lists.newArrayList();
        final CSVLoader<SimpleExport> csvLoader = new CSVLoader<>(configService.getInFile(), SimpleExport.class);
        final List<SimpleExport> csvRecords = Lists.newArrayList();
        try {
            csvRecords.addAll(csvLoader.load(new Predicate<SimpleExport>() {
                @Override
                public boolean apply(SimpleExport simpleExport) {
                    boolean inDB = DB.contains(simpleExport);
                    boolean isImported = false;

                    ContractImportDocument doc = documentsRepository.findOne(simpleExport.getHash());
                    if (doc != null) {
                        ContractImportItem header = itemsRepository.findOne(doc.getContract());
//                        isImported = ContractImportItemStatus.ALL_IMPORTED == header.getStatus();
                    }
                    return !inDB; //&& !isImported;
                }
            }));
            DB.addAll(csvRecords);
        } catch (IOException e) {
            log.error("Exception occured: {}", e);
        }

        if (csvRecords.isEmpty()) {
            return Lists.newArrayList();
        }

        Set<Field> headerSealFields = Sets.newHashSet();
        for (final Field field : HeaderDetailExport.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parsed.class)) {
                headerSealFields.add(field);
            }
        }

        int queueSize = csvRecords.size();
        ArrayBlockingQueue<SimpleExport> queue = new ArrayBlockingQueue<SimpleExport>(queueSize, true, csvRecords);

        ArrayBlockingQueue<SimpleExport> results = new ArrayBlockingQueue<SimpleExport>(queueSize, true);

        for (int i = 0; i < 100; i++) {
            new MetaDownloader(queue, String.format("MetaDownloader_%s", i + 1), sealService, results).start();
        }

        while (results.remainingCapacity() != 0) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<SimpleExport> filteredResults = results.stream().filter(simpleExport -> simpleExport.isMigrationReady()).collect(Collectors.toList());
        log.info("Number of records to be processed:{}", filteredResults.size());
        ImmutableListMultimap<String, SimpleExport> map = Multimaps.index(filteredResults, new Function<SimpleExport, String>() {
            @Override
            public String apply(SimpleExport record) {
                return record.getLegacyContractId();
            }
        });

        for (Map.Entry<String, Collection<SimpleExport>> entry : map.asMap().entrySet()) {
            final String legacyContractId = entry.getKey();
            final Collection<SimpleExport> records = entry.getValue();
            Map<String, HeaderDetailExport> headerDetails = Maps.newHashMap();

            for (SimpleExport record : records) {
                final String hash = record.getHash();
                final SealContractsResponse contractDetails = record.getData();
                final SealContractsResponse.MetaData metaData = contractDetails.getMetaData();

                final String owner = metaData.getOwner().or(Constants.UNKNOWN);
                final String fileName = contractDetails.getFileName();
                final String documentType = metaData.getDocumentType().or(Constants.UNKNOWN);
                final TargetReference targetReference = new AribaReference(metaData.getAribaContractId(), metaData.getAribaDocumentId());

                ReviewType reviewType = metaData.getReviewType();
                boolean reviewComplete = metaData.getReviewComplete();

                if (!headerDetails.containsKey(legacyContractId)) {
                    final HeaderDetailExport header = new HeaderDetailExport(legacyContractId, owner);
                    headerDetails.put(legacyContractId, header);
                }

                final HeaderDetailExport headerDetailExport = headerDetails.get(legacyContractId);
                final HeaderDetailExport.Detail detail = new HeaderDetailExport.Detail(hash, documentType, fileName, reviewType, reviewComplete, targetReference);
                headerDetailExport.getDetails().add(detail);

                boolean isPrimary = (reviewType == ReviewType.PRIMARY_DOCUMENT);
                if (isPrimary) {
                    for (final Field sealField : headerSealFields) {
                        final Parsed parsed = sealField.getAnnotation(Parsed.class);
                        final String sealName = parsed.field();
                        final Optional<Item> item = metaData.findItem(sealName);
                        if (item.isPresent()) {
                            Object value = item.get().getConcatenatedValues();
                            if (value != null) {
                                if (sealField.getType().equals(Date.class)) {
                                    try {
                                        value = DATE_FORMAT.parse(value.toString());
                                    } catch (ParseException e) {
                                        value = new Date(0);
                                        String message = String.format("Unable to parse value {} in field {}", value.toString(), sealField);
                                        record.getErrors().add(message);
                                        log.error(message);
                                    }
                                }
                                if (sealField.getType().equals(Integer.class)) {
                                    value = Integer.valueOf(value.toString());
                                }
                                if (sealField.getType().equals(BigDecimal.class)) {
                                    value = new BigDecimal(value.toString());
                                }
                                if (sealField.getType().equals(Boolean.class)) {
                                    value = Boolean.valueOf(value.toString());
                                }
                                ReflectionUtils.setField(sealField, headerDetailExport, value);
                            }
                        }
                    }
                }
                headerDetailExport.getErrors().addAll(record.getErrors());
            }

            SimpleExports simpleExports = new SimpleExports(legacyContractId);

            for (HeaderDetailExport header : headerDetails.values()) {
                for (HeaderDetailExport.Detail detail : header.getDetails()) {
                    Export export = new Export(detail.getContractId(), detail.getFilePath(), detail.getFileName(), detail.getReviewType(), detail.isReviewComplete(), detail.getTargetReference());
                    List<Field> exportFields = Arrays.asList(Export.class.getDeclaredFields());
                    Field[] headerDetailsFields = HeaderDetailExport.class.getDeclaredFields();
                    for (Field headerField : headerDetailsFields) {
                        Optional<Field> existingField = FluentIterable.from(exportFields).firstMatch(new Predicate<Field>() {
                            @Override
                            public boolean apply(Field field) {
                                boolean isSealField = field.getAnnotationsByType(Parsed.class).length > 0;
                                return isSealField && field.getName().equals(headerField.getName());
                            }
                        });
                        if (existingField.isPresent()) {
                            headerField.setAccessible(true);
                            try {
                                ReflectionUtils.setField(existingField.get(), export, headerField.get(header));
                            } catch (IllegalAccessException e) {
                                String message = String.format("Unable to set the field {} -> {}", existingField.get().getName(), e.getMessage());
                                header.getErrors().add(message);
                                log.error(message);
                            }
                        }
                    }
                    export.getErrors().addAll(header.getErrors());
                    simpleExports.getExports().add(export);
                }
                returnValue.add(simpleExports);
            }
        }
        log.info("Master data for current batch have been downloaded");
        return returnValue;
    }

    @Override
    @PostConstruct
    public void init() {
        if (enabled) {
            try {
                configuration = new Configuration(configService);
                super.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SealPullConsumer createNewConsumer() throws Exception {
        return new SealPullConsumer(this, configService, folderMappingService, itemsRepository, documentsRepository, configuration.usersService, configuration.supplierService, configuration.enumerationService, configuration.commodityCodeService, configuration.exportService, documentsRepository);
    }

    @Override
    public int poolSize() {
        return NUM_OF_THREADS;
    }

    @Override
    public void error(Exception exception, SimpleExports item) {
        String stackTrace = Throwables.getStackTraceAsString(exception);
        String message = String.format("Error occurred on item: %s -> %s", item, stackTrace);
        log.error(message);
        item.unlock(false);
    }

    @Getter
    private static class Configuration {
        private final UsersService usersService;
        private final ExportServiceImpl exportService;
        private final SupplierServiceImpl supplierService;
        private final EnumerationService enumerationService;
        private final CommodityCodeServiceImpl commodityCodeService;

        private Configuration(ConfigService configService) throws IOException {
            usersService = new UsersServiceImpl(configService.getUserFile(), configService.getDefaultOwner());
            exportService = new ExportServiceImpl(Lists.newArrayList());
            supplierService = new SupplierServiceImpl(configService.getSupplierFile(), exportService);
            enumerationService = new EnumerationServiceImpl(configService.getEnumsFile());
            commodityCodeService = new CommodityCodeServiceImpl(configService.getCommodityCodesFile());
        }
    }

    private static class MetaDownloader extends Thread {
        private final BlockingQueue<SimpleExport> queue;
        private final SealService sealService;
        private final BlockingQueue<SimpleExport> results;

        private boolean stop = false;

        private MetaDownloader(BlockingQueue<SimpleExport> queue, String name, SealService sealService, BlockingQueue<SimpleExport> results) {
            this.queue = queue;
            this.sealService = sealService;
            this.results = results;
            setName(name);
        }

        @Override
        public void run() {
            while (!stop) {
                SimpleExport record = queue.poll();
                if (record == null) {
                    log.info("DONE");
                    stop = true;
                } else {
                    log.info("About to retrieve metadata... {} left", queue.size());
                    final String hash = record.getHash();
                    try {
                        final SealContractsResponse sealData = sealService.getMetadata(hash);
                        final SealContractsResponse.MetaData metaData = sealData.getMetaData();
                        final boolean migrationReady = metaData.getMigrationReady();
                        final Optional<String> legacyContractId = metaData.getLegacyContractId();

                        if (!legacyContractId.isPresent()) {
                            record.getErrors().add("LegacyContractId must be set");
                            log.error("LegacyContractId must be set for {}", hash);
                        }
                        record.setLegacyContractId(legacyContractId.or(Constants.UNKNOWN));
                        record.setData(sealData);
                        record.setMigrationReady(migrationReady);
                        if (!migrationReady) {
                            log.info("Migration Flag not set for document {} -> will be skipped", hash);
                        }
                    } catch (Exception e) {
                        record.getErrors().add(e.getMessage());
                    }
                    results.add(record);
                }
            }
        }
    }

}