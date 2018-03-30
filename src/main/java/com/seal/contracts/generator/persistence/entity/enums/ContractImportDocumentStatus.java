package com.seal.contracts.generator.persistence.entity.enums;

/**
 * Created by root on 25.10.15..
 */
public enum ContractImportDocumentStatus {
    INACTIVE, VALIDATION_FAILED, READY, LOCKED, QUEUED, IMPORT_FAILED, IMPORTING, IMPORTED;

    public boolean equalOrAfter(ContractImportDocumentStatus status) {
        return this.ordinal() >= status.ordinal();
    }

    public boolean canResetToReady() {
        switch (this) {
            case IMPORTED:
            case IMPORTING:
            case QUEUED:
            case LOCKED:
                return false;
            default:
                return true;
        }
    }
}
