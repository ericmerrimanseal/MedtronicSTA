package com.seal.contracts.generator.persistence.entity;

/**
 * Created by root on 25.10.15..
 */

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus;
import com.seal.contracts.threading.Threadable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus.*;
import static com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus.*;

@Entity
@Table(name = "CONTRACT_IMPORT_ITEM",
        indexes = {@Index(name = "statusIndex", columnList = "status"),
                @Index(name = "hierachyTypeIndex", columnList = "hierarchicalType", unique = false)})
public class ContractImportItem implements Threadable {

    @Id
    @Getter
    @Column(name = "UNIQUENAME", nullable = false)
    private String uniqueName;

    @Getter
    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "GENERATED_TIME", nullable = false)
    private Long generatedTime;

    @Column(name = "IMPORT_START_TIME")
    private Long importStartTime;

    @Column(name = "IMPORT_FINISH_TIME")
    private Long importFinishTime;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    private ContractImportItemStatus status;

    @Column(name = "HIERARCHICALTYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    @Getter
    private Contract.HIERARCHICAL_TYPE hierarchicalType;

    @Lob
    @Column(name = "CONTRACT", nullable = false, columnDefinition = "BLOB")
    @Getter
    private Contract data;

    @Column(name = "DOCUMENTS")
    @OneToMany(mappedBy = "contract", fetch = FetchType.EAGER)
    @Getter
    private Set<ContractImportDocument> documents = Sets.newHashSet();

    @Column(name = "VERSION", nullable = false)
    @Getter
    private Long version;

    @Column(name = "ARIBAID")
    @Getter
    @Setter
    private String aribaId;

    @Column(name = "WEBJUMPER")
    @Getter
    @Setter
    private String aribaWebJumper;

    @Column(name = "ERRORMESSAGE", columnDefinition = "CLOB")
    @Getter
    @Setter
    @Lob
    private String errorMessage;

    @Getter
    @Setter
    private SealSyncStatus sealSyncStatus = NONE;


    public ContractImportItem() {
    }

    public ContractImportItem(String uniqueName, Long version, Contract data, Optional<String> aribaId, Collection<String> errors) {
        Preconditions.checkNotNull(uniqueName, "uniqueName must not be null");
        Preconditions.checkNotNull(version, "version must not be null");

        this.uniqueName = uniqueName;
        this.version = version;

        this.generatedTime = new Date().getTime();
        this.data = data;

        this.title = data.getTitle();

        if (aribaId.isPresent()) {
            this.aribaId = aribaId.get();
        }


        this.hierarchicalType = Contract.HIERARCHICAL_TYPE.valueOf(Optional.fromNullable(Strings.emptyToNull(data.getHierarchicalType())).or(Contract.HIERARCHICAL_TYPE.UNKNOWN.name()));
        setErrors(errors);
    }

    public void update(Contract data, Collection<String> errors) {
        this.data = data;
        this.title = data.getTitle();
        this.hierarchicalType = Contract.HIERARCHICAL_TYPE.valueOf(Optional.fromNullable(Strings.emptyToNull(data.getHierarchicalType())).or(Contract.HIERARCHICAL_TYPE.UNKNOWN.name()));
        this.version++;
        setErrors(errors);

    }

    public void setErrors(Collection<String> errors) {
        String errorsAsString = errorsToString(errors);
        setErrorMessage(errorsAsString);
        if (errorsAsString != null) {
            setStatus(VALIDATION_FAILED);
        } else {
            setStatus(ContractImportItemStatus.READY);
        }
    }

    public void setImportStartTime(Date importStartTime) {
        Preconditions.checkNotNull(importStartTime, "importStartTime must not be null");
        this.importStartTime = importStartTime.getTime();
    }

    public void setImportFinishTime(Date importFinishTime) {
        this.importFinishTime = importFinishTime != null ? importFinishTime.getTime() : null;
    }

    public Date getGeneratedTime() {
        return new Date(generatedTime);
    }

    public Date getImportStartTime() {
        return (importStartTime != null) ? new Date(importStartTime) : null;
    }

    public Date getImportFinishTime() {
        return (importFinishTime != null) ? new Date(importFinishTime) : null;
    }

    public boolean isReady() {
        return status == ContractImportItemStatus.READY;
    }

    public boolean isLocked() {
        boolean isLocked = status == LOCKED || status == IMPORTING_HEADER || status == IMPORTING_ATTACHMENTS;
        if (!isLocked) {
            if (!getActiveDocuments().isEmpty()) {
                isLocked = status == HEADER_IMPORTED;
            }
        }
        return isLocked;
    }

    public boolean isImported() {
        return status.equalOrAfter(ALL_IMPORTED);
    }

    public boolean isFailed() {
        return status == VALIDATION_FAILED || status == IMPORT_HEADER_FAILED || status == IMPORT_ATTACHMENTS_FAILED || sealSyncStatus == SYNCING_FAILED;
    }

    public long getNumberOfDocuments() {
        return documents.size();
    }

    public long getNumberOfActiveDocuments() {
        return getActiveDocuments().size();
    }

    public boolean hasAribaId() {
        return aribaId != null;
    }

    public long getNumberOfDocumentsImported() {
        return documents.stream().filter(contractImportDocument -> contractImportDocument.hasAribaId()).count();
    }

    public String getErrors() {
        StringBuilder builder = new StringBuilder();
        if (!Strings.isNullOrEmpty(getErrorMessage())) {
            builder.append("HEADER:" + getErrorMessage());
        }
        for (ContractImportDocument document : getActiveDocuments()) {
            if (!Strings.isNullOrEmpty(document.getErrorMessage())) {
                builder.append(String.format("\nDOC:%s", document.getErrorMessage()));
            }
        }
        String s = builder.toString();
        if (s.length() > 255) {
            return s.substring(0, 255);
        }
        return s;
    }

    public void setStatus(ContractImportItemStatus status) {
        this.status = status;
        switch (status) {
            case VALIDATION_FAILED:
            case READY:
            case IMPORT_HEADER_FAILED:
            case IMPORT_ATTACHMENTS_FAILED:
            case HEADER_IMPORTED:
            case ALL_IMPORTED:
                sealSyncStatus = SealSyncStatus.READY;
                break;
        }
    }

    @Override
    public String toString() {
        return "ContractImportItem{" +
                "uniqueName='" + uniqueName + '\'' +
                ", title='" + title + '\'' +
                ", generatedTime=" + generatedTime +
                ", importStartTime=" + importStartTime +
                ", importFinishTime=" + importFinishTime +
                ", status=" + status +
                ", data=" + data +
                ", documents=" + documents +
                ", version=" + version +
                ", aribaId='" + aribaId + '\'' +
                ", aribaWebJumper='" + aribaWebJumper + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContractImportItem item = (ContractImportItem) o;

        return uniqueName.equals(item.uniqueName);

    }

    @Override
    public int hashCode() {
        return uniqueName.hashCode();
    }

    @Override
    public void lock() {
        sealSyncStatus = SYNCING;
    }

    @Override
    public void unlock(final boolean success) {
        boolean overallSuccess = false;
        if (success) {
            boolean hasFailedDocument = FluentIterable.from(getDocuments()).anyMatch(new Predicate<ContractImportDocument>() {
                @Override
                public boolean apply(ContractImportDocument doc) {
                    return doc.getSealSyncStatus() == SealSyncStatus.SYNCING_FAILED;
                }
            });
            if (!hasFailedDocument) {
                overallSuccess = true;
            }
        }

        setSealSyncStatus(overallSuccess ? SealSyncStatus.SYNCED : SealSyncStatus.SYNCING_FAILED);
    }

    private String errorsToString(Collection<String> errors) {
        if (errors != null && !errors.isEmpty()) {
            return Joiner.on("\n").join(errors);
        }
        return null;
    }

    public boolean isActive() {
        return getActiveDocuments().size() > 0;
    }

    public Set<ContractImportDocument> getActiveDocuments() {
        return documents.stream().filter(doc -> doc.isActive()).collect(Collectors.toSet());
    }

}
