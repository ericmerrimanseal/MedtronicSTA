package com.seal.contracts.ws.client.ariba.push;

import com.google.common.collect.Maps;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Created by jantonak on 2.11.2015.
 */
public class DocumentsImportException extends Exception {

    @Getter
    private Map<ContractImportDocument, DocumentImportException> docToExceptionMap = Maps.newHashMap();

    public DocumentsImportException(Throwable throwable, List<DocumentImportException> exceptions) {
        super(throwable);
        for (DocumentImportException ex : exceptions) {
            this.docToExceptionMap.put(ex.getDoc(), ex);
        }

    }
}
