package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.enums.ExpirationTermType;
import com.seal.contracts.generator.csv.enums.seal.ReviewType;
import com.univocity.parsers.annotations.BooleanString;
import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.conversions.DateConversion;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by root on 11.08.15..
 */
@Data
public class Export implements Cloneable {

    private final String contractId;
    private final String filePath;
    private final String fileName;
    private final ReviewType reviewType;
    private final boolean reviewComplete;
    private final TargetReference targetReference;

    public Export(String contractId, String filePath, String fileName, ReviewType reviewType, boolean reviewComplete, TargetReference targetReference) {
        this.contractId = contractId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.reviewType = reviewType;
        this.reviewComplete = reviewComplete;
        this.targetReference = targetReference;
    }

    @Parsed(field = "Description_Norm")
    private String description;
    
    @Parsed(field = "AffectedParties_Norm")
    private String affectedParties;

    @Parsed(field = "ProjectOwner_Norm")
    private String owner;
    
    @Parsed(field = "BusinessContractOwner_Norm")
    private String projectGroup;
    
    @Parsed(field = "DocumentID_Norm")
    private String documentId;

    @Getter
    @Parsed(field = "Commodity_Norm")
    private String commodity;

    @Getter
    @Parsed(field = "Regions_Norm")
    private String region;
    
    @Getter
    @Parsed(field = "RelatedID_Norm")
    private String relatedId;
    
    @Getter
    @Parsed(field = "RelatedID_Norm")
    private String legacyContractId;

    @Parsed(field = "ParentContract_Norm")
    private String parentAgreement;

    @Parsed(field = "Supplier_Norm")
    private String supplier;

    @Parsed(field = "Name_Norm")
    private String title;
    
    
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

    @Parsed(field = "AutoRenewalInterval_Norm")
    private Integer autoRenewalInterval;

    @Parsed(field = "MaxAutoRenewalsAllowed_Norm")
    private Integer maxAutoRenewalsAllowed;    

    @Parsed(field = "BaseLanguage_Norm")
    private String baseLanguage;
    
    @Parsed(field = "VMO_Norm")
    private String vmo;
    
    @Parsed(field = "MeditronicEntity_Norm")
    private String entity;
    
    @Parsed(field = "ContractType_Norm")
    private String contractType;  
    
    @Parsed(field = "ContractStatus_Norm")
    private String contractStatus;
    
    @Parsed(field = "ContractCoverage_Norm")
    private String contractCoverage;
    
    @Parsed(field = "BusinessGroup_Norm")
    private String department;

    public String getDocumentType() {
        return Iterables.getLast(com.google.common.base.Splitter.on("\\").split(filePath));
    }

    private final List<String> errors = Lists.newArrayList();

    public String getSupplierId() {
        return supplier;
    }

}