package com.seal.contracts.generator.persistence.entity;

/**
 * Created by root on 25.10.15..
 */

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus;
import com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

import static com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus.IMPORT_FAILED;
import static com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus.INACTIVE;
import static com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus.NONE;
import static com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus.SYNCING_FAILED;


@Entity
@Table(name = "CONTRACT_IMPORT_DOCUMENT")
public class ContractImportDocument {


    @Id
    @Getter
    @Column(name = "UNIQUENAME", nullable = false)
    private String uniqueName;

    @Column(name = "CONTRACT", nullable = false)
    @Getter
    private String contract;

    @Getter
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "IMPORT_START_TIME")
    private Long importStartTime;

    @Column(name = "IMPORT_FINISH_TIME")
    private Long importFinishTime;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractImportDocumentStatus status;

    @Column(name = "ARIBAID")
    @Getter
    @Setter
    private String aribaId;

    @Column(name = "ERRORMESSAGE", columnDefinition = "CLOB")
    @Getter
    @Setter
    @Lob
    private String errorMessage;

    @Getter
    @Setter
    private String aribaFolder;

    @Getter
    @Setter
    private String hash;

    @Getter
    @Setter
    private SealSyncStatus sealSyncStatus = NONE;

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean active = true;

    public ContractImportDocument() {
    }

    public ContractImportDocument(String uniqueName, String contract, String name, String hash, Collection<String> errors, boolean active) {
        Preconditions.checkNotNull(uniqueName, "uniqueName must not be null");
        Preconditions.checkNotNull(contract, "contract must not be null");
        Preconditions.checkNotNull(hash, "hash must not be null");

        this.uniqueName = uniqueName;
        this.name = name;
        this.contract = contract;
        this.hash = hash;
        this.active = active;
        setErrors(errors);
        updateStatus();
    }

    private void updateStatus() {
        if (!this.active) {
            setStatus(INACTIVE);
        } else {
            String errorMessage = Strings.emptyToNull(getErrorMessage());
            if (errorMessage != null) {
                setStatus(ContractImportDocumentStatus.VALIDATION_FAILED);
            } else {
                setErrorMessage(null);
                if (status.canResetToReady()) {
                    setStatus(ContractImportDocumentStatus.READY);
                }
            }
        }
    }

    private void setErrors(Collection<String> errors) {
        String errorsToString = errorsToString(errors);
        setErrorMessage(errorsToString);
    }

    public void setImportStartTime(Date importStartTime) {
        Preconditions.checkNotNull(importStartTime, "importStartTime must not be null");
        this.importStartTime = importStartTime.getTime();
    }

    public void setImportFinishTime(Date importFinishTime) {
        this.importFinishTime = importFinishTime != null ? importFinishTime.getTime() : null;
    }

    public Date getImportStartTime() {
        return (importStartTime != null) ? new Date(importStartTime) : null;
    }

    public Date getImportFinishTime() {
        return (importFinishTime != null) ? new Date(importFinishTime) : null;
    }

    public boolean isImported() {
        return status.equalOrAfter(ContractImportDocumentStatus.IMPORTED);
    }

    public boolean isFailed() {
        return status == ContractImportDocumentStatus.VALIDATION_FAILED || status == IMPORT_FAILED || sealSyncStatus == SYNCING_FAILED;
    }

    public boolean hasAribaId() {
        return aribaId != null;
    }

    public ContractImportDocumentStatus getStatus() {
        return status;
    }

    public void setStatus(ContractImportDocumentStatus status) {
        this.status = status;
        switch (status) {
            case INACTIVE:
            case VALIDATION_FAILED:
            case READY:
            case IMPORTED:
            case IMPORT_FAILED:
                this.sealSyncStatus = SealSyncStatus.READY;
                break;
        }
    }


    @Override
    public String toString() {
        return "ContractImportDocument{" +
                "uniqueName='" + uniqueName + '\'' +
                ", contract='" + contract + '\'' +
                ", name='" + name + '\'' +
                ", importStartTime=" + importStartTime +
                ", importFinishTime=" + importFinishTime +
                ", status=" + status +
                ", aribaId='" + aribaId + '\'' +
                '}';
    }

    public void update(Optional<String> aribaFolder, Collection<String> errors, boolean active) {
        Optional<String> oldAribaFolderOptional = Optional.fromNullable(this.aribaFolder);
        if (!aribaFolder.equals(oldAribaFolderOptional)) {
            setAribaFolder(aribaFolder.orNull());
        }
        this.active = active;
        setErrors(errors);
        updateStatus();
    }

    private String errorsToString(Collection<String> errors) {
        if (errors != null && !errors.isEmpty()) {
            return Joiner.on("\n").join(errors);
        }
        return null;
    }
}
