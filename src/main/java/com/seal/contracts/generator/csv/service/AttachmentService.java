package com.seal.contracts.generator.csv.service;

import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.exception.AttachmentException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by root on 16.08.15..
 */
public interface AttachmentService {

    boolean attachmentExists(String sourcePath);

    InputStream getAttachment(Export export) throws AttachmentException;

    Optional<File> getSearchableAttachment(Export export) throws AttachmentException;

    Optional<String> getAribaFolder(Export export) throws AttachmentException;

    void addAttachment(Export export, File destinationFolder, Optional<String> renameTo) throws AttachmentException, IOException;
}
