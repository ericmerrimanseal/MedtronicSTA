package com.seal.contracts.generator.csv.service;


import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.bean.User;
import com.seal.contracts.generator.csv.bean.Users;

/**
 * Created by root on 16.08.15..
 */
public interface UsersService {
    Users usersByFullName(String fullName, boolean useDefaultIfMissing, boolean useForceUsers);

    Optional<User> userByFullName(String fullName, boolean useDefaultIfMissing);

    Optional<User> userByUniqueName(String uniqueName);
}
