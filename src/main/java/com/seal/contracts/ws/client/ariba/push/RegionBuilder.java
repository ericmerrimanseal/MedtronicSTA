package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;

/**
 * Created by jantonak on 24/11/16.
 */
public class RegionBuilder {

    private String rawValue;
    private ObjectFactory factory = new ObjectFactory();

    public RegionBuilder(String rawValue) {
        Preconditions.checkNotNull(rawValue);
        this.rawValue = rawValue;
    }

    public ContractWorkspaceWSProjectImport.Region build() {
        ContractWorkspaceWSProjectImport.Region top = factory.createContractWorkspaceWSProjectImportRegion();

        Iterable<String> items = Splitter.on(":").split(rawValue);
        for (String item : items) {
            ContractWorkspaceWSProjectImport.Region.Item regionItem = factory.createContractWorkspaceWSProjectImportRegionItem();
            regionItem.setRegion(factory.createContractWorkspaceWSProjectImportRegionItemRegion(item));
            top.getItem().add(regionItem);
        }
        return top;
    }
}
