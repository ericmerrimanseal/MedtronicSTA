package com.seal.contracts.ws.client.seal.pull;

import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.threading.Threadable;
import lombok.Getter;

import java.util.List;

/**
 * Created by Juraj on 23.08.2017.
 */
@Getter
public class SimpleExports implements Threadable {

    private final String legacyContractId;

    private final List<Export> exports = Lists.newArrayList();

    public SimpleExports(String legacyContractId) {
        this.legacyContractId = legacyContractId;
    }

    @Override
    public void lock() {

    }

    @Override
    public void unlock(boolean success) {

    }
}
