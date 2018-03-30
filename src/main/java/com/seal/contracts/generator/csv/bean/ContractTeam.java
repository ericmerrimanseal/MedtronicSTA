package com.seal.contracts.generator.csv.bean;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Collection;

/**
 * Created by root on 23.09.15..
 */
public class ContractTeam {
    @Getter
    Collection<ContractTeamMember> members = Sets.newHashSet();

    public static ContractTeamBuilder newBuilder() {
        return new ContractTeamBuilder();
    }

    public static class ContractTeamBuilder {
        private final ContractTeam team;

        public ContractTeamBuilder() {
            this.team = new ContractTeam();
        }

        public ContractTeam build() {
            return this.team;
        }

        public ContractTeamBuilder(ContractTeam copyFrom) {
            this();
            this.team.members.addAll(copyFrom.members);
        }

        public ContractTeamBuilder addMember(ContractTeamMember member) {
            this.team.members.add(member);
            return this;
        }

        public ContractTeamBuilder addMembers(Collection<ContractTeamMember> members) {
            this.team.members.addAll(members);
            return this;
        }

    }

}
