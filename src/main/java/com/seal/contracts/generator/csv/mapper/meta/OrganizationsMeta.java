package com.seal.contracts.generator.csv.mapper.meta;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by root on 11.08.15..
 */
public class OrganizationsMeta implements FileMeta {

    private final Set<String> headerFields = new LinkedHashSet<String>(Arrays.asList(new String[]{
            "ExternalOrganizationID",
            "OrganizationName",
            "IsSupplier",
            "IsCustomer",
            "IsOrgApproved",
            "IsManaged",
            "CorporatePhone",
            "CorporateFax",
            "CorporateEmailAddress",
            "CompanyURL",
            "Address",
            "City",
            "State",
            "ZipCode",
            "Country",
            "Organization Source"
    })
    );


    public String getFileName() {
        return "Organizations.csv";
    }

    public Set<String> getHeader() {
        return headerFields;
    }
}
