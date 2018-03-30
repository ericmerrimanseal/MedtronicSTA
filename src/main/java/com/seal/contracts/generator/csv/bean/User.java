package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.bean.factory.UserFactory;
import com.seal.contracts.generator.csv.converter.StringToOptionalConversion;
import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by root on 12.08.15..
 */
public class User implements Serializable {

    private static final UserFactory FACTORY = new UserFactory();

    @Getter
    @Parsed(field = "UniqueName")
    private String uniqueName;

    @Getter
    @Parsed(field = "Name")
    private String name;

    @Getter
    @Parsed(field = "PasswordAdapter")
    @Convert(conversionClass = StringToOptionalConversion.class)
    private Optional<String> passwordAdapter = Optional.absent();

    public User() {
    }

    public User(String uniqueName, Optional<String> passwordAdapter) {
        this.uniqueName = uniqueName;
        this.passwordAdapter = passwordAdapter;
    }

    private User(User copyFrom) {
        this(copyFrom.uniqueName, copyFrom.passwordAdapter);
    }

    public User(String rawUser) {
        this(FACTORY.buildUser(rawUser));
    }
}
