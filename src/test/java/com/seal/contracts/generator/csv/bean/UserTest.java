package com.seal.contracts.generator.csv.bean;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Created by root on 25.09.15..
 */
public class UserTest {

    @Test
    public void testUserWithUniqueNameAndPasswordAdapter() {
        User user = new User("uniqueName:passwordAdapter");
        assertEquals("uniqueName", user.getUniqueName());
        assertEquals("passwordAdapter", user.getPasswordAdapter().get());
    }

    @Test
    public void testUserWithUniqueNameOnly() {
        User user = new User("uniqueName");
        assertEquals("uniqueName", user.getUniqueName());
        assertFalse(user.getPasswordAdapter().isPresent());
    }

}
