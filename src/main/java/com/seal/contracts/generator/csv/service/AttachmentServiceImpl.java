package com.seal.contracts.generator.csv.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.exception.AttachmentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by root on 12.08.15..
 */
@Slf4j
public class AttachmentServiceImpl extends AbstractAttachmentService {

    private final File sourceFolder;
    private final File dupliactesFolder;

    public AttachmentServiceImpl(File sourceFolder, File dupliactesFolder) {
        this.sourceFolder = sourceFolder;
        this.dupliactesFolder = dupliactesFolder;
    }

    @Override
    public void addAttachment(Export export, File destinationFolder, Optional<String> renameTo) throws AttachmentException, IOException {
        InputStream source = getAttachment(export);

        String newName = renameTo.isPresent() ? renameTo.get() : "dummy-file";

        log.info("Attaching the attachment {}", newName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(destinationFolder, newName));

            IOUtils.copy(source, fos);
            source.close();

        } catch (IOException e) {
            throw new AttachmentException(e.getMessage(), new File(renameTo.get()), export);
        } finally {
            fos.close();
        }
    }

    public boolean attachmentExists(String sourcePath) {
        return new File(sourceFolder, sourcePath).exists();
    }

    @Override
    public InputStream getAttachment(Export export) throws AttachmentException {
        Preconditions.checkNotNull(export, "export must not be null");
        Preconditions.checkNotNull(export.getFileName(), "FileName must not be null");
//        File file = new File(sourceFolder, "attachment1.pdf");
        File file = new File(sourceFolder, export.getFileName());
        if (!file.exists()) {
            throw new AttachmentException(String.format("Attachment %s does not exist", file.getPath()), file, export);
        }
//        log.info("Including attachment {}", file.getName());
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new AttachmentException(e.getMessage(), file, export);
        }

    }

    @Override
    public Optional<File> getSearchableAttachment(Export export) throws AttachmentException {

//        String originalFileName = "attachment1.pdf";
        String originalFileName = export.getFileName();

        String searchableFileName = AbstractAttachmentService.buildAttachmentSearchableFileName(originalFileName);
        File searchableAttachment = Paths.get(dupliactesFolder.getPath(), searchableFileName).toFile();

        if (!searchableAttachment.exists()) {
            log.info("Searchable attachment {} does not exist", searchableAttachment.getName());
            return Optional.absent();
        }

//        log.info("Including seaarchable attachment {}", searchableAttachment.getName());
        return Optional.of(searchableAttachment);
    }

    @Override
    public Optional<String> getAribaFolder(Export export) throws AttachmentException {
        return Optional.absent();
    }
}
