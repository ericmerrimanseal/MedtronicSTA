package com.seal.contracts.generator.csv.mapper;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.Splitter;
import com.seal.contracts.generator.csv.bean.CommodityCode;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.bean.Supplier;
import com.seal.contracts.generator.csv.exception.ParsingException;
import com.seal.contracts.generator.csv.exception.Severity;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.service.*;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;

import java.io.IOException;
import java.util.*;

/**
 * Created by root on 11.08.15..
 */
public class ExportToContractMapper extends ValidatedExportToXMapper<Contract> {

    private final EnumerationService enumerationService;
    private final Map<String, Collection<Export>> allExports;
    private final ContractImportItemRepository itemRepository;
    private final FolderMappingService folderMappingService;
    private final ContractImportDocumentRepository documentRepository;

    private final static Splitter spliter = new Splitter();

    public ExportToContractMapper(UsersService usersService, SupplierService supplierService, Config config, EnumerationService enumerationService, CommodityCodeService commodityCodeService, ExportService exportService, ContractImportItemRepository itemRepository, FolderMappingService folderMappingService, ContractImportDocumentRepository documentRepository) throws IOException, ParsingException {
        super(usersService, supplierService, config, enumerationService, commodityCodeService, folderMappingService, documentRepository, itemRepository);
        this.enumerationService = enumerationService;
        this.itemRepository = itemRepository;
        this.folderMappingService = folderMappingService;
        this.documentRepository = documentRepository;


        List<ContractImportItem> masters = itemRepository.findByHierarchicalType(Contract.HIERARCHICAL_TYPE.MasterAgreement);
        Maps.uniqueIndex(masters, new Function<ContractImportItem, String>() {
            @Override
            public String apply(ContractImportItem item) {
                return item.getUniqueName();
            }
        });

        allExports = Collections.unmodifiableMap(exportService.recordsPerDocumentId());
    }

    @Override
    public Iterable<ValidationException> validate(final Export export) {
        // Validate Title
        List<ValidationException> exceptions = Lists.newArrayList(validateTitle(export));


        String parentAgreementId = export.getParentAgreement();
        if (parentAgreementId != null) {
            // Validate the parent agreement exists
            Optional<ContractImportItem> masterOptional = Optional.fromNullable(itemRepository.findOne(parentAgreementId));

            if (!masterOptional.isPresent()) {
                // Validate the Master exists
                exceptions.add(new ValidationException(String.format("Unknown ParentAgreement %s", parentAgreementId), Severity.ERROR, export));
            } else {
                ContractImportItem master = masterOptional.get();
                // Validate the Master is of hierarchical type MasterAgreement
                if (master.getHierarchicalType() != Contract.HIERARCHICAL_TYPE.MasterAgreement) {
                    exceptions.add(new ValidationException(String.format("Parent Agreement %s must be of hierarchical type ParentAgreement", parentAgreementId), Severity.ERROR, export));
                } else {
                    // Validate the Master passed the validation
                    if (!Strings.isNullOrEmpty(master.getErrors())) {
                        exceptions.add(new ValidationException(String.format("Parent Agreement %s failed the validation", parentAgreementId), Severity.ERROR, export));
                    }
                    // Validate the EffectiveDate if not before the Effective Date of the Parent Agreement (if used)
                    Date effectiveDate = export.getEffectiveDate();
                    Date parentEffectiveDate = master.getData().getEffectiveDate();
                    if (effectiveDate != null && parentEffectiveDate != null) {
                        if (effectiveDate.before(parentEffectiveDate)) {
                            exceptions.add(new ValidationException(String.format("EffectiveDate must not be before the EffectiveDate of the ParentAgreement %s", parentAgreementId), Severity.ERROR, export));
                        }
                    }
                }

            }
        }
        return exceptions;
    }


    @Override
    protected Contract mapInternal(final Export export) {
        final Contract contract = new Contract();

        contract.setTitle(removeUnsupportedCharacters(export.getTitle()));
        contract.setDescription(export.getDescription());
        contract.setOwner(usersService.userByFullName(export.getOwner(), false).orNull());

        if (!Strings.isNullOrEmpty(export.getCommodity())) {
            Iterable<String> codes = spliter.split(export.getCommodity());
            String joinedCodes = Joiner.on(",").join(Iterables.transform(codes, new Function<String, String>() {
                @Override
                public String apply(String s) {
                    CommodityCode commodityCode = commodityCodeService.lookup(s).or(CommodityCode.UNKNOWN);
                    return commodityCode.getKey();
                }
            }));
            contract.setCommodity(joinedCodes);
        }

        if (!Strings.isNullOrEmpty(export.getRegion())) {
            Iterable<String> regions = spliter.split(export.getRegion());
            String joinedRegions = Joiner.on("").join(Iterables.transform(regions, new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return String.format("[%s]", s);
                }
            }));
            contract.setRegion(enumerationService.byDescription(joinedRegions, EnumerationService.Type.Regions_Norm).orNull());
        }

      //  contract.setContractId(export.getLegacyContractId());
        contract.setHierarchicalType(export.getHierarchicalType());
        contract.setParentAgreement(export.getParentAgreement());
        try {
            String supplierId = supplierService.lookup(export.getSupplierId()).orElse(Supplier.UNKNOWN).getSystemId();
            contract.setSupplier(supplierId);
        } catch (IOException e) {
            contract.setSupplier(Supplier.UNKNOWN.getSystemId());
            contract.getErrors().add(new ValidationException(e.getMessage(), Severity.ERROR, export));
        }
        ;
        contract.setEffectiveDate(export.getEffectiveDate());
        contract.setExpirationDate(export.getExpirationDate());
        contract.setExpirationTermType(export.getExpirationTermType());
        contract.setAmount(export.getAmount());
//        contract.setProposedAmount(export.getProposedAmount());
//        contract.setCurrency(export.getCurrency());

        if (export.getAgreementDate() != null) {
            contract.setAgreementDate(export.getAgreementDate());
        }

        contract.setRelatedId(export.getLegacyContractId());

        contract.setAutoRenewalInterval(export.getAutoRenewalInterval());
        contract.setMaxAutoRenewalAllowed(export.getMaxAutoRenewalsAllowed());
        
        contract.setCus_VMOGroup(enumerationService.byDescription(export.getVmo(), EnumerationService.Type.VMO_Norm).orNull());
        contract.setCus_ContractType(enumerationService.byDescription(export.getContractType(), EnumerationService.Type.ContractType_Norm).orNull());
        contract.setCus_ContractCoverage(enumerationService.byDescription(export.getContractCoverage(), EnumerationService.Type.ContractCoverage_Norm).orNull());
        contract.setCus_Entity(enumerationService.byDescription(export.getEntity(), EnumerationService.Type.MedtronicEntity_Norm).orNull());
        contract.setDepartment(enumerationService.byDescription(export.getDepartment(), EnumerationService.Type.BusinessGroup_Norm).orNull());
        contract.setBaseLanguage(enumerationService.byDescription(export.getBaseLanguage(), EnumerationService.Type.BaseLanguage_Norm).orNull());

        return contract;
    }
}
