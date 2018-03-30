package com.seal.contracts.generator.csv.bean;

import com.google.common.collect.Lists;
import com.seal.contracts.threading.Threadable;
import com.seal.contracts.ws.client.seal.meta.SealContractsResponse;
import com.univocity.parsers.annotations.Parsed;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by root on 11.08.15..
 */
@Getter
public class SimpleExport {

    @Parsed(field = "ContractId")
    private String hash;

    @Setter
    private boolean migrationReady;

    @Parsed(field = "RelatedID_Norm")
    @Setter
    private String legacyContractId;

    @Setter
    @Getter
    private SealContractsResponse data;

    @Getter
    private final List<String> errors = Lists.newArrayList();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleExport that = (SimpleExport) o;

        return hash.equals(that.hash);

    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }
}

