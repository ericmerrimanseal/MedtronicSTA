package com.seal.contracts.generator.csv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by jantonak on 04/06/17.
 */
@Slf4j
@Service
public class Splitter {

    public static final String DELIMITER = ",";

    public Iterable<String> split(String value) {
        return com.google.common.base.Splitter.on(DELIMITER).split(value);
    }

}
