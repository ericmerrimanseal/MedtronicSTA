package com.seal.contracts.generator.csv.bean;

import com.seal.contracts.generator.csv.mapper.meta.ContractDocumentsMeta;
import com.seal.contracts.generator.csv.mapper.meta.ContractTeamsMeta;
import com.seal.contracts.generator.csv.mapper.meta.ContractsMeta;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by root on 11.08.15..
 */
@Getter
public class ImportProjectParameter {

    private static final String FILE_NAME_CONTRACTS = new ContractsMeta().getFileName();
    private static final String FILE_NAME_DOCUMENTS = new ContractDocumentsMeta().getFileName();
    private static final String FILE_NAME_TEAMS = new ContractTeamsMeta().getFileName();

    @Parsed(field = "WorkspaceLookupKey")
    String workspaceLookupKey = "ContractId";

    @Parsed(field = "TemplateName")
    String templateName = "Medtronic Indirect or Direct Importing Contract Template";

    @Parsed(field = "AttributesFileLocation")
    String attributesFileLocation = FILE_NAME_CONTRACTS;

    @Parsed(field = "DocumentsFileLocation")
    String documentsFileLocation = FILE_NAME_DOCUMENTS;

    @Parsed(field = "TeamsFileLocation")
    String teamsFileLocation = FILE_NAME_TEAMS;

    @Parsed(field = "RootParentId")
    String rootParentId;

    @Parsed(field = "TopFolderName")
    String topFolderName = "Test Load";

    @Setter
    @Parsed(field = "FolderFieldName")
    String folderFieldName;

    @Parsed(field = "FolderFieldPattern")
    String folderFieldPattern = "([1-9][0-9]*)";

    @Parsed(field = "FolderFormat")
    String folderFormat = "{0} to {1}";
}
