package com.seal.contracts.generator.csv;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Created by jantonak on 14.01.16..
 */
public class CSVLoader<T> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final File csvFile;
    private final Class<T> entityType;
    private final CsvFormat csvFormat;

    public CSVLoader(File csvFile, Class<T> entityType) {
        this(csvFile, entityType, ',');
    }

    public CSVLoader(File csvFile, Class<T> entityType, char delimiter) {
        this.csvFile = csvFile;
        this.entityType = entityType;
        this.csvFormat = new CsvFormat();
        this.csvFormat.setDelimiter(delimiter);
    }

    public List<T> load() throws IOException {
        return load(new AlwaysTrue());
    }

    public List<T> load(Predicate<T> predicate) throws IOException {

        Locale.setDefault(Locale.ENGLISH);
        final List<T> filteredRecords = Lists.newArrayList();

        if (skip()) {
            return Lists.newArrayList();
        }

        Reader inputStreamReader = null;
        inputStreamReader = new InputStreamReader(new FileInputStream(csvFile), CHARSET);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        if (shouldSkipFirstLine(csvFile)) {
            bufferedReader.readLine();
        }
        final BeanListProcessor<T> rowProcessor = new BeanListProcessor<T>(entityType);
        final CsvParserSettings settings = new CsvParserSettings();
        settings.setRowProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);
        settings.setFormat(csvFormat);
        final CsvParser parser = new CsvParser(settings);

        parser.parse(bufferedReader);
        bufferedReader.close();
        inputStreamReader.close();

        final List<T> records = rowProcessor.getBeans();

        filteredRecords.addAll(filter(records));

        return FluentIterable.from(filteredRecords).filter(predicate).toList();
    }

    private boolean shouldSkipFirstLine(File inputFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String firstLine = bufferedReader.readLine();
        if (!Strings.isNullOrEmpty(firstLine)) {
            firstLine = firstLine.replaceAll("\"", "");
            if (firstLine.startsWith(CHARSET.name())) {
                return true;
            }
        }
        return false;
    }

    protected boolean skip() {
        return false;
    }


    protected List<T> filter(List<T> in) {
        return Lists.newArrayList(in);
    }

    private class AlwaysTrue implements Predicate<T> {
        @Override
        public boolean apply(T t) {
            return true;
        }
    }

}
