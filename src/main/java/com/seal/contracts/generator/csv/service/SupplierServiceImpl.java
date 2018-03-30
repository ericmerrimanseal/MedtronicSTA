package com.seal.contracts.generator.csv.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.Supplier;
import com.seal.contracts.generator.csv.bean.SupplierProfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by root on 18.10.15..
 */
public class SupplierServiceImpl implements SupplierService {

    private final File supplierProfileFile;

    private final ExportService sealExportService;

    private final Set<Supplier> existingSuppliers = Sets.newHashSet();

    public SupplierServiceImpl(File supplierProfileFile, ExportService sealExportService) throws FileNotFoundException {
        Preconditions.checkNotNull(supplierProfileFile, "supplierProfileFile must not be empty");
        Preconditions.checkArgument(supplierProfileFile.exists() && supplierProfileFile.isFile(), "supplierProfileFile must exists and must be a file");
        Preconditions.checkNotNull(sealExportService, "sealExportService must not be empty");

        this.sealExportService = sealExportService;
        this.supplierProfileFile = supplierProfileFile;
    }

    @Override
    public Collection<Supplier> getExistingSuppliers() throws IOException {
        existingSuppliers.clear();
        CSVLoader<SupplierProfile> loader = new CSVLoader<>(supplierProfileFile, SupplierProfile.class);
        List<SupplierProfile> supplierProfiles = loader.load();

        for (SupplierProfile supplierProfile : supplierProfiles) {
            existingSuppliers.add(new Supplier(supplierProfile.getErpId(), supplierProfile.getOrgId()));
        }
        return Collections.unmodifiableSet(existingSuppliers);
    }

    @Override
    public Collection<Supplier> getNonExistingSuppliers(boolean parentsOnly) throws IOException {
        Collection<Supplier> returnValue = Sets.newHashSet();
        Collection<Supplier> existingSuppliers = getExistingSuppliers();
        Collection<Supplier> suppliersFromSealExport = getSuppliersFromSealExport(parentsOnly);

        returnValue.addAll(suppliersFromSealExport);

        returnValue.removeAll(existingSuppliers);
        return returnValue;
    }


    @Override
    public boolean supplierExists(String id) throws IOException {
        if (existingSuppliers.isEmpty()) {
            refresh();
        }
        return existingSuppliers.contains(new Supplier(id, null));
    }

    private Optional<Supplier> lookupById(final String id) throws IOException {
        return existingSuppliers.stream().filter(new Predicate<Supplier>() {
            @Override
            public boolean test(Supplier supplier) {
                return supplier.getErpId().equals(id);
            }
        }).findFirst();
    }

    private Optional<Supplier> lookupByName(final String name) throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<Supplier> lookup(final String idOrName) throws IOException {
        if (existingSuppliers.isEmpty()) {
            refresh();
        }
        Optional<Supplier> byId = lookupById(idOrName);
        if (byId.isPresent()) {
            return byId;
        } else {
            return lookupByName(idOrName);
        }
    }

    @Override
    public void refresh() throws IOException {
        getExistingSuppliers();
    }

    private Collection<Supplier> getSuppliersFromSealExport(boolean parentsOnly) throws IOException {
        return sealExportService.getSuppliers();
    }

}
