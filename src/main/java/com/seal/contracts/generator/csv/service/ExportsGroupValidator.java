package com.seal.contracts.generator.csv.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.enums.seal.ReviewType;
import com.seal.contracts.generator.csv.exception.ValidationException;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static com.seal.contracts.generator.csv.enums.seal.ReviewType.PRIMARY_DOCUMENT;
import static com.seal.contracts.generator.csv.enums.seal.ReviewType.REVIEWTYPE_SKIP;
import static com.seal.contracts.generator.csv.exception.Severity.ERROR;
import static com.seal.contracts.generator.csv.exception.Severity.WARN;

/**
 * Created by Juraj on 22.08.2017.
 */
public class ExportsGroupValidator {

    private final String contractId;
    private final Collection<Export> records;

    public ExportsGroupValidator(String contractId, Collection<Export> records) {
        this.contractId = contractId;
        this.records = records;
    }


    public Iterable<ValidationException> validate() {

        List<ValidationException> exceptions = Lists.newArrayList();

        Multimap<ReviewType, Export> reviewTypeMap = groupByReviewType();
        if (!reviewTypeMap.containsKey(PRIMARY_DOCUMENT)) {
            exceptions.add(new ValidationException("At least one record must be flagged as Primary", ERROR, Optional.of(contractId)));
        } else {
            if (reviewTypeMap.get(PRIMARY_DOCUMENT).size() > 1) {
                exceptions.add(new ValidationException("Only one record must be flagged as Primary", ERROR, Optional.of(contractId)));
            }
        }

        if (!reviewTypeMap.containsKey(PRIMARY_DOCUMENT)) {
            exceptions.add(new ValidationException("At least one record must be flagged as Primary", ERROR, Optional.of(contractId)));
        }

        if (reviewTypeMap.size() == 1 && reviewTypeMap.containsKey(REVIEWTYPE_SKIP)) {
            exceptions.add(new ValidationException(String.format("All records are flagged as '%s'", REVIEWTYPE_SKIP.getLabel()), ERROR, Optional.of(contractId)));
        }


        for (Export export : reviewTypeMap.get(ReviewType.UNKNOWN)) {
            exceptions.add(new ValidationException(String.format("Review type not defined"), ERROR, export));
        }

        exceptions.addAll(validateDuplicateFileName());

        return exceptions;
    }

    private Multimap<ReviewType, Export> groupByReviewType() {
        Multimap<ReviewType, Export> index = Multimaps.index(records, new Function<Export, ReviewType>() {
            @Override
            public ReviewType apply(Export export) {
                return export.getReviewType();
            }
        });
        return index;
    }

    private List<ValidationException> validateDuplicateFileName() {
        List<ValidationException> exceptions = Lists.newArrayList();

        ImmutableListMultimap<String, Export> byName = Multimaps.index(records, new Function<Export, String>() {
            @Override
            public String apply(Export export) {
                return export.getFileName();
            }
        });

        FluentIterable.from(byName.asMap().values()).forEach(new Consumer<Collection<Export>>() {
            @Override
            public void accept(Collection<Export> exports) {
                if (exports.size() > 1) {
                    for (Export export : exports) {
                        exceptions.add(new ValidationException(String.format("There are %s documents with same file name", exports.size()), ERROR, export));
                    }
                }
            }
        });

        return exceptions;
    }
}
