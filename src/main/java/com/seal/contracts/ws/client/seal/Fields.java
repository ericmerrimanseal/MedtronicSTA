package com.seal.contracts.ws.client.seal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Juraj on 01.08.2017.
 */
public class Fields {

    public static final String ARIBA_CONTRACTID = "SC_AribaContractID";
    public static final String ARIBA_DOCUMENTID = "SC_AribaDocumentID";
    public static final String MIGRATION_READY = "SC_MigrationReady";
    public static final String MIGRATION_ERRORS = "SC_MigrationErrorDesc";
    public static final String DOCUMENT_TYPE = "DocumentType_Norm";
    public static final String LEGACY_CONTRACT_ID = "RelatedID_Norm";
    public static final String OWNER = "ProjectOwner_Norm";
    public static final String REVIEW_TYPE = "SC_ReviewType";
    public static final String REVIEW_COMPLETE = "SC_ReviewComplete";
}
