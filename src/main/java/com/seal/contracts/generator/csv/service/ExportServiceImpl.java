package com.seal.contracts.generator.csv.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.bean.Supplier;
import com.seal.contracts.generator.csv.exception.ParsingException;
import com.seal.contracts.generator.csv.exception.ValidationException;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static com.seal.contracts.generator.csv.exception.Severity.ERROR;

/**
 * Created by root on 12.08.15..
 */
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final Set<Supplier> suppliers = Sets.newHashSet();
    private final List<Export> data = Lists.newArrayList();

    public ExportServiceImpl(List<Export> data) throws IOException {
        Preconditions.checkNotNull(data, "data can not be null");
        this.data.addAll(data);
    }

    @Override
    public Map<String, Collection<Export>> recordsPerDocumentId() throws IOException, ParsingException {
        Map<String, Collection<Export>> returnValue = Maps.newHashMap();
        for (Export export : data) {
            Optional<String> idOptional = Optional.fromNullable(export.getLegacyContractId());

            log.info("Export {} parsed", idOptional.orNull());

            if (idOptional.isPresent()) {
                String id = idOptional.get();
                if (!returnValue.containsKey(id)) {
                    returnValue.put(id, new LinkedList<Export>());
//                } else {
//                    log.error("Duplicate LecagyContractId: {}", id);
                }
                returnValue.get(id).add(export);
            }
        }


        refreshSuppliers(returnValue);
        return Collections.unmodifiableMap(returnValue);
    }

    @Override
    public Map<String, Collection<Export>> recordsPerSupplier() throws IOException {
        final Map<String, Collection<Export>> returnValue = new TreeMap<String, Collection<Export>>(new Comparator<String>() {
            @Override
            public int compare(String int1, String int2) {
                return int1.compareTo(int2);
            }
        });

        for (Export export : data) {
            Optional<String> supplierIdOptional = Optional.fromNullable(export.getSupplierId());
            if (!supplierIdOptional.isPresent()) {
                continue;
            }
            String supplierId = supplierIdOptional.get();
            if (!returnValue.containsKey(supplierId)) {
                returnValue.put(supplierId, new TreeSet(new DocumentIdComparator()));
            }
            returnValue.get(supplierId).add(export);
        }

        refreshSuppliers(returnValue);
        return Collections.unmodifiableMap(returnValue);
    }

    @Override
    public Collection<Supplier> getSuppliers() throws IOException {
        recordsPerSupplier();
        return Collections.unmodifiableSet(suppliers);
    }

    @Override
    public Collection<Export> getRecords() throws IOException {
        return data;
    }

    private void refreshSuppliers(Map<String, Collection<Export>> records) {
        suppliers.clear();
        for (Map.Entry<String, Collection<Export>> entry : records.entrySet()) {
            for (Export export : entry.getValue()) {
                String supplierId = export.getSupplierId();
                if (supplierId == null) {
                    supplierId = Supplier.EMPTY_SUPPLIER;
                }
                suppliers.add(new Supplier(supplierId, supplierId));
            }
        }
    }

    private class DocumentIdComparator implements Comparator<Export> {
        @Override
        public int compare(Export export1, Export export2) {
            return export1.getLegacyContractId().compareTo(export2.getLegacyContractId());
        }
    }

    private Optional<ParsingException> validateParsable(CsvParser parser) throws IOException {
        return Optional.absent();
    }

}
