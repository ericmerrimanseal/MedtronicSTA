package com.seal.contracts.generator.csv.mapper.meta;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by root on 11.08.15..
 */
public class ImportConractParametersMeta implements FileMeta {

    private final Set<String> headerFields = new LinkedHashSet<String>(Arrays.asList(new String[]{
            "WorkspaceLookupKey",
            "TemplateName",
            "AttributesFileLocation",
            "DocumentsFileLocation",
            "TeamsFileLocation",
            "RootParentId",
            "TopFolderName",
            "FolderFieldName",
            "FolderFieldPattern",
            "FolderFormat"})
    );


    public String getFileName() {
        return "ImportProjectsParameters.csv";
    }

    public Set<String> getHeader() {
        return headerFields;
    }
}
