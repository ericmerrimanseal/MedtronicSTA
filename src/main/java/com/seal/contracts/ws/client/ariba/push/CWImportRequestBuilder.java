package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceImportRequest;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.WSContractWorkspaceInputBean;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jantonak on 24/11/16.
 */
public class CWImportRequestBuilder {

    private enum ACTION {Create, Update}

    private final Contract contract;
    private final Config config;

    public CWImportRequestBuilder(Contract contract, Config config) {
        this.config = config;
        Preconditions.checkNotNull(contract, "contract must not be null");
        this.contract = contract;
    }

    public ContractWorkspaceImportRequest buildContractWorkspaceImportRequest(Optional<String> targetSystemId, Optional<ContractImportItem> parentItem) throws DatatypeConfigurationException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        ObjectFactory fact = new ObjectFactory();
        ContractWorkspaceImportRequest request = fact.createContractWorkspaceImportRequest();
        ContractWorkspaceImportRequest.WSContractWorkspaceInputBeanItem inputBeanItem = fact.createContractWorkspaceImportRequestWSContractWorkspaceInputBeanItem();
        request.setWSContractWorkspaceInputBeanItem(inputBeanItem);
        WSContractWorkspaceInputBean bean = new ObjectFactory().createWSContractWorkspaceInputBean();
        bean.setAction(targetSystemId.isPresent() ? ACTION.Update.name() : ACTION.Create.name());

        if (config.isUseDefaultOwner()) {
            bean.setOnBehalfUserId(config.getDefaultOwner());
            bean.setOnBehalfUserPasswordAdapter(config.getDefaultOwnerPasswordAdapter());
        } else {
            bean.setOnBehalfUserId(contract.getOwner().getUniqueName());
            bean.setOnBehalfUserPasswordAdapter(contract.getOwner().getPasswordAdapter().get());
        }

        inputBeanItem.setItem(bean);
        bean.setTemplateId(config.getAribaTemplate());
        bean.setWorkspaceId(targetSystemId.isPresent() ? targetSystemId.get() : "");

        bean.setParentWorkspaceId("");
        bean.setParentAgreementId("");
        if (contract.getHierarchicalType().equals(Contract.HIERARCHICAL_TYPE.SubAgreement.name())) {
            String parentTargetSystemId = parentItem.get().getAribaId();
            bean.setParentAgreementId(parentTargetSystemId);
        }

        ContractWorkspaceWSProjectImport headerFields = new ContractWorkspaceWSProjectImport();
        bean.setProjectHeaderFields(headerFields);

        //Title
        ContractWorkspaceWSProjectImport.Title title = fact.createContractWorkspaceWSProjectImportTitle();
        title.setDefaultStringTranslation(fact.createContractWorkspaceWSProjectImportTitleDefaultStringTranslation(contract.getTitle()));
        headerFields.setTitle(title);

        //Description
        ContractWorkspaceWSProjectImport.Description description = fact.createContractWorkspaceWSProjectImportDescription();
        description.setDefaultStringTranslation(fact.createContractWorkspaceWSProjectImportDescriptionDefaultStringTranslation(contract.getDescription()));
        headerFields.setDescription(description);

        //EffectiveDate
        headerFields.setEffectiveDate(dateToXMLGregorianCalendar(contract.getEffectiveDate()));

        //Supplier
        ContractWorkspaceWSProjectImport.Supplier supplier = fact.createContractWorkspaceWSProjectImportSupplier();
        supplier.setSystemID(fact.createContractWorkspaceWSProjectImportSupplierSystemID(String.valueOf(contract.getSupplier())));
        headerFields.setSupplier(supplier);

        // ExpirationTermType
        headerFields.setExpirationTermType(contract.getExpirationTermType());

        // MaxAutoRenewal
        if (contract.getMaxAutoRenewalAllowed() != null) {
            headerFields.setMaxAutoRenewalsAllowed(fact.createContractWorkspaceWSProjectImportMaxAutoRenewalsAllowed(BigInteger.valueOf(contract.getMaxAutoRenewalAllowed())));
        }

        // AutoRenewalInterval
        if (contract.getAutoRenewalInterval() != null) {
            headerFields.setAutoRenewalInterval(fact.createContractWorkspaceWSProjectImportAutoRenewalInterval(BigInteger.valueOf(contract.getAutoRenewalInterval())));
        }


        // ExpirationDate
        headerFields.setExpirationDate(fact.createContractWorkspaceWSProjectImportExpirationDate(dateToXMLGregorianCalendar(contract.getExpirationDate())));

        // RelatedId
        headerFields.setRelatedId(fact.createContractWorkspaceWSProjectImportRelatedId(contract.getRelatedId()));

        // Owner
      //  ContractWorkspaceWSProjectImport.Owner owner = fact.createContractWorkspaceWSProjectImportOwner();
      //  owner.setUniqueName(fact.createContractWorkspaceWSProjectImportOwnerUniqueName(contract.getOwner()));
      //  headerFields.setOwner(owner);

        // Commodity
        if (!Strings.isNullOrEmpty(contract.getCommodity())) {
            headerFields.setCommodity(new CommodityBuilder(contract.getCommodity()).build());
        }

        // ContractId
        headerFields.setContractId(fact.createContractWorkspaceWSProjectImportContractId(contract.getContractId()));

        // Region
        headerFields.setRegion(new RegionBuilder(contract.getRegion()).build());

        //HierarchicalType
        headerFields.setHierarchicalType(fact.createContractWorkspaceWSProjectImportHierarchicalType(contract.getHierarchicalType()));

        //AgreementDate
        if (contract.getAgreementDate() != null) {
            headerFields.setAgreementDate(fact.createContractWorkspaceWSProjectImportAgreementDate(dateToXMLGregorianCalendar(contract.getAgreementDate())));
        }

        // Amount
        if (contract.getAmount() != null && !Strings.isNullOrEmpty(contract.getCurrency())) {
            JAXBElement<String> currId = fact.createContractWorkspaceWSProjectImportAmountCurrencyUniqueName(contract.getCurrency());
            ContractWorkspaceWSProjectImport.Amount.Currency curr = fact.createContractWorkspaceWSProjectImportAmountCurrency();
            curr.setUniqueName(currId);
            ContractWorkspaceWSProjectImport.Amount amt = fact.createContractWorkspaceWSProjectImportAmount();
            amt.setAmount(fact.createContractWorkspaceWSProjectImportAmountAmount(contract.getAmount()));
            amt.setCurrency(curr);
            headerFields.setAmount(amt);
        }

       //  Affected Parties
        if (!Strings.isNullOrEmpty(contract.getAffectedParties())) {
            ContractWorkspaceWSProjectImport.AffectedParties affectedParties = fact.createContractWorkspaceWSProjectImportAffectedParties();
            ContractWorkspaceWSProjectImport.AffectedParties.Item affPartItem = fact.createContractWorkspaceWSProjectImportAffectedPartiesItem();
            affPartItem.setSystemID(fact.createContractWorkspaceWSProjectImportAffectedPartiesItemSystemID("test"));
            affectedParties.getItem().add(affPartItem);
            headerFields.setAffectedParties(affectedParties);
        }


      //   Department (Client)
      //  if (contract.getDepartment() != null) {
      //      headerFields.setClient(new ClientBuilder(contract.getDepartment()).build());
      //  }

        // Custom fields
        headerFields.setCustom(new CustomFieldsBuilder(contract).build());


        return request;
    }

    private XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) throws DatatypeConfigurationException {
        if (date != null) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        }
        return null;
    }
}
