package com.seal.contracts.generator.csv.service;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.FolderMapping;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jantonak on 24/11/16.
 */
@Service
public class FolderMappingService {

  /*  private static final Logger LOGGER = Logger.getLogger(FolderMappingService.class.getName());

    private final File file = new File("C:/emergency/sta-master/src/main/resources/in/masterdata/foldermapping.csv");

    private static final Map<String, String> VALUUES = Maps.newHashMap();

    @PostConstruct
    private void init() throws IOException {
        CSVLoader<FolderMapping> loader = new CSVLoader(file, FolderMapping.class);
        List<FolderMapping> records = loader.load();
        for (FolderMapping record : records) {
            final String docType = record.getDocumentType().trim();
            if (VALUUES.containsKey(docType)) {
                LOGGER.log(Level.WARNING, String.format("%s exists multiple times", docType));
                continue;
            }

            String rawFolderName = record.getFolderName();
            String folderName;
            if (Strings.isNullOrEmpty(rawFolderName)) {
                LOGGER.log(Level.WARNING, String.format("DocumentType %s has no folder defined -> ignoring", docType));
                continue;
            } else {
                folderName = rawFolderName.trim();
                if (folderName.endsWith("/")) {
                    folderName = folderName.substring(0, folderName.length() - 2);
                }
            }

            VALUUES.put(docType, folderName);
        }
    }*/

    public Optional<String> findOne(final String docType) {
       // return Optional.fromNullable(VALUUES.get(docType));
    	return Optional.fromNullable("Contract Documents");
    }
}
