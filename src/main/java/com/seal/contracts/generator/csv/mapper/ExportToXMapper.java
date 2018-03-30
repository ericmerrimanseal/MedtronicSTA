package com.seal.contracts.generator.csv.mapper;

import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.bean.Result;
import com.seal.contracts.generator.csv.exception.MappingException;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.seal.contracts.generator.csv.exception.ValidationExceptions;

import java.util.List;

/**
 * Created by root on 11.08.15..
 */
public interface ExportToXMapper<T> {
    T map(Export export) throws ValidationExceptions;
}
