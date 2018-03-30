package com.seal.contracts.generator.persistence.entity.enums;

/**
 * Created by root on 25.10.15..
 */
public enum SealSyncStatus {
    NONE, READY, SYNCING, SYNCING_FAILED, SYNCED;

    public boolean equalOrAfter(SealSyncStatus status) {
        return this.ordinal() >= status.ordinal();
    }

    public boolean before(SealSyncStatus status) {
        return this.ordinal() < status.ordinal();
    }

}
