package com.seal.contracts.generator.csv.bean;

import com.univocity.parsers.annotations.Parsed;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by root on 11.08.15..
 */
@Data
public class ContractTeamMember {

    @Parsed(field = "Workspace")
    String workspace;

    @Parsed(field = "ProjectGroup")
    String projectGroup;

    @Parsed(field = "Member")
    String member;
}
