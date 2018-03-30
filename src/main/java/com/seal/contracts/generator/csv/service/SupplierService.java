package com.seal.contracts.generator.csv.service;

import com.seal.contracts.generator.csv.bean.Supplier;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by root on 18.10.15..
 */
public interface SupplierService {
    Collection<Supplier> getExistingSuppliers() throws IOException;

    Collection<Supplier> getNonExistingSuppliers(boolean parentsOnly) throws IOException;

    boolean supplierExists(String id) throws IOException;

    Optional<Supplier> lookup(String name) throws IOException;

    void refresh() throws IOException;
}
