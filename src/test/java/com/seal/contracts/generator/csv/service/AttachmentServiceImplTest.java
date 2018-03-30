package com.seal.contracts.generator.csv.service;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.exception.AttachmentException;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by root on 26.08.15..
 */
public class AttachmentServiceImplTest {

    @Test
    public void testAttachmentSearchableFileName() throws AttachmentException, IOException {

        File tempDir1 = Files.createTempDir();
        File originalsFolder = Paths.get(tempDir1.getPath(), "subFolder1", "subFolder2").toFile();

        File tempDir2 = Files.createTempDir();
        File searchablesFolder = Paths.get(tempDir2.getPath(), "subFolder1_s", "subFolder2_s").toFile();

        File originalFile = Paths.get(originalsFolder.getPath(), "file.pdf").toFile();
        Files.createParentDirs(originalFile);
        originalFile.createNewFile();
        originalFile.deleteOnExit();

        File searchableFile = Paths.get(searchablesFolder.getPath(), "file_s.pdf").toFile();
        Files.createParentDirs(searchableFile);
        searchableFile.createNewFile();
        searchableFile.deleteOnExit();

        AttachmentService attachmentService = new AttachmentServiceImpl(originalsFolder, searchablesFolder);

        Export export = Mockito.mock(Export.class);
        Mockito.when(export.getFileName()).thenReturn("file.pdf");

//        File attachment = attachmentService.getAttachment(export);
//        assertEquals(originalFile, attachment);

        Optional<File> searchable = attachmentService.getSearchableAttachment(export);
        assertTrue(searchable.isPresent());
        assertEquals(searchableFile, searchable.get());
    }
}
