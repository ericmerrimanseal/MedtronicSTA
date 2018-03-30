package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Preconditions;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;

import java.util.List;

/**
 * Created by jantonak on 24/11/16.
 */
public class CustomStringVectorBuilder {
    private String fieldName;
    private List<String> values;
    private ObjectFactory factory = new ObjectFactory();

    public CustomStringVectorBuilder(String fieldName, List values) {
        Preconditions.checkNotNull(fieldName);
        this.fieldName = fieldName;
        this.values = values;
    }

    public ContractWorkspaceWSProjectImport.Custom.CustomStringVector build() {
        ContractWorkspaceWSProjectImport.Custom.CustomStringVector vector = factory.createContractWorkspaceWSProjectImportCustomCustomStringVector();
        for (String value : values) {
            ContractWorkspaceWSProjectImport.Custom.CustomStringVector.Item item = factory.createContractWorkspaceWSProjectImportCustomCustomStringVectorItem();
            item.setCustomString(value);
            vector.getItem().add(item);
        }
        vector.setName(fieldName);
        return vector;
    }
}

