package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.csv.exception.Severity;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.ui.bean.UIError;

import java.util.*;

/**
 * Created by root on 17.08.15..
 */
public class Result {
    protected STATUS status;

    private Collection<ValidationException> errors = Sets.newHashSet();

    Map<String, Set<ValidationException>> errorsByContract = Maps.newHashMap();

    public enum STATUS {IN_PROGRESS, SUCCESS, WARNINGS, FAILURE}

    public Result(STATUS status) {

        Preconditions.checkNotNull(status, "status must not be null");
        this.status = status;
    }

    public Collection<ValidationException> getErrors() {
        return errors;
    }

    public STATUS getStatus() {
        return status;
    }

    public Map<String, Set<ValidationException>> getErrorsByContract() {
        return errorsByContract;
    }

    public Set<ValidationException> getErrorsByContract(String contractId) {
        if (errorsByContract.containsKey(contractId)) {
            Set<ValidationException> exceptions = errorsByContract.get(contractId);
            return FluentIterable.from(exceptions).filter(new Predicate<ValidationException>() {
                @Override
                public boolean apply(ValidationException e) {
                    return e.getSeverity() == Severity.ERROR;
                }
            }).toSet();
        } else {
            return Sets.newHashSet();
        }
    }

    public boolean contractHasErrors(String contractId) {
        return !getErrorsByContract(contractId).isEmpty();
    }


    public List<UIError> getErrorsForAllContracts() {
        List<UIError> returnValue = Lists.newArrayList();
        if (errorsByContract.isEmpty()) {
            return returnValue;
        } else {
            for (Map.Entry<String, Set<ValidationException>> entry : errorsByContract.entrySet()) {
                for (ValidationException e : entry.getValue()) {
                    returnValue.add(new UIError(entry.getKey(), e.getMessage(), e.getSeverity(), e.getId()));
                }
            }
        }
        Collections.sort(returnValue, new Comparator<UIError>() {
            @Override
            public int compare(UIError e1, UIError e2) {
                if (e1.getContractId() == null && e2.getContractId() == null) {
                    return 0;
                }
                return e1.getContractId().compareTo(e2.getContractId());
            }
        });
        return returnValue;
    }


    public static ResultBuilder newBuilder(STATUS status) {
        return new ResultBuilder(status);
    }

    public static class ResultBuilder {
        private final Result result;

        public ResultBuilder(STATUS status) {
            this.result = new Result(status);
        }

        public Result build() {
            return result;
        }

        public ResultBuilder addError(String contractId, ValidationException exception) {
            result.errors.add(exception);
            result.status = STATUS.FAILURE;
            if (result.errorsByContract.containsKey(contractId)) {
                result.errorsByContract.get(contractId).add(exception);
            } else {
                result.errorsByContract.put(contractId, Sets.newHashSet(exception));
            }
            return this;
        }
    }

    public String logErrorsByContract(String contractId) {
        return logErrors(getErrorsByContract(contractId));
    }

    public static String logErrors(Set<ValidationException> errors) {
        return FluentIterable.from(errors).join(Joiner.on("\n"));
    }


}
