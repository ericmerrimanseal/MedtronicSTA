package com.seal.contracts.generator.csv.service;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Created by jantonak on 26/09/17.
 */
@Service
public class CleanupService {

    @Autowired
    private ContractImportItemRepository itemsRepository;

    @Autowired
    private ContractImportDocumentRepository documentRepository;

    @PostConstruct
    private void cleanup() {
        Iterable<ContractImportItem> all = itemsRepository.findAll();

        ImmutableList<ContractImportItem> lockedOnes = FluentIterable.from(all).filter(new Predicate<ContractImportItem>() {
            @Override
            public boolean apply(ContractImportItem item) {
                return item.isLocked() || item.getSealSyncStatus() == SealSyncStatus.SYNCING;
            }
        }).toList();

        ImmutableList<ContractImportItem> syncFailed = FluentIterable.from(all).filter(new Predicate<ContractImportItem>() {
            @Override
            public boolean apply(ContractImportItem item) {
                return item.getSealSyncStatus() == SealSyncStatus.SYNCING_FAILED || item.getSealSyncStatus() == SealSyncStatus.SYNCING;
            }
        }).toList();


        for (ContractImportItem locked : lockedOnes) {
            locked.setStatus(ContractImportItemStatus.READY);
        }
        itemsRepository.save(lockedOnes);

        Set<ContractImportDocument> failedDocs = Sets.newHashSet();
        for (ContractImportItem failed : syncFailed) {
            for (ContractImportDocument doc : failed.getActiveDocuments()) {
                if (doc.getSealSyncStatus() == SealSyncStatus.SYNCING) {
                    doc.setSealSyncStatus(SealSyncStatus.SYNCING_FAILED);
                    failedDocs.add(doc);
                }
            }

        }
        itemsRepository.save(syncFailed);
        documentRepository.save(failedDocs);

    }

}
