package com.seal.contracts.generator.csv.service;

import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.ws.client.ariba.push.ImportException;
import com.seal.contracts.ws.client.ariba.push.AribaContractImportClient;

import java.util.List;

/**
 * Created by jantonak on 2.11.2015.
 */
public interface ImportService {
    //    void importItem(Iterable<ContractImportItem> items) throws ImportException;
    enum STATUS {
        IN_PROGRESS,IDLE
    }

    int getLeft();

    void add(List<ContractImportItem> items) throws ImportException;

    STATUS getStatus();

    List<AribaContractImportClient.Consumer> getConsumers();
}
