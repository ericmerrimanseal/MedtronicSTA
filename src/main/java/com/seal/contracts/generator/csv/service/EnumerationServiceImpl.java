package com.seal.contracts.generator.csv.service;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.AribaEnum;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jantonak on 24/11/16.
 */
public class EnumerationServiceImpl implements EnumerationService {

    private static final Logger LOGGER = Logger.getLogger(EnumerationServiceImpl.class.getName());

    private final File file;

    private Map<String, String> vmoByName = Maps.newHashMap();
    private Map<String, String> entityByName = Maps.newHashMap();
    private Map<String, String> deptByName = Maps.newHashMap();
    private Map<String, String> conTypeByName = Maps.newHashMap();
    private Map<String, String> conCovByName = Maps.newHashMap();
    private Map<String, String> baseLangByName = Maps.newHashMap();
    private Map<String, String> regionsByName = Maps.newHashMap();
    
    public EnumerationServiceImpl(File file) throws IOException {
        this.file = file;
        init();
    }

    private void init() throws IOException {
        CSVLoader<AribaEnum> loader = new CSVLoader(file, AribaEnum.class);
        List<AribaEnum> records = loader.load();
        for (AribaEnum rec : records) {
            final String name = rec.getName().trim().toLowerCase();
            switch (rec.getType()) {
                case BusinessGroup_Norm:
                    if (deptByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    deptByName.put(name, rec.getUniqueName());
                    break;
                case MedtronicEntity_Norm:
                    if (entityByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    entityByName.put(name, rec.getUniqueName());
                    break;
                case BaseLanguage_Norm:
                    if (baseLangByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    baseLangByName.put(name, rec.getUniqueName());
                    break;
                case ContractType_Norm:
                    if (conTypeByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    conTypeByName.put(name, rec.getUniqueName());
                    break;
                case ContractCoverage_Norm:
                    if (conCovByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    conCovByName.put(name, rec.getUniqueName());
                    break;
                case VMO_Norm:
                    if (vmoByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    vmoByName.put(name, rec.getUniqueName());
                    break;
                case Regions_Norm:
                    if (regionsByName.containsKey(name)) {
                        LOGGER.log(Level.WARNING, String.format("%s %s exists multiple times",rec.getType(), name));
                        continue;
                    }
                    regionsByName.put(name, rec.getUniqueName());
                    break;
            }
        }

    }

    @Override
    public Optional<String> byDescription(final String description, Type type) {
        Map<String, String> recs = Maps.newHashMap();

        switch (type) {
            case VMO_Norm:
                recs.putAll(vmoByName);
                break;
            case MedtronicEntity_Norm:
                recs.putAll(entityByName);
                break;
            case BusinessGroup_Norm:
                recs.putAll(deptByName);
                break;
            case ContractType_Norm:
                recs.putAll(conTypeByName);
                break;
            case ContractCoverage_Norm:
                recs.putAll(conCovByName);
                break;
            case BaseLanguage_Norm:
                recs.putAll(baseLangByName);
                break;
            case Regions_Norm:
                recs.putAll(regionsByName);
                break;
        };
  
        final String descToFind = description!=null?description.toLowerCase():description;
        Optional<String> byDesc = Optional.fromNullable(recs.get(descToFind));
        if (!byDesc.isPresent()) {
            Collection<String> uniqueNames = Maps.filterValues(recs, new Predicate<String>() {
                @Override
                public boolean apply(String s) {
                    return s.equals(descToFind);
                }
            }).values();

            if (!uniqueNames.isEmpty()) {
                return Optional.of(uniqueNames.iterator().next());
            }
            return Optional.absent();
        }
        return byDesc;
    };

}
