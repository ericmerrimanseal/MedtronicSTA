package com.seal.contracts.generator.csv.exception;

import com.google.common.base.Joiner;
import com.seal.contracts.generator.csv.bean.Export;

/**
 * Created by root on 12.08.15..
 */
public class MappingException extends ValidationException {


    public enum FILE {CONTRACTS, DOCUMENTS, TEAMS, PARAMETERS}

    private final FILE fileType;

    public MappingException(String description, Severity severity, FILE fileType, Export export) {
        super(description, severity, export);
        this.fileType = fileType;
    }

    @Override
    public String toCSV() {
        return Joiner.on(",").join(super.toCSV(), fileType);
    }
}
