package com.seal.contracts.generator.csv.mapper.meta;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by root on 11.08.15..
 */
public class ContractsMeta implements FileMeta {

    private final Set<String> headerFields = new LinkedHashSet<String>(Arrays.asList(new String[]{
            "Owner",
            "Title",
            "ContractId",
            "Description",
            "Supplier",
            "AffectedParties",
            "HierarchicalType",
            "ParentAgreement",
            "ProposedAmount",
            "Amount",
            "Commodity",
            "Region",
            "Department",
            "ExpirationTermType",
            "AutoRenewalInterval",
            "MaxAutoRenewalsAllowed",
            "AgreementDate",
            "EffectiveDate",
            "ExpirationDate",
            "NoticePeriod",
            "NoticeEmailRecipients",
            "ExpiringEmailRecipients",
            "RelatedId",
            "ContractStatus",
            "Base Language"})
    );

    public String getFileName() {
        return "Contracts.csv";
    }

    public Set<String> getHeader() {
        return headerFields;
    }


}
