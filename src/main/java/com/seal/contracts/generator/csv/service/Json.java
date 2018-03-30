package com.seal.contracts.generator.csv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Juraj on 01.08.2017.
 */

public enum Json {
    JSON;

    @Getter
    private final ObjectMapper mapper = new ObjectMapper();

    private Json() {
    }

    public String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public Object toObject(InputStream stream, Class clazz) throws IOException {
        return mapper.readValue(stream, clazz);
    }

    }
