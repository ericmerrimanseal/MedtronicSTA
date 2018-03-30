package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.annotation.AribaField;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.conversions.DateConversion;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by root on 11.08.15..
 */
//@CsvDataType
public class Contract implements Serializable,Validatable {

    public enum HIERARCHICAL_TYPE {MasterAgreement, SubAgreement, StandAlone, UNKNOWN}

    public enum STATUS {Expired, Draft}

    @Setter
    @Parsed(field = "Owner")
    private User owner;

    @Setter
    @Parsed(field = "Title")
    private String title;	

    @Setter
    @Parsed(field = "ContractId")
    private String contractId;

    @Setter
    @Parsed(field = "Description")
    private String description;

    @Setter
    @Parsed(field = "Supplier")
    private String supplier;

    @Setter
    @Parsed(field = "AffectedParties")
    private String affectedParties;

    @Setter
    @Parsed(field = "HierarchicalType")
    private String hierarchicalType;

    @Setter
    @Parsed(field = "ParentAgreement")
    private String parentAgreement;

    @Setter
    @Parsed(field = "Amount")
    private BigDecimal amount;

    @Setter
    @Parsed(field = "Commodity")
    private String commodity;

    @Setter
    @Parsed(field = "Region")
    private String region;

    @Setter
    @Parsed(field = "Department")
    private String department;

    @Setter
    @Parsed(field = "ExpirationTermType")
    private String expirationTermType;
    
    @Setter
    @Parsed(field = "File")
    private String file;

    // in months
    @Setter
    @Parsed(field = "AutoRenewalInterval")
    private Integer autoRenewalInterval;

    @Setter
    @Parsed(field = "MaxAutoRenewalsAllowed")
    private Integer maxAutoRenewalAllowed;

    @Setter
    @Parsed(field = "AgreementDate")
    @Convert(conversionClass = DateConversion.class, args = ("MM/dd/yyyy"))
    private Date agreementDate;

    @Setter
    @Parsed(field = "EffectiveDate")
    @Convert(conversionClass = DateConversion.class, args = ("MM/dd/yyyy"))
    private Date effectiveDate;

    @Setter
    @Parsed(field = "ExpirationDate")
    @Convert(conversionClass = DateConversion.class, args = ("MM/dd/yyyy"))
    private Date expirationDate;

    @Setter
    @Parsed(field = "NoticePeriod")
    private String noticePeriod;
    
    @Setter
    @Parsed(field = "ProjectGroup")
    private String projectGroup;

    @Setter
    @Parsed(field = "NoticeEmailRecipients")
    private String noticeEmailRecipients;

    @Setter
    @Parsed(field = "ExpiringEmailRecipients")
    private String expiringEmailRecipients;

    @Setter
    @Parsed(field = "RelatedId")
    private String relatedId;

    @Setter
    @Parsed(field = "Contract Status")
    private String contractStatus;

    @Setter
    @Parsed(field = "Base Language")
    private String baseLanguage;

    @Setter
    @Parsed(field = "Currency")
    private String currency;
    
    @Setter
    @Getter
    @AribaField(name = "Contract Type",custom = true)
    private String cus_ContractType;

    @Getter
    @Setter
    @AribaField(name = "Business Contract Owner",custom = true)
    private String cus_BusinessContractOwner;

    @Getter
    @Setter
    @AribaField(name = "VMO Group",custom = true)
    private String cus_VMOGroup;
    
    @Setter
    @Getter
    @AribaField(name = "Entity",custom = true)
    private String cus_Entity;
    
    @Setter
    @Getter
    @AribaField(name = "Contract Coverage",custom = true)
    private String cus_ContractCoverage;
    
    @Getter
    private List<ValidationException> errors = Lists.newArrayList();

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return normalize(title);
    }

    public String getContractId() {
        return normalize(contractId);
    }

    public String getDescription() {
        return normalize(description);
    }

    public String getSupplier() {
        return normalize(supplier);
    }

    public String getAffectedParties() {
        return normalize(affectedParties);
    }

    public String getHierarchicalType() {
        return normalize(hierarchicalType);
    }

    public String getParentAgreement() {
        return normalize(parentAgreement);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCommodity() {
        return normalize(commodity);
    }

    public String getRegion() {
        return normalize(region);
    }

    public String getDepartment() {
        return normalize(department);
    }

    public String getExpirationTermType() {
        return normalize(expirationTermType);
    }

    public Integer getAutoRenewalInterval() {
        return autoRenewalInterval;
    }

    public Integer getMaxAutoRenewalAllowed() {
        return maxAutoRenewalAllowed;
    }

    public Date getAgreementDate() {
        return agreementDate;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getNoticePeriod() {
        return normalize(noticePeriod);
    }

    public String getNoticeEmailRecipients() {
        return normalize(noticeEmailRecipients);
    }

    public String getExpiringEmailRecipients() {
        return normalize(expiringEmailRecipients);
    }

    public String getRelatedId() {
        return normalize(relatedId);
    }

    public String getContractStatus() {
        return normalize(contractStatus);
    }

    public String getCurrency() {
        return normalize(currency);
    }

    public String getCus_BusinessContractOwner() {
        return normalize(cus_BusinessContractOwner);
    }

    public String getCus_VMOGroup() {
        return normalize(cus_VMOGroup);
    }

    public String getCus_Entity() {
        return normalize(cus_Entity);
    }
    

    public String getCus_ContractType() {
        return normalize(cus_ContractType);
    }
    
    public String getCus_ContractCoverage() {
        return normalize(cus_ContractCoverage);
    }
	
	public String getBaseLanguage() {
        return normalize(baseLanguage);
    }

    private String normalize(String value) {
        return Strings.isNullOrEmpty(value) ? "" : value;
    }

    private Boolean normalize(Boolean value) {
        return value == null ? false : value;
    }

}

