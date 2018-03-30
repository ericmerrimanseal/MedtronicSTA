package com.seal.contracts.generator.csv.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.CSVLoader;
import com.seal.contracts.generator.csv.bean.User;
import com.seal.contracts.generator.csv.bean.Users;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 12.08.15..
 */
@Slf4j
public class UsersServiceImpl implements UsersService {

    private final File userFile;

    private final Map<String, User> idToNameMap = Maps.newHashMap();
    private final Map<String, User> nameToIdMap = Maps.newHashMap();

    private final User defaultOwner;

    public UsersServiceImpl(File userFile, User defaultUser) throws IOException {
        Preconditions.checkNotNull(userFile, "userFile must not be null");
        Preconditions.checkArgument(userFile.exists(), "userFile must exist");
        Preconditions.checkNotNull(defaultUser, "defaultOwner must not be null");
        this.userFile = userFile;
        this.defaultOwner = defaultUser;
        init();
    }

    private void init() throws IOException {

        CSVLoader<User> loader = new CSVLoader(userFile, User.class);
        List<User> records = loader.load();
        for (User rec : records) {
            String name = rec.getName().trim().toLowerCase();
            if (nameToIdMap.containsKey(name)) {
                log.warn(String.format("User %s exists multiple times", rec.getName()));
                continue;
            }
            nameToIdMap.put(name, rec);
            idToNameMap.put(rec.getUniqueName(), rec);
        }
    }

    public Users usersByFullName(String fullName, boolean useDefaultIfMissing, boolean useForceUsers) {

        Users.UsersBuilder builder = Users.newBuilder();

        if (Strings.isNullOrEmpty(fullName)) {
            builder.addUser(defaultOwner);
        } else {
            User user = nameToIdMap.get(fullName.trim().toLowerCase());
            if (user != null) {
                builder.addUser(user);
            } else if (useDefaultIfMissing) {
                builder.addUser(defaultOwner);
            }
        }
        return builder.build();
    }

    @Override
    public Optional<User> userByFullName(String fullName, boolean useDefaultIfMissing) {
        Users users = usersByFullName(fullName, useDefaultIfMissing, false);
        return Optional.fromNullable(Iterables.getFirst(users.getUsers(),null));
    }

    @Override
    public Optional<User> userByUniqueName(String uniqueName) {
        return Optional.fromNullable(idToNameMap.get(uniqueName));
    }
}


