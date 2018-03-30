package com.seal.contracts.generator.csv.bean;

import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

/**
 * Created by root on 11.08.15..
 */
public class SupplierProfile {

    @Parsed(field = "ERPId")
    @Getter
    String erpId;

    @Parsed(field = "Name")
    @Getter
    String organization;

    @Parsed(field = "SystemID")
    @Getter
    String orgId;


    public SupplierProfile() {
    }

    public SupplierProfile(String organization, String orgId, String erpId) {
        this.organization = organization;
        this.orgId = orgId;
        this.erpId = erpId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupplierProfile that = (SupplierProfile) o;

        return erpId.equals(that.erpId);

    }

    @Override
    public int hashCode() {
        return erpId.hashCode();
    }
}
