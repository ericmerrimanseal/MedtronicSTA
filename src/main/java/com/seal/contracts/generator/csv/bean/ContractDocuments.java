package com.seal.contracts.generator.csv.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.csv.exception.ValidationException;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

/**
 * Created by root on 11.08.15..
 */
public class ContractDocuments implements Validatable {

    @Getter
    Collection<ContractDocument> documents = Sets.newHashSet();

    public static ContractDocumentsBuilder newBuilder() {
        return new ContractDocumentsBuilder();
    }

    @Override
    public List<ValidationException> getErrors() {
        return Lists.newArrayList();
    }

    public static class ContractDocumentsBuilder {
        private final ContractDocuments contractDocuments;

        public ContractDocumentsBuilder() {
            this.contractDocuments = new ContractDocuments();
        }

        public ContractDocuments build() {
            return this.contractDocuments;
        }

        public ContractDocumentsBuilder(ContractDocuments copyFrom) {
            this();
            this.contractDocuments.documents.addAll(copyFrom.documents);
        }

        public ContractDocumentsBuilder addDocument(ContractDocument document) {
            this.contractDocuments.documents.add(document);
            return this;
        }

        public ContractDocumentsBuilder addDocuments(Collection<ContractDocument> documents) {
            this.contractDocuments.documents.addAll(documents);
            return this;
        }

    }


}
