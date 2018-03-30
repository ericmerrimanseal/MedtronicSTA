package com.seal.contracts.generator.csv.service;


import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.bean.CommodityCode;


/**
 * Created by root on 16.08.15..
 */
public interface CommodityCodeService {

    //Looks up the commodity code in format of "domain:uniquename"
    Optional<CommodityCode> lookupById(String uniqueName);

    Optional<CommodityCode> lookupByName(String name);

    Optional<CommodityCode> lookup(String nameOrUniqueName);
}
