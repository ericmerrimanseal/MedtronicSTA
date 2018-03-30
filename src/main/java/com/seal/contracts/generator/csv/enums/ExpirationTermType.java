package com.seal.contracts.generator.csv.enums;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by root on 06.09.15..
 */
public enum ExpirationTermType {

    FIXED("Fixed", "Fixed"),
    PERPETUAL("Perpetual", "Perpetual"),
    AUTORENEW("AutoRenew", "AutoRenew"),
    AUTORENEW2("Autorenew", "AutoRenew");

    private static Map<String, ExpirationTermType> lookup = Maps.newHashMap();

    static {
        for (ExpirationTermType type : ExpirationTermType.values()) {
            lookup.put(type.getSealDescription(), type);
        }
    }

    private final String sealDescription;
    private final String aribaDescription;

    ExpirationTermType(String sealDescription, String aribaDescription) {
        this.sealDescription = sealDescription;
        this.aribaDescription = aribaDescription;
    }

    public String getSealDescription() {
        return sealDescription;
    }

    public String getAribaDescription() {
        return aribaDescription;
    }

    public static Optional<ExpirationTermType> bySealDescription(String string) {
        return Optional.fromNullable(lookup.get(string));
    }

}
