package com.seal.contracts.generator.csv.mapper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.bean.BuildResult;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.bean.Validatable;
import com.seal.contracts.generator.csv.exception.Severity;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.exception.ValidationExceptions;
import com.seal.contracts.generator.csv.service.*;
import com.seal.contracts.generator.csv.validator.ExportValidator;
import com.seal.contracts.generator.csv.validator.Validator;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by root on 20.08.15..
 */
public abstract class ValidatedExportToXMapper<T extends Validatable> implements ExportToXMapper {

    private static final String[] UNSUPPORTED_CHARS = new String[]{"\\", "/", ":", "*", "?", "\"", "<", ">", "|", "#", "+", "%", "&"};

    protected final UsersService usersService;
    protected final SupplierService supplierService;
    protected final Config config;
    protected final CommodityCodeService commodityCodeService;
    private final Validator validator;
    protected final ContractImportDocumentRepository documentRepository;
    protected final ContractImportItemRepository itemsRepository;
    protected final FolderMappingService folderMappingService;


    public ValidatedExportToXMapper(UsersService usersService, SupplierService supplierService, Config config, EnumerationService enumerationService, CommodityCodeService commodityCodeService, FolderMappingService folderMappingService, ContractImportDocumentRepository documentRepository, ContractImportItemRepository itemsRepository) {
        this.folderMappingService = folderMappingService;
        this.itemsRepository = itemsRepository;
        Preconditions.checkNotNull(usersService, "usersService must not be null");
        Preconditions.checkNotNull(config, "config must not be null");
        Preconditions.checkNotNull(supplierService, "supplierService must not be null");
        this.usersService = usersService;
        this.supplierService = supplierService;
        this.config = config;
        this.commodityCodeService = commodityCodeService;
        this.documentRepository = documentRepository;
        this.validator = new ExportValidator(usersService, supplierService, enumerationService, commodityCodeService, this.folderMappingService, this.documentRepository, this.itemsRepository);
    }

    @Override
    public T map(Export export) throws ValidationExceptions {
        throw new RuntimeException("not implemented");
    }

    public T map(Export export, BuildResult.BuildResultBuilder resultBuilder) {

        final ValidationExceptions.ValidationExceptionsBuilder exceptionsBuilder = ValidationExceptions.builder();

        Iterable<ValidationException> validatorExceptions = null;
        try {
            validatorExceptions = validator.validate(export);
        } catch (IOException e) {
            validatorExceptions = Lists.newArrayList(new ValidationException(e.getMessage(), Severity.ERROR, export));
        }

        exceptionsBuilder.addExceptions(validatorExceptions);

        exceptionsBuilder.addExceptions(validate(export));

//        if (exceptionsBuilder.build().hasError()) {
//            throw exceptionsBuilder.build();
//        }

        resultBuilder.addErrors(exceptionsBuilder.build().getExceptions(), export.getLegacyContractId());

        final T mapped = mapInternal(export);
        mapped.getErrors().addAll(Lists.newArrayList(resultBuilder.build().getErrors()));
        return mapped;
    }

    protected String mapCusLegacyContractIDASL(Export export) {
        return removeUnsupportedCharacters(export.getTitle());
    }

    public Iterable<ValidationException> validateTitle(final Export export) {
        Collection<ValidationException> exceptions = Lists.newArrayList();
        final String title = export.getTitle();
        if (title == null || Strings.isNullOrEmpty(title)) {
            exceptions.add(new ValidationException(String.format("Title must not be empty", title), Severity.ERROR, export));
        } else {
            if (title.length() > 254) {
                exceptions.add(new ValidationException(String.format("Title must not be longer than 254 characters (%s)", title), Severity.ERROR, export));
            }
            String unsupported = Joiner.on("").join(UNSUPPORTED_CHARS);
            if (title.matches(String.format(".*[%s].*", unsupported))) {
                exceptions.add(new ValidationException(String.format("Title must not contain any of '%s' (%s). -> will be replaced with blanks", unsupported, title), Severity.WARN, export));
            }
            if (title.trim().endsWith(".")) {
                exceptions.add(new ValidationException(String.format("Title must not end with '.' (%s)", title), Severity.ERROR, export));
            }
        }
        return ImmutableList.copyOf(exceptions);
    }

    protected abstract Iterable<ValidationException> validate(Export export);

    protected abstract T mapInternal(Export export);

    // replace unsupported characters with blank
    protected String removeUnsupportedCharacters(String withUnsupportedCharacters) {
        String text = withUnsupportedCharacters;
        if (!Strings.isNullOrEmpty(text)) {
            for (int i = 0; i < UNSUPPORTED_CHARS.length; i++) {
                String unsupportedChar = UNSUPPORTED_CHARS[i];
                text = Joiner.on("").join(Splitter.on(unsupportedChar).split(text));
            }
        }
        return text;
    }

}
