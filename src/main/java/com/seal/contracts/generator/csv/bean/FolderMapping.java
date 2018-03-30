package com.seal.contracts.generator.csv.bean;

import com.univocity.parsers.annotations.Parsed;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jantonak on 24/11/16.
 */
@Data
public class FolderMapping {

    @Parsed(field = "DocumentType_Norm")
    String documentType;

    @Parsed(field = "FolderName")
    String folderName;
}
