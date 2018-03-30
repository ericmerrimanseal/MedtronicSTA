package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceWSProjectImport;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ObjectFactory;
import com.seal.contracts.generator.annotation.AribaField;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.utils.AnnotationUtil;
import org.springframework.beans.BeanUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

/**
 * Created by jantonak on 24/11/16.
 */
public class CustomFieldsBuilder {

    private enum TYPES {String, Boolean, List}

    private Contract contract;
    private ObjectFactory factory = new ObjectFactory();

    public CustomFieldsBuilder(Contract contract) {
        Preconditions.checkNotNull(contract);
        this.contract = contract;
    }

    public ContractWorkspaceWSProjectImport.Custom build() throws IllegalAccessException, IntrospectionException, InvocationTargetException {

        ContractWorkspaceWSProjectImport.Custom custom = factory.createContractWorkspaceWSProjectImportCustom();

        Set<Field> fieldsInAriba = AnnotationUtil.findFields(Contract.class, AribaField.class);
        for (Field fieldInAriba : fieldsInAriba) {
            AribaField aribaField = fieldInAriba.getAnnotation(AribaField.class);
            if (aribaField.custom()) {
                fieldInAriba.setAccessible(true);
                Object value = BeanUtils.getPropertyDescriptor(Contract.class, fieldInAriba.getName()).getReadMethod().invoke(contract);
                setField(fieldInAriba, value, custom);
            }
        }

        return custom;
    }

    private void setField(Field fieldInAriba, Object value, ContractWorkspaceWSProjectImport.Custom target) {
        if (value == null) {
            return;
        }

        AribaField aribaField = fieldInAriba.getAnnotation(AribaField.class);
        if (aribaField == null || !aribaField.custom()) {
            return;
        }


        switch (TYPES.valueOf(fieldInAriba.getType().getSimpleName())) {
            case String:
                if (!Strings.isNullOrEmpty((String) value)) {
                    target.getCustomString().add(new CustomStringBuilder(aribaField.name(), value.toString()).build());
                }
                break;
            case Boolean:
                target.getCustomBoolean().add(new CustomBooleanBuilder(aribaField.name(), (Boolean) value).build());
                break;
            case List:
                if (!((List) value).isEmpty()) {
                    target.getCustomStringVector().add(new CustomStringVectorBuilder(aribaField.name(), (List) value).build());
                }
                break;
            default:
                throw new RuntimeException(String.format("Field %s is of unknown type", fieldInAriba.getName()));
        }

    }
}

