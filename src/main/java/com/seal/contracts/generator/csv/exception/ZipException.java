package com.seal.contracts.generator.csv.exception;

import com.seal.contracts.generator.csv.bean.Export;

/**
 * Created by root on 17.08.15..
 */
public class ZipException extends ValidationException {
    public ZipException(String description, Severity severity, Export export) {
        super(description, severity, export);
    }
}
