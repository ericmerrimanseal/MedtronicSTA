package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Preconditions;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;

/**
 * Created by jantonak on 24/11/16.
 */
public class CustomStringBuilder {
    private String fieldName;
    private String value;
    private ObjectFactory factory = new ObjectFactory();

    public CustomStringBuilder(String fieldName, String value) {
        Preconditions.checkNotNull(fieldName);
        this.fieldName = fieldName;
        this.value = value;
    }

    public ContractWorkspaceWSProjectImport.Custom.CustomString build() {
        ContractWorkspaceWSProjectImport.Custom.CustomString custom = factory.createContractWorkspaceWSProjectImportCustomCustomString();
        custom.setName(fieldName);
        custom.setValue(value);
        return custom;
    }
}

