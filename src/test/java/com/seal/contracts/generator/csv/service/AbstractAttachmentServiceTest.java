package com.seal.contracts.generator.csv.service;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static junit.framework.Assert.assertEquals;

/**
 * Created by root on 26.08.15..
 */
public class AbstractAttachmentServiceTest {

    @Test
    public void testAttachmentSearchableFileName() {

        File file = Mockito.mock(File.class);
        Mockito.when(file.getName()).thenReturn("TEST.pdf");

        String searchable = AbstractAttachmentService.buildAttachmentSearchableFileName(file);
        assertEquals("TEST_s.pdf", searchable);
    }
}
