package com.seal.contracts.generator.csv.processor;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.CSVWriter;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.univocity.parsers.annotations.Parsed;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by jantonak on 27/01/17.
 */
@Service
public class LinksGenerator {

    @Autowired
    private ContractImportItemRepository repository;

    public File generate() throws IOException {
        List<ContractImportItem> items = repository.findByStatus(ContractImportItemStatus.ALL_IMPORTED);

        ImmutableList<Record> records = FluentIterable.from(items).transform(new Function<ContractImportItem, Record>() {
            @Override
            public Record apply(ContractImportItem item) {
                return new Record(item.getUniqueName(), item.getTitle(), item.getHierarchicalType().name(), item.getStatus().name(), item.getAribaWebJumper());
//                        String.format("=HYPERLINK(\"%s\",\"%s\")    ",item.getAribaWebJumper(),"Show Me"));
            }
        }).toList();

        File file = File.createTempFile("links_", ".csv");
        CSVWriter<Record> writer = new CSVWriter<Record>(file, Record.class);
        writer.write(records);
        return zip(file);
    }

    private File zip(File... files) throws IOException {
        File zipFile = File.createTempFile("Links", ".zip");
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (File file : files) {
            FileInputStream in = new FileInputStream(file);
            String entryName = Lists.newArrayList(Splitter.on("_").split(file.getName())).get(0) + ".csv";
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            IOUtils.copy(in, zos);
            in.close();
            zos.closeEntry();
        }
        zos.close();
        return zipFile;
    }

    public class Record {
        @Parsed(field = "ConractId")
        private final String UniqueName;

        @Parsed(field = "Name")
        private final String name;

        @Parsed(field = "HierarchicalType")
        private final String hierarchicalType;

        @Parsed(field = "Status")
        private final String status;

        @Parsed(field = "Link")
        private final String link;


        private Record(String uniqueName, String name, String hierarchicalType, String status, String link) {
            UniqueName = uniqueName;
            this.name = name;
            this.hierarchicalType = hierarchicalType;
            this.status = status;
            this.link = link;
        }
    }


}
