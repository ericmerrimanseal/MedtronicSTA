package com.seal.contracts.generator.csv.processor;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.bean.BuildResult;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.exception.Severity;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.mapper.ExportToContractMapper;
import com.seal.contracts.generator.csv.service.ExportsGroupValidator;
import com.seal.contracts.generator.csv.service.FolderMappingService;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

import static com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus.VALIDATION_FAILED;

/**
 * Created by root on 31.08.15..
 */
@Slf4j
public class ExportProcessor {

    private final ExportToContractMapper contractMapper;
    private final String uniqueName;
    private final ContractImportItemRepository itemsRepository;
    private final ContractImportDocumentRepository documentsRepository;
    private final BuildResult.BuildResultBuilder resultBuilder;
    private final boolean isGenerate;
    private final FolderMappingService folderMappingService;

    public ExportProcessor(ExportToContractMapper contractMapper, String uniqueName, ContractImportItemRepository itemsRepository, ContractImportDocumentRepository documentsRepository, BuildResult.BuildResultBuilder resultBuilder, boolean isGenerate, FolderMappingService folderMappingService) {
        this.contractMapper = contractMapper;
        this.folderMappingService = folderMappingService;
        this.documentsRepository = documentsRepository;
        this.uniqueName = uniqueName;
        this.itemsRepository = itemsRepository;
        this.resultBuilder = resultBuilder;
        this.isGenerate = isGenerate;
    }

    public ContractImportItem process(Collection<Export> exports) {

        final List<Contract> contracts = Lists.newArrayList();
        final List<Attachment> attachments = Lists.newArrayList();

        ExportsGroupValidator groupValidator = new ExportsGroupValidator(uniqueName, exports);
        Iterable<ValidationException> groupExceptions = groupValidator.validate();
        for (ValidationException validationException : groupExceptions) {
            resultBuilder.addError(validationException, uniqueName);
            log.error(validationException.toString());
        }
//        ExportGroupsFilter filter = new ExportGroupsFilter(uniqueName, exports);
//        Collection<Export> eligibleExports = filter.filter();

        for (Export export : exports) {

            log.info("Mapping masterdata to extranal structure...");
            Contract contract = mapContract(export, resultBuilder);
            log.info("Mapping masterdata to extranal structure... SUCCESS");

            Optional<String> aribaFolder = folderMappingService.findOne(export.getDocumentType());
            if (!aribaFolder.isPresent()) {
                String message = String.format("Unable to find destination folder for document type %s", export.getDocumentType());
                contract.getErrors().add(new ValidationException(message, Severity.ERROR, export));
                log.error(message);
            }
            attachments.add(new Attachment(export.getFileName(), aribaFolder, export.getContractId(), !export.getReviewType().isSkip()));
            contracts.add(contract);

        }
        if (isGenerate && !contracts.isEmpty()) {
            Contract header = contracts.get(0);
            for (Contract contract : contracts) {
                if (!header.equals(contract)) {
                    header.getErrors().addAll(contract.getErrors());
                }
            }
            return persist(header, attachments);
        }
        return null;
    }

    private ContractImportItem persist(Contract header, List<Attachment> attachments) {
        ContractImportItem item = itemsRepository.findOne(uniqueName);

        ImmutableSet<String> errors = FluentIterable.from(header.getErrors()).filter(new Predicate<ValidationException>() {
            @Override
            public boolean apply(ValidationException e) {
                boolean isError = e.getSeverity() == Severity.ERROR;
                boolean isHeaderLevel = (!e.getId().isPresent() || e.getId().get().equals(uniqueName));
                return isError && isHeaderLevel;
            }
        }).transform(new Function<ValidationException, String>() {
            @Override
            public String apply(ValidationException e) {
                return e.getMessage();
            }
        }).toSet();

        if (item == null) {
            item = new ContractImportItem(uniqueName, 1L, header, Optional.<String>absent(), errors);
        } else {
            item.update(header, errors);
        }
        List<ContractImportDocument> docsToSave = Lists.newArrayList();
        for (Attachment att : attachments) {
            String hash = att.getHash();

            final ImmutableSet<String> errorsByHash = FluentIterable.from(header.getErrors()).filter(new Predicate<ValidationException>() {
                @Override
                public boolean apply(ValidationException e) {
                    return e.getSeverity() == Severity.ERROR && hash.equals(e.getId().orNull());
                }
            }).transform(new Function<ValidationException, String>() {
                @Override
                public String apply(ValidationException e) {
                    return e.getMessage();
                }
            }).toSet();

            String docId = Joiner.on("_").join(uniqueName, att.hash);
            Optional<ContractImportDocument> docOptional = Optional.fromNullable(documentsRepository.findOne(docId));
            if (!docOptional.isPresent()) {
                ContractImportDocument doc = new ContractImportDocument(docId, uniqueName, att.getName(), hash, errorsByHash, att.isActive());
                doc.setAribaFolder(att.getAribaFolder().orNull());
                docsToSave.add(doc);
            } else {
                ContractImportDocument doc = docOptional.get();
                doc.update(att.getAribaFolder(), errorsByHash, att.isActive());
                docsToSave.add(doc);
            }
        }

        boolean hasFailedDocs = FluentIterable.from(docsToSave).anyMatch(new Predicate<ContractImportDocument>() {
            @Override
            public boolean apply(ContractImportDocument doc) {
                return doc.getStatus() == ContractImportDocumentStatus.VALIDATION_FAILED;
            }
        });
        if (hasFailedDocs) {
            item.setStatus(VALIDATION_FAILED);
        }
        itemsRepository.save(item);
        documentsRepository.save(docsToSave);

        return item;
    }

    private Contract mapContract(Export export, BuildResult.BuildResultBuilder resultBuilder) {
        Contract contract = contractMapper.map(export, resultBuilder);
        return contract;
    }

    private class Attachment {

        @Getter
        private final String name;
        @Getter
        private final Optional<String> aribaFolder;
        @Getter
        private final String hash;
        @Getter
        private final boolean active;


        public Attachment(String name, Optional<String> aribaFolder, String hash, boolean active) {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(aribaFolder);
            this.name = name;
            this.aribaFolder = aribaFolder;
            this.hash = hash;
            this.active = active;

        }

//        private String normalizeName(String name) {
//
//            StringBuilder fn = new StringBuilder();
//            if (Files.getNameWithoutExtension(file.getName()).endsWith("_s")) {
//                fn.append(Files.getNameWithoutExtension(name) + "_s." + Files.getFileExtension(name));
//            } else {
//                fn.append(name);
//            }
//
//            return fn.toString();
//        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Attachment that = (Attachment) o;

            return name.equals(that.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}