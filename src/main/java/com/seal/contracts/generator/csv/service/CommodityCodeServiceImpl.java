package com.seal.contracts.generator.csv.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.CommodityCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by jantonak on 02/12/16.
 */
@Slf4j
public class CommodityCodeServiceImpl implements CommodityCodeService {

    private static final String FORMAT = "%s";

    private final File file;

    private Map<String, CommodityCode> records = Maps.newHashMap();

    private Map<String, CommodityCode> recordsByName = Maps.newHashMap();

    public CommodityCodeServiceImpl(File file) throws IOException {
        this.file = file;
        init();
    }

    private void init() throws IOException {
        CSVLoader<CommodityCode> loader = new CSVLoader(file, CommodityCode.class);
        List<CommodityCode> commodityCodes = loader.load();
        records.putAll(Maps.uniqueIndex(commodityCodes, new Function<CommodityCode, String>() {
            @Override
            public String apply(CommodityCode commodityCode) {
//                return String.format(FORMAT, commodityCode.getDoman(), commodityCode.getUniqueName());
                return String.format(FORMAT, commodityCode.getUniqueName());
            }
        }));

        for (CommodityCode commodityCode : commodityCodes) {
            if (recordsByName.containsKey(commodityCode.getName())) {
                log.warn(String.format("Commodity Code with Name %s already exists -> skipping", commodityCode.getName()));
            } else {
                recordsByName.put(commodityCode.getName(), commodityCode);
            }
        }
    }

    @Override
    public Optional<CommodityCode> lookupById(String id) {
        return Optional.fromNullable(records.get(id));
    }

    @Override
    public Optional<CommodityCode> lookupByName(String name) {
        return Optional.fromNullable(recordsByName.get(name));
    }

    @Override
    public Optional<CommodityCode> lookup(String nameOrUniqueName) {
        return lookupById(nameOrUniqueName).or(lookupByName(nameOrUniqueName));
    }

}
