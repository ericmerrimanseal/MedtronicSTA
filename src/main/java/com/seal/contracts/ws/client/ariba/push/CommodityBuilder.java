package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;

import java.util.List;

/**
 * Created by jantonak on 24/11/16.
 */
public class CommodityBuilder {

    private String rawCommodities;
    private com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory factory = new ObjectFactory();

    public CommodityBuilder(String rawCommodities) {
        Preconditions.checkNotNull(rawCommodities);
        this.rawCommodities = rawCommodities;
    }

    public ContractWorkspaceWSProjectImport.Commodity build() {
        ContractWorkspaceWSProjectImport.Commodity commodity = factory.createContractWorkspaceWSProjectImportCommodity();

        Iterable<String> items = Splitter.on(",").split(rawCommodities);
        for (String item : items) {
            List<String> strings = Splitter.on(":").splitToList(item);
            ContractWorkspaceWSProjectImport.Commodity.Item i = factory.createContractWorkspaceWSProjectImportCommodityItem();
            i.setDomain(strings.get(0));
            i.setUniqueName(strings.get(1));
            commodity.getItem().add(i);
        }
        return commodity;
    }
}
