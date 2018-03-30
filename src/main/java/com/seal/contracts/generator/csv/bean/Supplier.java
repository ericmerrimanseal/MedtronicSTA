package com.seal.contracts.generator.csv.bean;

import lombok.Getter;

/**
 * Created by root on 12.10.15..
 */
public class Supplier implements Comparable {

    public static final String EMPTY_SUPPLIER = "EMPTY_SUPPLIER";

    public static final Supplier UNKNOWN = new Supplier("--UNKNOWN--", "--UNKNOWN--");

    @Getter
    private final String erpId;

    @Getter
    private final String systemId;

    private Supplier() {
        this.erpId = null;
        this.systemId = null;
    }

    public Supplier(String id, String name) {
        this.erpId = id;
        this.systemId = name;
    }

    public Supplier(Integer id, String name) {
        this(String.valueOf(id), name);
    }

    @Override
    public int compareTo(Object o) {
        return this.erpId.compareTo(((Supplier) o).erpId);
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id='" + erpId + '\'' +
                ", name='" + systemId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Supplier supplier = (Supplier) o;

        return erpId.equals(supplier.erpId);

    }

    @Override
    public int hashCode() {
        return erpId.hashCode();
    }
}
