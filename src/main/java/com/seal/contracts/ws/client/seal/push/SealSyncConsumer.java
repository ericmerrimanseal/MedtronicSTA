package com.seal.contracts.ws.client.seal.push;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus;
import com.seal.contracts.threading.DataConsumer;
import com.seal.contracts.threading.DataProducer;
import com.seal.contracts.ws.client.seal.Constants;
import com.seal.contracts.ws.client.seal.Fields;
import com.seal.contracts.ws.client.seal.SealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jantonak on 19/07/17.
 */
@Slf4j
public class SealSyncConsumer extends DataConsumer<ContractImportItem, Iterable<ContractImportDocument>> {

    private final SealService sealService;

    protected SealSyncConsumer(DataProducer<ContractImportItem, Iterable<ContractImportDocument>> producer, SealService sealService) {
        super(producer);
        this.sealService = sealService;
    }

    @Override
    public Iterable<ContractImportDocument> process(ContractImportItem item) throws Exception {

        Set<ContractImportDocument> docs = item.getDocuments();

        for (ContractImportDocument doc : docs) {
            Map<String, List<Object>> fieldsAndValues = Maps.newHashMap();

            boolean hasError = !Strings.isNullOrEmpty(item.getErrorMessage()) || !Strings.isNullOrEmpty(doc.getErrorMessage());
            if (hasError) {
                fieldsAndValues.put(Fields.MIGRATION_READY, Lists.newArrayList(Constants.ERROR));
            } else {
                fieldsAndValues.put(Fields.MIGRATION_READY, Lists.newArrayList(Constants.NO));
            }
            fieldsAndValues.put(Fields.ARIBA_CONTRACTID, Lists.newArrayList(Strings.nullToEmpty(item.getAribaId())));
            fieldsAndValues.put(Fields.ARIBA_DOCUMENTID, Lists.newArrayList(Strings.nullToEmpty(doc.getAribaId())));

            List<String> errors = Lists.newArrayList();
            if (!Strings.isNullOrEmpty(item.getErrorMessage())) {
                errors.add(item.getErrorMessage());
            }
            if (!Strings.isNullOrEmpty(doc.getErrorMessage())) {
                errors.add(doc.getErrorMessage());
            }
            if (errors.isEmpty()) {
                errors.add("");
            }
            fieldsAndValues.put(Fields.MIGRATION_ERRORS, Lists.newArrayList(errors));

            List<String> syncErrors = Lists.newArrayList();
            for (Map.Entry<String, List<Object>> fieldAndValues : fieldsAndValues.entrySet()) {
                ResponseEntity<Object> result = sealService.updateMetadata(doc.getHash(), fieldAndValues.getKey(), fieldAndValues.getValue(), String.class);
                if (result.getStatusCode().is2xxSuccessful()) {
                    log.debug("Field {} successfully synced for Document {}", fieldAndValues.getKey(), doc.getUniqueName());
                } else {
                    syncErrors.add(result.getStatusCode().toString());
                }
            }

            if (syncErrors.isEmpty()) {
                doc.setSealSyncStatus(SealSyncStatus.SYNCED);
                item.unlock(true);
                log.info("Document {} successfully synced", doc.getUniqueName());
            } else {
                final String errorMessage = Joiner.on("\n").join(syncErrors);
                log.error("Error occurred while syncing document {} --> {}", doc.getUniqueName(), errorMessage);
                doc.setSealSyncStatus(SealSyncStatus.SYNCING_FAILED);
                doc.setErrorMessage(errorMessage);
                item.unlock(false);
            }
        }
        return docs;
    }
}
