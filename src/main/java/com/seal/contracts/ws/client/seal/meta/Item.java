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
public class Item {
    private String name;
    //    private boolean inReview;
    private List<Value> values = Lists.newArrayList();

    public Item() {
    }

    public Item(String name, List<Value> values) {
        this.name = name;
        this.values.addAll(values);
    }

    @JsonIgnore
    public Object getConcatenatedValues() {
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (values.size() > 1) {
            ImmutableList<Object> allValues = FluentIterable.from(values).filter(new Predicate<Value>() {
                @Override
                public boolean apply(Value value) {
                    return value.getValue() != null && !Strings.isNullOrEmpty(value.getValue().toString());
                }
            }).transform(new Function<Value, Object>() {
                @Override
                public Object apply(Value value) {
                    return value.getValue();
                }
            }).toList();
            return Joiner.on(",").join(allValues);

        } else {
            Object value = values.get(0).getValue();
            return !Strings.isNullOrEmpty(value.toString()) ? value : null;
        }
    }

    @JsonIgnore
    public Object getValue() {
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0).getValue();
    }

    @Override
    public String toString() {
        return "{" +
                "name:'" + name + '\'' +
                ", values:" + values +
                '}';
    }
}