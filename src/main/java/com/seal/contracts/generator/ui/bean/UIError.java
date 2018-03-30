package com.seal.contracts.generator.ui.bean;

import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.exception.Severity;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

/**
 * Created by root on 17.08.15..
 */
public class UIError {
    @Getter
    @Parsed(field = "ContractId")
    private final String contractId;

    @Getter
    @Parsed(field = "Error")
    private final String error;

    @Getter
    @Parsed(field = "Severity")
    private final Severity severity;

    @Getter
    @Parsed(field = "RecordId")
    private final Optional<String> recordId;

    public UIError(String contractId, String error, Severity severity, Optional<String> recordId) {
        this.contractId = contractId;
        this.error = error;
        this.severity = severity;
        this.recordId = recordId;
    }
}
