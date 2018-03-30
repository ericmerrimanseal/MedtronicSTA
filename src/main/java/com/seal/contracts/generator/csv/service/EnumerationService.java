package com.seal.contracts.generator.csv.service;

import com.seal.contracts.generator.csv.service.EnumerationService.Type;

/**
 * Created by root on 16.08.15..
 */
public interface EnumerationService {

    public enum Type {Regions_Norm, Description_Norm, ContractType_Norm, MedtronicEntity_Norm, BusinessGroup_Norm, VMO_Norm, ContractCoverage_Norm, BaseLanguage_Norm}

    com.google.common.base.Optional<String> byDescription(String description, Type type);
}
