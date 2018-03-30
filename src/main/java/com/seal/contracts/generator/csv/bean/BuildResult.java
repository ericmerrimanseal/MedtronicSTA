package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.csv.exception.MappingException;
import com.seal.contracts.generator.csv.exception.Severity;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.exception.ZipException;
import com.seal.contracts.generator.persistence.entity.Config;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

/**
 * Created by root on 16.08.15..
 */
public class BuildResult extends Result {

    private final Config config;

    private Set<String> processed = Sets.newHashSet();
    private Set<String> withSuccess = Sets.newHashSet();
    private Set<String> withFailure = Sets.newHashSet();
    private Set<String> filteredOut = Sets.newHashSet();
    @Getter
    private Map<String, String> noSearchables = Maps.newHashMap();

    public BuildResult(Config config, STATUS status) {
        super(status);
        Preconditions.checkNotNull(config, "config must not be null");
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public Set<String> getProcessed() {
        return processed;
    }

    public Set<String> getWithSuccess() {
        return withSuccess;
    }

    public Set<String> getWithFailure() {
        return withFailure;
    }

    public Set<String> getFilteredOut() {
        return filteredOut;
    }

    public Set<String> getMissing() {
        return Sets.difference(processed, withSuccess);
    }

    public static BuildResultBuilder newBuilder(Config config, STATUS status) {
        return new BuildResultBuilder(config, status);
    }

    public static class BuildResultBuilder {
        private final BuildResult result;

        private int hasError;
        private int hasWarning;

        public BuildResultBuilder(Config config, STATUS status) {
            this.result = new BuildResult(config, status);
        }

        public BuildResult build() {
            result.status = resolveStatus();
            return result;
        }

        public BuildResultBuilder addError(ZipException exception) {
            result.getErrors().add(exception);
            return this;
        }

        public BuildResultBuilder addError(MappingException exception) {
            result.getErrors().add(exception);
            return this;
        }

        public BuildResultBuilder addError(ValidationException exception, String contractId) {
            result.getErrors().add(exception);
            if (result.errorsByContract.containsKey(contractId)) {
                result.errorsByContract.get(contractId).add(exception);
            } else {
                result.errorsByContract.put(contractId, Sets.newHashSet(exception));
            }
            return this;
        }

        public BuildResultBuilder addErrors(Iterable<ValidationException> exceptions, String contractId) {
            Iterables.addAll(result.getErrors(), exceptions);
            if (result.errorsByContract.containsKey(contractId)) {
                Iterables.addAll(result.errorsByContract.get(contractId), exceptions);
            } else {
                result.errorsByContract.put(contractId, Sets.newHashSet(exceptions));
            }
            return this;
        }

        public BuildResultBuilder addProcessed(String integer) {
            result.processed.add(integer);
            return this;
        }

        public BuildResultBuilder addSuccess(String integer) {
            result.withSuccess.add(integer);
            return this;
        }

        public BuildResultBuilder addFailure(String integer) {
            result.withFailure.add(integer);
            return this;
        }

        public BuildResultBuilder addFilteredOut(String integer) {
            result.filteredOut.add(integer);
            return this;
        }

        public BuildResultBuilder addNoSearchable(Export export) {
            String documentId = export.getLegacyContractId();
            String originalFileName = export.getFileName();
            result.noSearchables.put(documentId, originalFileName);
            return this;
        }

        public boolean isFilteredOut(String id) {
            return result.filteredOut.contains(id);
        }

        private STATUS resolveStatus() {
            boolean hasErrors = FluentIterable.from(result.getErrors()).anyMatch(new Predicate<ValidationException>() {
                @Override
                public boolean apply(ValidationException e) {
                    return e.getSeverity() == Severity.ERROR;
                }
            });

            if (hasErrors) {
                return STATUS.FAILURE;
            }

            boolean hasWarnings = FluentIterable.from(result.getErrors()).anyMatch(new Predicate<ValidationException>() {
                @Override
                public boolean apply(ValidationException e) {
                    return e.getSeverity() == Severity.WARN;
                }
            });

            if (hasWarnings) {
                return STATUS.WARNINGS;
            }

            return STATUS.SUCCESS;
        }

    }
}
