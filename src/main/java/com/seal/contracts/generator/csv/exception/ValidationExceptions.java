package com.seal.contracts.generator.csv.exception;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;

import static com.seal.contracts.generator.csv.exception.Severity.ERROR;

/**
 * Created by root on 20.08.15..
 */
public class ValidationExceptions extends Exception {

    private static final Predicate<ValidationException> HAS_ERROR = new Predicate<ValidationException>() {
        @Override
        public boolean apply(ValidationException e) {
            return e.getSeverity() == ERROR;
        }
    };

    private Collection<ValidationException> exceptions = Lists.newArrayList();

    public Iterable<ValidationException> getExceptions() {
        return Sets.newHashSet(exceptions);
    }

    public boolean hasError() {
        return Iterables.tryFind(exceptions, HAS_ERROR).isPresent();

    }

    public static ValidationExceptionsBuilder builder() {
        return new ValidationExceptionsBuilder();
    }

    public static ValidationExceptionsBuilder builder(ValidationExceptions copyFrom) {
        return new ValidationExceptionsBuilder(copyFrom);
    }

    public static class ValidationExceptionsBuilder {

        private ValidationExceptions validationExceptions;

        public ValidationExceptionsBuilder() {
            this.validationExceptions = new ValidationExceptions();
        }

        public ValidationExceptionsBuilder(ValidationExceptions copyFrom) {
            this();
            validationExceptions.exceptions.addAll(copyFrom.exceptions);
        }

        public ValidationExceptions build() {
            return validationExceptions;
        }

        public ValidationExceptionsBuilder addException(ValidationException exception) {
            this.validationExceptions.exceptions.add(exception);
            return this;
        }

        public ValidationExceptionsBuilder addExceptions(Iterable<ValidationException> exceptions) {
            Iterables.addAll(this.validationExceptions.exceptions, exceptions);
            return this;
        }
    }

    @Override
    public String getMessage() {

        FluentIterable<String> details = FluentIterable.from(exceptions).transform(new Function<ValidationException, String>() {
            @Override
            public String apply(ValidationException e) {
                return e.toString();
            }
        });

        return Joiner.on("\n").join(details);
    }
}
