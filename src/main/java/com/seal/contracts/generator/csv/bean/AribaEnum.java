package com.seal.contracts.generator.csv.bean;

import com.seal.contracts.generator.csv.service.EnumerationServiceImpl;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jantonak on 24/11/16.
 */
public class AribaEnum {

    @Setter
    @Getter
    @Parsed(field = "UniqueName")
    String uniqueName;

    @Setter
    @Getter
    @Parsed(field = "Name")
    String name;

    @Setter
    @Getter
    @Parsed(field = "Type")
    EnumerationServiceImpl.Type type;

}
