package com.seal.contracts.generator.csv.exception;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by jantonak on 04/01/17.
 */
public class ParsingException extends Exception {
    private final List<ValidationException> exceptions;

    public ParsingException(String s, List<ValidationException> exceptions) {
        super(s);
        Preconditions.checkNotNull(exceptions);
        Preconditions.checkState(exceptions.isEmpty() == false);
        this.exceptions = exceptions;
    }

    public List<ValidationException> getExceptions() {
        return exceptions;
    }
}
