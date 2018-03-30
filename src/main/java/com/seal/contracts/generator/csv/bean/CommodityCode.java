package com.seal.contracts.generator.csv.bean;

import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

/**
 * Created by root on 11.08.15..
 */
//@CsvDataType
public class CommodityCode {

    public enum Domain {unspsc, custom, ascc}

    public static CommodityCode UNKNOWN = new CommodityCode("--UNKNOWN--", Domain.unspsc);

    @Getter
    @Parsed(field = "UniqueName")
    String uniqueName;

    @Getter
    @Parsed(field = "Domain")
    Domain doman;

    @Getter
    @Parsed(field = "Name")
    String name;

    public String getKey() {
        return String.format("%s:%s", doman, uniqueName);
    }

    public CommodityCode(String uniqueName, Domain doman) {
        this.uniqueName = uniqueName;
        this.doman = doman;
    }

    public CommodityCode() {
    }
}
