package com.seal.contracts.generator.csv.validator;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.bean.AribaReference;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.bean.Supplier;
import com.seal.contracts.generator.csv.enums.ExpirationTermType;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.service.*;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.seal.contracts.generator.csv.bean.Contract.HIERARCHICAL_TYPE.*;
import static com.seal.contracts.generator.csv.enums.ExpirationTermType.*;
import static com.seal.contracts.generator.csv.exception.Severity.ERROR;

/**
 * Created by root on 20.08.15..
 */
@Slf4j
public class ExportValidator implements Validator<Export> {

    private static final String SYSTEM_NEW_LINE = System.getProperty("line.separator");
    private static final String LF = "\n";

    private final UsersService userService;
    private final SupplierService supplierService;
    private final EnumerationService enumsService;
    private final CommodityCodeService commodityCodeService;
    private final com.seal.contracts.generator.csv.Splitter splitter = new com.seal.contracts.generator.csv.Splitter();
    private final FolderMappingService folderMappingService;
    private final ContractImportDocumentRepository documentRepository;
    private final ContractImportItemRepository itemsRepository;

    public ExportValidator(UsersService userService, SupplierService supplierService, EnumerationService enumsService, CommodityCodeService commodityCodeService, FolderMappingService folderMappingService, ContractImportDocumentRepository documentRepository, ContractImportItemRepository itemsRepository) {
        this.userService = userService;
        this.supplierService = supplierService;
        this.enumsService = enumsService;
        this.commodityCodeService = commodityCodeService;
        this.folderMappingService = folderMappingService;
        this.documentRepository = documentRepository;
        this.itemsRepository = itemsRepository;
    }

    @Override
    public Iterable<ValidationException> validate(Export export) throws IOException {
        List<ValidationException> exceptions = Lists.newArrayList();

        // InternalId
        String internalId = export.getContractId();
        if (Strings.isNullOrEmpty(internalId)) {
            exceptions.add(new ValidationException("InternalId must not be empty.", ERROR, export));
        }

        //ReviewType==SKIP AND Already in Ariba
        if (export.getReviewType().isSkip()) {
            ContractImportDocument doc = documentRepository.findOne(export.getContractId());
            if (doc != null && doc.hasAribaId()) {
                exceptions.add(new ValidationException("Wrong Review Type as Document already exists in Ariba", ERROR, export));
            }
        }

        if (export.getTargetReference().existsInTarget()) {
            ContractImportDocument doc = documentRepository.findOne(export.getContractId());
            ContractImportItem header = itemsRepository.findOne(export.getLegacyContractId());
            if (doc != null) {
                Optional<String> docIdOptional = Optional.fromNullable(((AribaReference) export.getTargetReference()).getDocumentId());
                if (docIdOptional.isPresent()) {
                    if (!docIdOptional.get().equals(doc.getAribaId())) {
                        exceptions.add(new ValidationException(String.format("AribaDocumentID %s does not match the record in Ariba (%s)", docIdOptional.get(), doc.getAribaId()), ERROR, export));
                    }
                }
            }
            if (header != null) {
                Optional<String> headerIdOptional = Optional.fromNullable(((AribaReference) export.getTargetReference()).getContractId());
                if (headerIdOptional.isPresent()) {
                    if (!headerIdOptional.get().equals(header.getAribaId())) {
                        exceptions.add(new ValidationException(String.format("AribaContractID %s does not match the record in Ariba (%s)", headerIdOptional.get(), header.getAribaId()), ERROR, export));
                    }
                }
            }
        }

        //CommodityCode
        if (!Strings.isNullOrEmpty(export.getCommodity())) {
            String value = export.getCommodity();
            Iterable<String> splits = splitter.split(value);
            for (String split : splits) {
                if (!commodityCodeService.lookup(split).isPresent()) {
                    exceptions.add(new ValidationException(String.format("Commodity %s does not exist", split), ERROR, export));
                }
            }
        }
        
        if (!Strings.isNullOrEmpty(export.getBaseLanguage())) {
            Optional<String> baseLangOptional = enumsService.byDescription(export.getBaseLanguage(), EnumerationService.Type.BaseLanguage_Norm);
            if (!baseLangOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("Base Language %s does not exist", export.getBaseLanguage()), ERROR, export));
            }
        }
        
        if (!Strings.isNullOrEmpty(export.getVmo())) {
            Optional<String> vmoOptional = enumsService.byDescription(export.getVmo(), EnumerationService.Type.VMO_Norm);
            if (!vmoOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("VMO Group %s does not exist", export.getVmo()), ERROR, export));
            }
        }
        
        
        if (!Strings.isNullOrEmpty(export.getContractCoverage())) {
            Optional<String> coverageOptional = enumsService.byDescription(export.getContractCoverage(), EnumerationService.Type.ContractCoverage_Norm);
            if (!coverageOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("Contract Coverage %s does not exist", export.getContractCoverage()), ERROR, export));
            }
        }
        
        if (!Strings.isNullOrEmpty(export.getDepartment())) {
            Optional<String> deptOptional = enumsService.byDescription(export.getDepartment(), EnumerationService.Type.BusinessGroup_Norm);
            if (!deptOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("Department %s does not exist", export.getDepartment()), ERROR, export));
            }
        }
        
        if (!Strings.isNullOrEmpty(export.getContractType())) {
            Optional<String> conTypeOptional = enumsService.byDescription(export.getContractType(), EnumerationService.Type.ContractType_Norm);
            if (!conTypeOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("Contract Type %s does not exist", export.getContractType()), ERROR, export));
            }
        }
        
        if (!Strings.isNullOrEmpty(export.getEntity())) {
            Optional<String> entityOptional = enumsService.byDescription(export.getEntity(), EnumerationService.Type.MedtronicEntity_Norm);
            if (!entityOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("Entity %s does not exist", export.getEntity()), ERROR, export));
            }
        }

         if (Strings.isNullOrEmpty(export.getRegion())) {            
                    exceptions.add(new ValidationException(String.format("Region must not be empty", export.getRegion()), ERROR, export));
         	}
         
        
        //Title
        if (Strings.isNullOrEmpty(export.getTitle())) {
            exceptions.add(new ValidationException("Title must not be null", ERROR, export));
        } else {
            if (export.getTitle().contains(SYSTEM_NEW_LINE) || export.getTitle().contains(LF)) {
                exceptions.add(new ValidationException("Title must not contain '\\n'", ERROR, export));
            }
        }   
   
        // LegacyContractId
        String aslDocumentId = export.getLegacyContractId();
        if (Strings.isNullOrEmpty(aslDocumentId)) {
            exceptions.add(new ValidationException("LegacyContractId must not be empty.", ERROR, export));
        }

        // ExpirationTermType
        String ett = export.getExpirationTermType();
        if (Strings.isNullOrEmpty(ett)) {
            exceptions.add(new ValidationException("Expiration Term Type must not be empty.", ERROR, export));
        } else {

            Optional<ExpirationTermType> expirationTermTypeOptional = ExpirationTermType.bySealDescription(ett);
            if (!expirationTermTypeOptional.isPresent()) {
                exceptions.add(new ValidationException(String.format("Expiration Term Type %s is unknown. Valid ones are %s,%s", ett, FIXED.name(), PERPETUAL.name(), AUTORENEW.name()), ERROR, export));
            } else {
                // ExpirationDate
                ExpirationTermType value = expirationTermTypeOptional.get();
                if (value == FIXED || value == AUTORENEW) {
                    if (export.getExpirationDate() == null) {
                        exceptions.add(new ValidationException(String.format("ExpirationDate must not be empty for %s", ett, value.name()), ERROR, export));
                    }

                }
            }
        }

        // Supplier Name
        String supplierId = export.getSupplierId();
        if (Strings.isNullOrEmpty(supplierId) || supplierId.equals(Supplier.EMPTY_SUPPLIER)) {
            exceptions.add(new ValidationException("SupplierId must not be empty.", ERROR, export));
        } else {
            java.util.Optional<Supplier> supplierByErpId = supplierService.lookup(String.valueOf(supplierId));
            if (!supplierByErpId.isPresent()) {
                exceptions.add(new ValidationException(String.format("Supplier %s either does not exist or is not approved", supplierId), ERROR, export));
            } else if (Strings.isNullOrEmpty(supplierByErpId.get().getSystemId())) {
                exceptions.add(new ValidationException(String.format("Unknown SystemID associated to supplier %s", supplierId), ERROR, export));
            }
        }

        // HierarchicalType
        Contract.HIERARCHICAL_TYPE hierarchicalType;
        String rawHierarchicalType = export.getHierarchicalType();
        if (Strings.isNullOrEmpty(rawHierarchicalType)) {
            exceptions.add(new ValidationException("HierarchicalType must not be empty", ERROR, export));
        } else {
            try {
                hierarchicalType = Contract.HIERARCHICAL_TYPE.valueOf(export.getHierarchicalType());
                // Validate the MaterAgreement has no ParentAgreement set
                String parentAgmt = export.getParentAgreement();
                if (hierarchicalType == MasterAgreement && !Strings.isNullOrEmpty(parentAgmt)) {
                    exceptions.add(new ValidationException("ParentAgreement must be empty for MasterAgreement(s)", ERROR, export));
                }
                if (hierarchicalType == SubAgreement && Strings.isNullOrEmpty(parentAgmt)) {
                    exceptions.add(new ValidationException("ParentAgreement must not be empty for SubAgreement(s)", ERROR, export));
                }
            } catch (IllegalArgumentException e) {
                exceptions.add(new ValidationException(String.format("HierarchicalType %s is unknown. Valid ones are %s,%s,%s", export.getHierarchicalType(), MasterAgreement, SubAgreement, StandAlone), ERROR, export));
            }

            // TODO Validate the StandAlone one is not referenced as ParentAgreement
        }

        // Effective Date
        if (export.getEffectiveDate() == null) {
            exceptions.add(new ValidationException("Effective Date must not be empty", ERROR, export));
        }

        // Expiration Date
        if (export.getExpirationDate() != null) {
            Date expDate = export.getExpirationDate();
            Date effectiveDate = export.getEffectiveDate();
            if (effectiveDate != null) {
                if (expDate.before(effectiveDate)) {
                    exceptions.add(new ValidationException("Expiration Date must not be before the Effective Date", ERROR, export));
                }
            }
        }

        // FolderName
        if (Strings.isNullOrEmpty(export.getDocumentType())) {
            exceptions.add(new ValidationException("Document Type must not be empty", ERROR, export));
        } else {
            Optional<String> folderName = folderMappingService.findOne(export.getDocumentType());
            if (!folderName.isPresent()) {
                exceptions.add(new ValidationException(String.format("No mapping defined for document type %s", export.getDocumentType()), ERROR, export));
            }
        }

        // ReviewComplete
        if (!export.isReviewComplete()) {
            exceptions.add(new ValidationException("ReviewComplete must be set to Yes", ERROR, export));
        }

        // Amount
        if (export.getAmount() != null) {
            String currency = export.getCurrency();
            if (currency == null) {
                exceptions.add(new ValidationException("Currency must be set", ERROR, export));
            } else {
                int numOfCurrencies = Iterables.size(splitter.split(currency));
                if (numOfCurrencies > 1) {
                    exceptions.add(new ValidationException("Only one currency is allowed", ERROR, export));
                }
            }
        }


        return ImmutableList.copyOf(exceptions);
    }
}
