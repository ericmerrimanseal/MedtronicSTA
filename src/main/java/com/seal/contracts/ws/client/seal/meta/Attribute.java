package com.seal.contracts.ws.client.seal.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

/**
 * Created by Juraj on 22.08.2017.
 */
@Getter
public class Attribute {

    @JsonIgnore
    private static final String ORIGIN = "origin";
    @JsonIgnore
    private static final String EMPTY_FIELD = "EmptyField";

    private String name;
    private Object value;

    public Attribute() {
    }

    public Attribute(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @JsonIgnore
    public static Attribute createDefault() {
        return new Attribute(ORIGIN, Value.EXT_SYSTEM);
    }

    @JsonIgnore
    public static Attribute createEmptyField() {
        return new Attribute(EMPTY_FIELD, true);
    }

}
