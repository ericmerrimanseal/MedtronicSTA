package com.seal.contracts.generator.csv.processor;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jantonak on 27/01/17.
 */
@Service
public class DBDownloader {

    private static final File inFolder = new File("./in");

    @Value("${config.db.path}")
    private String dbPath;

    public OutputStream generate(OutputStream os) throws IOException {
        File tempDir = Files.createTempDirectory(String.format("sta_export_%s", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))).toFile();
        FileUtils.copyDirectoryToDirectory(inFolder, tempDir);
        FileUtils.copyDirectoryToDirectory(new File(dbPath), tempDir);
        ZipUtil.pack(tempDir, os);
        return os;

    }
}
