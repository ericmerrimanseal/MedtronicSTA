package com.seal.contracts.generator.csv.exception;

import com.seal.contracts.generator.csv.bean.Export;

/**
 * Created by root on 20.08.15..
 */
public class ContractMappingException extends MappingException {

    private static final FILE fileType = FILE.CONTRACTS;

    public ContractMappingException(String description, Severity severity, Export export) {
        super(description, severity, fileType, export);
    }
}
