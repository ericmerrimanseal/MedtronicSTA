package com.seal.contracts.generator.csv.mapper;

import com.google.common.collect.ImmutableList;
import com.seal.contracts.generator.csv.bean.BuildResult;
import com.seal.contracts.generator.csv.bean.ContractDocument;
import com.seal.contracts.generator.csv.bean.ContractDocuments;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.exception.ValidationExceptions;
import com.seal.contracts.generator.csv.service.*;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;

import java.io.IOException;

import static com.seal.contracts.generator.csv.bean.Contract.STATUS.Draft;


/**
 * Created by root on 11.08.15..
 */
public class ExportToContractDocumentMapper extends ValidatedExportToXMapper<ContractDocuments> {

    private enum TYPE {ORIGINAL, SEARCHABLE}

    public ExportToContractDocumentMapper(UsersService usersService, SupplierService supplierService, Config config, EnumerationService enumerationService, CommodityCodeService commodityCodeService, FolderMappingService folderMappingService, ContractImportDocumentRepository documentRepository, ContractImportItemRepository itemsRepository) {
        super(usersService, supplierService, config, enumerationService, commodityCodeService, folderMappingService, documentRepository, itemsRepository);
    }

    public ContractDocuments map(Export export, BuildResult.BuildResultBuilder resultBuilder, boolean originalExists, boolean searchableExists) throws ValidationExceptions, IOException {
        super.map(export, resultBuilder);
        ContractDocuments.ContractDocumentsBuilder builder = ContractDocuments.newBuilder();
        if (originalExists) {
            ContractDocument original = mapContractDocument(export, TYPE.ORIGINAL);
            builder.addDocument(original);
        }
        if (searchableExists) {
            ContractDocument searchable = mapContractDocument(export, TYPE.SEARCHABLE);
            builder.addDocument(searchable);
        }

        return builder.build();
    }

    @Override
    protected Iterable<ValidationException> validate(Export export) {
        return ImmutableList.of();
    }

    @Override
    @Deprecated
    protected ContractDocuments mapInternal(Export export) {
        return ContractDocuments.newBuilder().build();
    }

    private ContractDocument mapContractDocument(Export export, TYPE type) {
        ContractDocument contractDocument = new ContractDocument();

        String workspace = mapCusLegacyContractIDASL(export);
        contractDocument.setWorkspace(workspace);

        String fileName = type == TYPE.ORIGINAL ? export.getFileName() : AbstractAttachmentService.buildAttachmentSearchableFileName(export.getFileName());
        contractDocument.setFile(fileName);
        contractDocument.setTitle(fileName);

        contractDocument.setStatus(Draft.name());

        contractDocument.setOwner(export.getOwner());

        contractDocument.setFolder(null);

        return contractDocument;
    }

}
