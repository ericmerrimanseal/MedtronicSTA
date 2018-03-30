package com.seal.contracts.generator.csv.bean;

import com.univocity.parsers.annotations.Parsed;
import lombok.Data;

/**
 * Created by root on 11.08.15..
 */
@Data
public class ContractDocument {

    @Parsed(field = "Workspace")
    String workspace;

    @Parsed(field = "File")
    String file;

    @Parsed(field = "Title")
    String title;

    @Parsed(field = "Folder")
    String folder;

    @Parsed(field = "Owner")
    String owner;

    @Parsed(field = "Status")
    String status;
}
