package com.seal.contracts.generator.csv.bean;

import com.seal.contracts.generator.csv.exception.ValidationException;

import java.util.List;

/**
 * Created by Juraj on 23.08.2017.
 */
public interface Validatable {

    List<ValidationException> getErrors();
}
