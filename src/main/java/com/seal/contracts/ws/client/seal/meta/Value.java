package com.seal.contracts.ws.client.seal.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

/**
 * Created by Juraj on 22.08.2017.
 */
@Getter
public class Value {

    @JsonIgnore
    public static final String EXT_SYSTEM = "ARIBA";

    private String origin;
    private Object value;
    private List<Attribute> attributes = Lists.newArrayList();
    private String type;

    public Value() {
    }

    public Value(Object value, String type) {
        this.origin = EXT_SYSTEM;
        this.value = value;
        this.type = type;
        attributes.add(Attribute.createDefault());
        if (value == null || value instanceof String && Strings.isNullOrEmpty((String) value)) {
            attributes.add(Attribute.createEmptyField());
        }
    }

    @JsonIgnore
    public static List<Value> values(List<Object> objects) {
        final List<Value> values = Lists.newArrayList();
        for (Object object : objects) {
            values.add(new Value(object.toString(), object.getClass().getSimpleName()));
        }
        return values;
    }

    @Override
    public String toString() {
        return "{" +
                "value:" + value +
                ", type:'" + type + '\'' +
                '}';
    }
}
