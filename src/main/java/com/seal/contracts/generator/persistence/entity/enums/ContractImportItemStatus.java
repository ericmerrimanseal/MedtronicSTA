package com.seal.contracts.generator.persistence.entity.enums;

/**
 * Created by root on 25.10.15..
 */
public enum ContractImportItemStatus {
    VALIDATION_FAILED, READY, LOCKED, QUEUED, IMPORTING_HEADER, IMPORT_HEADER_FAILED, IMPORTING_ATTACHMENTS, IMPORT_ATTACHMENTS_FAILED, HEADER_IMPORTED, ALL_IMPORTED;

    public boolean equalOrAfter(ContractImportItemStatus status) {
        return this.ordinal() >= status.ordinal();
    }

    public boolean canImport() {
        switch (this) {
            case READY:
            case IMPORT_HEADER_FAILED:
            case IMPORT_ATTACHMENTS_FAILED:
            case HEADER_IMPORTED:
                return true;
            default:
                return false;
        }
    }
}
