package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Preconditions;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;

/**
 * Created by jantonak on 24/11/16.
 */
public class CustomBooleanBuilder {
    private String fieldName;
    private Boolean value;
    private ObjectFactory factory = new ObjectFactory();

    public CustomBooleanBuilder(String fieldName, Boolean value) {
        Preconditions.checkNotNull(fieldName);
        this.fieldName = fieldName;
        this.value = value;
    }

    public ContractWorkspaceWSProjectImport.Custom.CustomBoolean build() {
        ContractWorkspaceWSProjectImport.Custom.CustomBoolean custom = factory.createContractWorkspaceWSProjectImportCustomCustomBoolean();
        custom.setName(fieldName);
        custom.setValue(value);
        return custom;
    }
}

