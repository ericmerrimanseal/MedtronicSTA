package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceImportRequest;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;

import javax.xml.datatype.DatatypeConfigurationException;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * Created by Juraj on 11.09.2016.
 */
public class WSObjectBuilder {

    private final Config config;
    private final ContractImportItem item;
    private final Optional<ContractImportItem> parentItem;

    public WSObjectBuilder(Config config, ContractImportItem item, Optional<ContractImportItem> parentItem) {
        Preconditions.checkNotNull(config, "config must not be null");
        this.config = config;
        this.item = item;
        this.parentItem = parentItem;
    }

    public ContractWorkspaceImportRequest buildConractHeaderRequest() throws IOException, URISyntaxException, DatatypeConfigurationException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        CWImportRequestBuilder builder = new CWImportRequestBuilder(item.getData(), config);
        return builder.buildContractWorkspaceImportRequest(Optional.fromNullable(item.getAribaId()), parentItem);
    }
}
