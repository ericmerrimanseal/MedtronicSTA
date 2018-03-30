package com.seal.contracts.generator.csv.mapper.meta;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by root on 18.08.15..
 */
public class ExportMeta implements FileMeta {

//    private final Set<String> headerFields = new LinkedHashSet<String>(Arrays.asList(new String[]{
//            "Seal internal ContractId",
//            "FilePath",
//            "FileName",
//            "ImportedAt",
//            "ASLASLNo",
//            "ASLContractStatus",
//            "ASLCreatedDate",
//            "ASLDescription",
//            "ASLDocumentDate",
//            "ASLDocumentID",
//            "ASLDocumentSubType",
//            "ASLDocumentType",
//            "ASLDocumentTypeID",
//            "ASLExclusivity",
//            "ASLExpiryMDate",
//            "ASLFilepath",
//            "ASLHierarchicalType",
//            "ASLLastModifiedDate",
//            "ASLLoadedBy",
//            "ASLModifiedBy",
//            "ASLResponsibleToDisplay",
//            "ASLSLBEntity",
//            "ASLSupplierEntity",
//            "ASLSupplierName"
//    }));

    private final Set<String> headerFields = new LinkedHashSet<String>(Arrays.asList(new String[]{

            "ContractId",
            "FilePath",
            "FileName",
            "InternalLanguage",
            "ImportedAt",
            "Priority",
            "MimeType",
            "ASLASLNo",
            "ASLContractStatus",
            "ASLCreatedDate",
            "ASLDescription",
            "ASLDocumentDate",
            "ASLDocumentID",
            "ASLDocumentSubType",
            "ASLDocumentType",
            "ASLDocumentTypeID",
            "ASLEvergreenContracts",
            "ASLExclusivity",
            "ASLExpiryMDate",
            "ASLExpiryTextOrig",
            "ASLFilepath",
            "ASLHierarchicalType",
            "ASLLastModifiedDate",
            "ASLLoadedBy",
            "ASLModifiedBy",
            "ASLParentAgmt",
            "ASLResponsibleToDisplay",
            "ASLSLBEntity",
            "ASLSupplierEntity",
            "ASLSupplierName",
            "ASLSupplierRegisteredOffice",
            "cusAutoRenewal",
            "cusContractSubTypeMulti",
            "cusExportStatus",
            "cusPaymentTerms",
            "cusPaymentTermsOther",
            "cusTermType"
    }));

    @Override
    public String getFileName() {
        return "export.csv";
    }

    @Override
    public Set<String> getHeader() {
        return headerFields;
    }
}
