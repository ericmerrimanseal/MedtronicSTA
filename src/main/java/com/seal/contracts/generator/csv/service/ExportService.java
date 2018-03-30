package com.seal.contracts.generator.csv.service;

import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.bean.Supplier;
import com.seal.contracts.generator.csv.exception.ParsingException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by root on 16.08.15..
 */
public interface ExportService {

    Map<String, Collection<Export>> recordsPerDocumentId() throws IOException, ParsingException;

    Map<String, Collection<Export>> recordsPerSupplier() throws IOException;

    Collection<Supplier> getSuppliers() throws IOException;

    Collection<Export> getRecords() throws IOException;
}
