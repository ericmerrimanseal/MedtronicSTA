package com.seal.contracts.generator.csv.converter;

import com.google.common.base.Optional;
import com.univocity.parsers.conversions.Conversion;

/**
 * Created by root on 21.10.15..
 */
public class StringToOptionalConversion implements Conversion<String, Optional<String>> {


    public StringToOptionalConversion(String... params) {
    }

    @Override
    public Optional<String> execute(String s) {
        return Optional.fromNullable(s);
    }

    @Override
    public String revert(Optional<String> o) {
        return o.orNull();
    }

}
