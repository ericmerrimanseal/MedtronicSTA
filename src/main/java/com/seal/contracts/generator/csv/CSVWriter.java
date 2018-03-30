package com.seal.contracts.generator.csv;

import com.google.common.collect.Lists;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by jantonak on 14.01.16..
 */
public class CSVWriter<T> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final File csvFile;
    private final Class<T> entityType;
    private final String[] headerFields;

    public CSVWriter(File csvFile, Class<T> entityType) {
        this.csvFile = csvFile;
        this.entityType = entityType;
        this.headerFields = extractHeader(entityType);
    }

    public CSVWriter(File csvFile, Class<T> entityType, String[] headerFields) {
        this.csvFile = csvFile;
        this.entityType = entityType;
        this.headerFields = headerFields;
    }

    public void write(List<T> records) throws IOException {

        CsvWriterSettings settings = new CsvWriterSettings();

        settings.setQuoteAllFields(true);
        settings.setHeaders(headerFields);
        settings.setRowWriterProcessor(new BeanWriterProcessor<T>(entityType));

        CsvWriter writer = new CsvWriter(new FileWriter(csvFile), settings);
        boolean needsExplicitEncoding = needsExplicitEncoding();

        if (needsExplicitEncoding) {
            writer.writeRow(CHARSET.name());
        }

        writer.writeHeaders();

        writer.processRecordsAndClose(records);
    }

    private boolean needsExplicitEncoding() {
        return true;
    }

    private String[] extractHeader(Class<T> clazz) {
        List<String> headerFields = Lists.newArrayList();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parsed.class)) {
                headerFields.add(field.getAnnotation(Parsed.class).field());
            }
        }

        String[] returnValue = new String[headerFields.size()];
        headerFields.toArray(returnValue);
        return returnValue;
    }

}
