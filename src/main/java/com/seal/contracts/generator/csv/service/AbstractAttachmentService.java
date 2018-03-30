package com.seal.contracts.generator.csv.service;

import com.google.common.io.Files;

import java.io.File;

/**
 * Created by root on 26.08.15..
 */
public abstract class AbstractAttachmentService implements AttachmentService {

    // FileName_S.extension
    private static final String SEARCHABLE_ATTACHMENT_FORMAT = "%s_s.%s";

    public static String buildAttachmentSearchableFileName(File file) {
        return buildAttachmentSearchableFileName(file.getName());
    }

    public static String buildAttachmentSearchableFileName(String fileName) {
        return String.format(SEARCHABLE_ATTACHMENT_FORMAT, Files.getNameWithoutExtension(fileName), Files.getFileExtension(fileName));
    }

}
