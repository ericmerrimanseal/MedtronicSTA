package com.seal.contracts.generator.csv.mapper.meta;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by root on 11.08.15..
 */
public class ContractDocumentsMeta implements FileMeta {

    private final Set<String> headerFields = new LinkedHashSet<String>(Arrays.asList(new String[]{
            "Workspace",
            "File",
            "Title",
            "Folder",
            "Owner",
            "Status"})
    );


    public String getFileName() {
        return "ContractDocuments.csv";
    }

    public Set<String> getHeader() {
        return headerFields;
    }
}
