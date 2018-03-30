package com.seal.contracts.ws.client.seal.push;

import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.service.ConfigService;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.threading.AbstractDataProducer;
import com.seal.contracts.ws.client.seal.SealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by jantonak on 19/07/17.
 */
@Slf4j
@Service
public class SealSyncProducer extends AbstractDataProducer<ContractImportItem, Iterable<ContractImportDocument>, SealSyncConsumer> {

    private static final int NUM_OF_THREADS = 10;

    @Value("${seal.sync.enabled}")
    private boolean enabled;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ContractImportItemRepository repository;

    @Autowired
    private ContractImportDocumentRepository docRepository;

    @Autowired
    private SealService sealService;

    @Override
    public void processed(Iterable<ContractImportDocument> result, ContractImportItem input) throws Exception {
        docRepository.save(result);
        repository.save(input);
    }

    @Override
    public synchronized List<ContractImportItem> retrieve() {
        List<ContractImportItem> items = Lists.newArrayList();
        if (!enabled) {
            return items;
        }
        items.addAll(repository.findBySealSyncStatus(SealSyncStatus.READY));
        items.addAll(repository.findBySealSyncStatus(SealSyncStatus.SYNCING_FAILED));

        for (ContractImportItem item : items) {
            item.lock();
        }
        repository.save(items);
        return items;
    }

    @Override
    @PostConstruct
    public void init() {
        if (enabled) {
            super.init();
        }
    }

    @Override
    public SealSyncConsumer createNewConsumer() throws Exception {
        return new SealSyncConsumer(this, sealService);
    }

    @Override
    public int poolSize() {
        return NUM_OF_THREADS;
    }

    @Override
    public void error(Exception exception, ContractImportItem item) {
        String message = String.format("Error occurred while updating the ContractId %s", exception.getMessage());
        log.error(message);
        item.setErrorMessage(message);
        item.unlock(false);
        repository.save(item);
    }
}
