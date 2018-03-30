package com.seal.contracts.generator.csv.validator;

import com.google.common.collect.ImmutableCollection;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.exception.ValidationExceptions;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by root on 20.08.15..
 */
public interface Validator<T> {
    Iterable<ValidationException> validate(T victim) throws IOException;
}
