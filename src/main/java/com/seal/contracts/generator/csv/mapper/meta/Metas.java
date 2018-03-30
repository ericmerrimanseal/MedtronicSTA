package com.seal.contracts.generator.csv.mapper.meta;

import com.google.common.base.Preconditions;

/**
 * Created by root on 17.08.15..
 */
public class Metas {
    private final FileMeta contractsMeta;
    private final FileMeta documentsMeta;
    private final FileMeta teamsMeta;
    private final FileMeta parametersMeta;

    public Metas(FileMeta contractsMeta, FileMeta documentsMeta, FileMeta teamsMeta, FileMeta parametersMeta) {
        Preconditions.checkNotNull(contractsMeta, "contractsMeta must not be null");
        Preconditions.checkNotNull(documentsMeta, "documentsMeta must not be null");
        Preconditions.checkNotNull(teamsMeta, "teamsMeta must not be null");
        Preconditions.checkNotNull(parametersMeta, "parametersMeta must not be null");

        this.contractsMeta = contractsMeta;
        this.documentsMeta = documentsMeta;
        this.teamsMeta = teamsMeta;
        this.parametersMeta = parametersMeta;
    }

    public FileMeta getContractsMeta() {
        return contractsMeta;
    }

    public FileMeta getDocumentsMeta() {
        return documentsMeta;
    }

    public FileMeta getTeamsMeta() {
        return teamsMeta;
    }

    public FileMeta getParametersMeta() {
        return parametersMeta;
    }
}
