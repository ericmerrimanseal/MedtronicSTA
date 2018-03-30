package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.enums.ExpirationTermType;
import com.seal.contracts.generator.csv.enums.seal.ReviewType;
import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.conversions.DateConversion;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by root on 11.08.15..
 */
@Getter
public class HeaderDetailExport implements Cloneable {

    public static final String LEGACY_ID_FIELD = "RelatedID_Norm";

    private final String legacyContractId;
    private final String owner;

    public HeaderDetailExport(String legacyContractId, String owner) {
        this.legacyContractId = legacyContractId;
        this.owner = owner;
    }

    private final List<Detail> details = Lists.newArrayList();

    @Parsed(field = "Supplier_Norm")
    private String supplier;
    
    @Parsed(field = "DocumentFile_Norm")
    private String file;
    
    @Parsed(field = "DocumentType_Norm")
    private String documentType;

    @Parsed(field = "Description_Norm")
    private String description;

    @Parsed(field = "Commodity_Norm")
    String commodity;

    @Parsed(field = "Regions_Norm")
    String region;

    @Parsed(field = "ParentContract_Norm")
    String parentAgreement;

    @Parsed(field = "Name_Norm")
    String title;

    @Parsed(field = "EffectiveDate_Norm")
    @Convert(conversionClass = DateConversion.class, args = {"yyyy-MM-dd", "MM/dd/yyyy"})
    private Date effectiveDate;

    @Parsed(field = "ExpirationDate_Norm")
    @Convert(conversionClass = DateConversion.class, args = {"yyyy-MM-dd", "MM/dd/yyyy"})
    private Date expirationDate;

    @Parsed(field = "AgreementDate_Norm")
    @Convert(conversionClass = DateConversion.class, args = {"yyyy-MM-dd", "MM/dd/yyyy"})
    private Date agreementDate;

    @Parsed(field = "TermType_Norm")
    private String expirationTermType = ExpirationTermType.FIXED.getAribaDescription();

    @Parsed(field = "ContractAmount_Norm")
    private BigDecimal amount = BigDecimal.ZERO;

    @Parsed(field = "HierarchicalType_Norm")
    private String hierarchicalType;

    @Parsed(field = "ContractCurrency_Norm")
    private String currency;
    
    @Parsed(field = "DocumentID_Norm")
    private String documentId;
    
    @Parsed(field = "BusnessProjectOwner_Norm")
    private String projectGroup;

    @Parsed(field = "AutoRenewalInterval_Norm")
    private Integer autoRenewalInterval;

    @Parsed(field = "MaxAutoRenewalsAllowed_Norm")
    private Integer maxAutoRenewalsAllowed;

    @Parsed(field = "MedtronicEntity_Norm")
    private String entity;

    @Parsed(field = "ContractCoverage_Norm")
    private String contractCoverage;

    @Parsed(field = "ContractStatus_Norm")
    private String contractStatus;

    @Parsed(field = "BaseLanguage_Norm")
    private String baseLanguage;

    @Parsed(field = "ContractType_Norm")
    private String contractType;

    @Parsed(field = "VMO_Norm")
    private String vmo;

    @Getter
    private final List<String> errors = Lists.newArrayList();

    public String getSupplierId() {
        return supplier;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public HeaderDetailExport cloneWithParentAgreement(String parentAgreement) throws CloneNotSupportedException {
        HeaderDetailExport clone = (HeaderDetailExport) clone();
        clone.parentAgreement = parentAgreement;
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeaderDetailExport that = (HeaderDetailExport) o;

        return legacyContractId.equals(that.legacyContractId);

    }

    @Override
    public int hashCode() {
        return legacyContractId.hashCode();
    }

    @Getter
    public static class Detail {

        private final String contractId;
        private final String filePath;
        private final String fileName;
        private final ReviewType reviewType;
        private final boolean reviewComplete;
        private final TargetReference targetReference;


        public Detail(String contractId, String filePath, String fileName, ReviewType reviewType, boolean reviewComplete, TargetReference targetReference) {
            this.contractId = contractId;
            this.filePath = filePath;
            this.fileName = fileName;
            this.reviewType = reviewType;
            this.reviewComplete = reviewComplete;
            this.targetReference = targetReference;
        }

    }
}
