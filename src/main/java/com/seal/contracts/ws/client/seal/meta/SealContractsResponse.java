package com.seal.contracts.ws.client.seal.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.enums.seal.ReviewType;
import com.seal.contracts.ws.client.seal.Constants;
import lombok.Getter;

import java.util.List;

import static com.seal.contracts.ws.client.seal.Fields.*;

/**
 * Created by Juraj on 01.08.2017.
 */
@Getter
public class SealContractsResponse {

    private String id;

    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("metadata")
    private MetaData metaData;

    public SealContractsResponse(@JsonProperty(value = "id") String id,
                                 @JsonProperty(value = "filename") String fileName,
                                 @JsonProperty(value = "metadata") MetaData metaData) {
        this.id = id;
        this.fileName = fileName;
        this.metaData = metaData;
    }

    public static class MetaData {

        @JsonIgnore
        private static final Item NOT_FOUND = new Item();

        @JsonProperty
        private List<Item> items;

        @JsonIgnore
        private final ImmutableMap<String, Item> lookup;

        public MetaData(@JsonProperty(value = "items") List<Item> items) {
            this.items = items;
            lookup = Maps.uniqueIndex(items, new Function<Item, String>() {
                @Override
                public String apply(Item item) {
                    return item.getName();
                }
            });
        }

        public Optional<Item> findItem(final String name) {
            return Optional.fromNullable(lookup.get(name));
        }

        public Optional<String> getDocumentType() {
            return Optional.fromNullable(Strings.emptyToNull((String) findItem(DOCUMENT_TYPE).or(NOT_FOUND).getValue()));
        }

        public Optional<String> getLegacyContractId() {
            return Optional.fromNullable(Strings.emptyToNull((String) findItem(LEGACY_CONTRACT_ID).or(NOT_FOUND).getValue()));
        }

        public Optional<String> getOwner() {
            return Optional.fromNullable(Strings.emptyToNull((String) findItem(OWNER).or(NOT_FOUND).getValue()));
        }

        public ReviewType getReviewType() {
            return ReviewType.lookup((String) findItem(REVIEW_TYPE).or(NOT_FOUND).getValue());
        }

        public boolean getReviewComplete() {
            String value = Optional.fromNullable(Strings.emptyToNull((String) findItem(REVIEW_COMPLETE).or(NOT_FOUND).getValue())).or(Constants.NO);
            return Constants.YES.equals(value);
        }

        public boolean getMigrationReady() {
            String value = Optional.fromNullable(Strings.emptyToNull((String) findItem(MIGRATION_READY).or(NOT_FOUND).getValue())).or(Constants.NO);
            return Constants.YES.equals(value);
        }
        public String getAribaContractId() {
            String value = Optional.fromNullable(Strings.emptyToNull((String) findItem(ARIBA_CONTRACTID).or(NOT_FOUND).getValue())).orNull();
            return value;
        }
        public String getAribaDocumentId() {
            String value = Optional.fromNullable(Strings.emptyToNull((String) findItem(ARIBA_DOCUMENTID).or(NOT_FOUND).getValue())).orNull();
            return value;
        }

    }
}

