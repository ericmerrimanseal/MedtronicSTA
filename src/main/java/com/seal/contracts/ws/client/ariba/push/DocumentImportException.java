package com.seal.contracts.ws.client.ariba.push;

import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import lombok.Getter;

/**
 * Created by jantonak on 2.11.2015.
 */
public class DocumentImportException extends Exception {

    @Getter
    private ContractImportDocument doc;

    public DocumentImportException(Throwable throwable, ContractImportDocument doc) {
        super(throwable);
        this.doc = doc;
    }

    public DocumentImportException(String s, ContractImportDocument doc) {
        super(s);
        this.doc = doc;
    }
}
