package com.seal.contracts.ws.client.ariba.push;

import com.google.common.collect.Lists;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.generator.csv.bean.Contract;
import junit.framework.TestCase;
import org.junit.Assert;
import org.mockito.Mockito;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by jantonak on 12/06/17.
 */
public class CustomFieldsBuilderTest extends TestCase {


    public void testBuild() throws IllegalAccessException, IntrospectionException, InvocationTargetException {

        final List<String> legalEntities = Lists.newArrayList();
        legalEntities.add("LE1");
        legalEntities.add("LE3");


        Contract contract = Mockito.mock(Contract.class);
        when(contract.getCus_BPLegalEntities()).thenReturn(legalEntities);
        when(contract.getCus_GoverningLaw()).thenReturn("GL1");
        when(contract.getCus_PaymentTermsCW()).thenReturn("PT1");
        when(contract.getCus_Segment()).thenReturn("SEG1");
        when(contract.getCus_incoTerms()).thenReturn("IT1");
        when(contract.getCus_incoTermsPointOfTransfer()).thenReturn("ITPOT1");
        when(contract.getCus_firstDofaApprover()).thenReturn("APP1");
        when(contract.getCus_OperatingLease()).thenReturn(true);
        when(contract.getCus_Longtermcontractualcommitment()).thenReturn(true);


        CustomFieldsBuilder victim = new CustomFieldsBuilder(contract);
        ContractWorkspaceWSProjectImport.Custom result = victim.build();

        Assert.assertEquals(2, result.getCustomStringVector().get(0).getItem().size());
        Assert.assertEquals("BPLegalEntity", result.getCustomStringVector().get(0).getName());
        Assert.assertEquals("LE1", result.getCustomStringVector().get(0).getItem().get(0).getCustomString());
        Assert.assertEquals("LE3", result.getCustomStringVector().get(0).getItem().get(1).getCustomString());


        ArrayList<ContractWorkspaceWSProjectImport.Custom.CustomString> customStrings = Lists.newArrayList(result.getCustomString());
        Collections.sort(customStrings, new Comparator<ContractWorkspaceWSProjectImport.Custom.CustomString>() {
            @Override
            public int compare(ContractWorkspaceWSProjectImport.Custom.CustomString cs1, ContractWorkspaceWSProjectImport.Custom.CustomString cs2) {
                return cs1.getName().compareTo(cs2.getName());
            }
        });

        Assert.assertEquals(6,customStrings.size());

        Assert.assertEquals("FirstDOFAcommitmentapprover", customStrings.get(0).getName());
        Assert.assertEquals("APP1", customStrings.get(0).getValue());

        Assert.assertEquals("GoverningLaw", customStrings.get(1).getName());
        Assert.assertEquals("GL1", customStrings.get(1).getValue());

        Assert.assertEquals("INCOterms", customStrings.get(2).getName());
        Assert.assertEquals("IT1", customStrings.get(2).getValue());

        Assert.assertEquals("INCOtermspointoftransfer", customStrings.get(3).getName());
        Assert.assertEquals("ITPOT1", customStrings.get(3).getValue());

        Assert.assertEquals("PaymenttermsCW", customStrings.get(4).getName());
        Assert.assertEquals("PT1", customStrings.get(4).getValue());

        Assert.assertEquals("Segment", customStrings.get(5).getName());
        Assert.assertEquals("SEG1", customStrings.get(5).getValue());

        ArrayList<ContractWorkspaceWSProjectImport.Custom.CustomBoolean> customBooleans= Lists.newArrayList(result.getCustomBoolean());
        Collections.sort(customBooleans, new Comparator<ContractWorkspaceWSProjectImport.Custom.CustomBoolean>() {
            @Override
            public int compare(ContractWorkspaceWSProjectImport.Custom.CustomBoolean cs1, ContractWorkspaceWSProjectImport.Custom.CustomBoolean cs2) {
                return cs1.getName().compareTo(cs2.getName());
            }
        });

        Assert.assertEquals(2,customBooleans.size());

        Assert.assertEquals("Longtermcontractualcommitment", customBooleans.get(0).getName());
        Assert.assertEquals(true, customBooleans.get(0).isValue());

        Assert.assertEquals("Operatinglease", customBooleans.get(1).getName());
        Assert.assertEquals(true, customBooleans.get(1).isValue());


        Assert.assertTrue(result.getCustomBigDecimal().isEmpty());
        Assert.assertTrue(result.getCustomDate().isEmpty());
        Assert.assertTrue(result.getCustomInteger().isEmpty());
        Assert.assertTrue(result.getCustomMoney().isEmpty());
        Assert.assertTrue(result.getCustomUser().isEmpty());
    }

    public void testBuildWithEmptyList() throws IllegalAccessException, IntrospectionException, InvocationTargetException {

        final List<String> legalEntities = Lists.newArrayList();

        Contract contract = Mockito.mock(Contract.class);
        when(contract.getCus_BPLegalEntities()).thenReturn(legalEntities);

        CustomFieldsBuilder victim = new CustomFieldsBuilder(contract);
        ContractWorkspaceWSProjectImport.Custom result = victim.build();

        Assert.assertEquals(0, result.getCustomStringVector().size());
    }

    public void testBuildWithEmptyString() throws IllegalAccessException, IntrospectionException, InvocationTargetException {

        Contract contract = Mockito.mock(Contract.class);
        when(contract.getCus_GoverningLaw()).thenReturn("");

        CustomFieldsBuilder victim = new CustomFieldsBuilder(contract);
        ContractWorkspaceWSProjectImport.Custom result = victim.build();

        Assert.assertEquals(0, result.getCustomString().size());
    }

    public void testBuildWithNullBoolean() throws IllegalAccessException, IntrospectionException, InvocationTargetException {

        Contract contract = Mockito.mock(Contract.class);
        when(contract.getCus_OperatingLease()).thenReturn(null);
        when(contract.getCus_Longtermcontractualcommitment()).thenReturn(null);

        CustomFieldsBuilder victim = new CustomFieldsBuilder(contract);
        ContractWorkspaceWSProjectImport.Custom result = victim.build();

        Assert.assertEquals(0, result.getCustomBoolean().size());
    }

}