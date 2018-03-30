package com.seal.contracts.generator.csv.bean;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.bean.factory.UserFactory;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 25.09.15..
 */
public class Users {

    private static final UserFactory FACTORY = new UserFactory();

    @Getter
    private final Set<User> users = new HashSet<User>();

    private Users() {
    }

    private Users(Users copyFrom) {
        this.getUsers().addAll(copyFrom.getUsers());
    }

    public Users(String rawUsers) {
        this(FACTORY.buildUsers(rawUsers));
    }

    public static UsersBuilder newBuilder() {
        return new UsersBuilder();
    }

    public static UsersBuilder newBuilder(Users copyFrom) {
        return new UsersBuilder(copyFrom);
    }

    public static class UsersBuilder {
        private Users users;

        public UsersBuilder() {
            this.users = new Users();
        }

        public UsersBuilder(Users copyFrom) {
            this.users = new Users();
            this.users.users.addAll(copyFrom.getUsers());
        }

        public Users build() {
            return users;
        }

        public UsersBuilder addUser(User user) {
            users.users.add(user);
            return this;
        }

        public UsersBuilder addUsers(Set<User> usersToAdd) {
            users.users.addAll(usersToAdd);
            return this;
        }

        public boolean hasUsers() {
            return !users.getUsers().isEmpty();
        }
    }

    @Override
    public String toString() {
        final Map<String, String> usersMap = Maps.newHashMap();
        for (User user : users) {
            usersMap.put(user.getUniqueName(), user.getPasswordAdapter().isPresent() ? ":" + user.getPasswordAdapter().get() : null);
        }
        return Joiner.on(",").withKeyValueSeparator("").useForNull("").join(usersMap);
    }
}
