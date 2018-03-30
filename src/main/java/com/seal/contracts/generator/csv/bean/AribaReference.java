package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Strings;
import lombok.Getter;

/**
 * Created by jantonak on 09/09/17.
 */
@Getter
public class AribaReference implements TargetReference {

    private final String contractId;
    private final String documentId;

    public AribaReference(String contractId, String documentId) {
        this.contractId = Strings.emptyToNull(contractId);
        this.documentId = Strings.emptyToNull(documentId);
    }

    @Override
    public boolean existsInTarget() {
        return documentId != null;
    }
}
