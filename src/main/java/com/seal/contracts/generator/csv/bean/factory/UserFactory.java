package com.seal.contracts.generator.csv.bean.factory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.seal.contracts.generator.csv.bean.User;
import com.seal.contracts.generator.csv.bean.Users;

import java.util.List;

/**
 * Created by root on 25.09.15..
 */
public class UserFactory {

    private static final String SEPARATOR = ":";
    private static final String LIST_SEPARATOR = ",";

    public static User buildUser(String rawUser) {
        Preconditions.checkNotNull(rawUser, "rawUser must not be null");
        Preconditions.checkArgument(!rawUser.trim().startsWith(SEPARATOR), "uniqueName must be specified");
        Preconditions.checkArgument(!rawUser.trim().endsWith(SEPARATOR), "passwordAdapter not speciied");


        List<String> userDetails = Splitter.on(SEPARATOR).limit(2).splitToList(rawUser.trim());
        String uniqueName = userDetails.get(0);
        Optional<String> passwordAdapter = Optional.absent();
        if (userDetails.size() > 1) {
            passwordAdapter = Optional.of(userDetails.get(1));
        }

        return new User(uniqueName, passwordAdapter);
    }

    public static Users buildUsers(String rawUsers) {
        Preconditions.checkNotNull(rawUsers, "rawUser must not be null");
        Preconditions.checkArgument(!rawUsers.trim().startsWith(LIST_SEPARATOR), "unexpected leading separator");
        Preconditions.checkArgument(!rawUsers.trim().endsWith(LIST_SEPARATOR), "unexpected trailing separator");

        Users.UsersBuilder builder = Users.newBuilder();
        Iterable<String> usersIterable = Splitter.on(LIST_SEPARATOR).omitEmptyStrings().split(rawUsers);

        for (String rawUser : usersIterable) {
            builder.addUser(buildUser(rawUser));
        }

        return builder.build();
    }

}
