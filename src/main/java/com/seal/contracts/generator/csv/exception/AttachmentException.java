package com.seal.contracts.generator.csv.exception;

import com.seal.contracts.generator.csv.bean.Export;

import java.io.File;

/**
 * Created by root on 12.08.15..
 */
public class AttachmentException extends ValidationException {

    private final File attachment;

    public AttachmentException(String description, File attachment, Export export) {
        super(description, Severity.ERROR, export);
        this.attachment = attachment;
    }
}
