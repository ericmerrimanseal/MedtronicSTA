package com.seal.contracts.generator.csv.bean.factory;

import com.google.common.collect.Iterables;
import com.seal.contracts.generator.csv.bean.User;
import com.seal.contracts.generator.csv.bean.Users;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created by root on 25.09.15..
 */
public class UserFactoryTest {

    private static UserFactory victim;

    @BeforeClass
    public static void beforeClass() {
        victim = new UserFactory();
    }

    @Test
    public void testUniqueNameAndPasswordAdapter() {
        User user = victim.buildUser("uniqueName:passwordAdapter");
        assertNotNull(user);
        assertEquals("uniqueName", user.getUniqueName());
        assertTrue(user.getPasswordAdapter().isPresent());
        assertEquals("passwordAdapter", user.getPasswordAdapter().get());
    }

    @Test
    public void testUniqueNameOnly() {
        User user = victim.buildUser("uniqueName");
        assertNotNull(user);
        assertEquals("uniqueName", user.getUniqueName());
        assertFalse(user.getPasswordAdapter().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUniqueNameOnlyWithTrailingSeparator() {
        victim.buildUser("uniqueName:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPasswordAdapterOnly() {
        victim.buildUser(":passwordAdapter");
    }

    // USERS
    @Test
    public void testOneUser() {
        Users users = victim.buildUsers("uniqueName:passwordAdapter");
        assertNotNull(users);
        assertEquals(1, users.getUsers().size());

        User firstUser = Iterables.getFirst(users.getUsers(), null);

        assertEquals("uniqueName", firstUser.getUniqueName());
        assertTrue(firstUser.getPasswordAdapter().isPresent());
        assertEquals("passwordAdapter", firstUser.getPasswordAdapter().get());
    }

    @Test
    public void testTwoUsers() {
        Users users = victim.buildUsers("user1:pwdAdapter,user2:pwdAdapter");
        assertNotNull(users);
        assertEquals(2, users.getUsers().size());

        List<User> usersOrdered = new ArrayList<User>(users.getUsers());
        Collections.sort(usersOrdered, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getUniqueName().equals("user1")?-1:1;
            }
        });

        User firstUser = usersOrdered.get(0);
        User secondUser = usersOrdered.get(1);

        assertEquals("user1", firstUser.getUniqueName());
        assertTrue(firstUser.getPasswordAdapter().isPresent());
        assertEquals("pwdAdapter", firstUser.getPasswordAdapter().get());

        assertEquals("user2", secondUser.getUniqueName());
        assertTrue(secondUser.getPasswordAdapter().isPresent());
        assertEquals("pwdAdapter", secondUser.getPasswordAdapter().get());
    }

    @Test
    public void testTwoUsersWOPasswordAdapter() {
        Users users = victim.buildUsers("user1:pwdAdapter,user2");
        assertNotNull(users);
        assertEquals(2, users.getUsers().size());

        List<User> usersOrdered = new ArrayList<User>(users.getUsers());
        Collections.sort(usersOrdered, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getUniqueName().equals("user1") ? -1 : 1;
            }
        });

        User firstUser = usersOrdered.get(0);
        User secondUser = usersOrdered.get(1);

        assertEquals("user1", firstUser.getUniqueName());
        assertTrue(firstUser.getPasswordAdapter().isPresent());
        assertEquals("pwdAdapter", firstUser.getPasswordAdapter().get());

        assertEquals("user2", secondUser.getUniqueName());
        assertFalse(secondUser.getPasswordAdapter().isPresent());
    }

    @Test
    public void testNoUsers() {
        Users users = victim.buildUsers("");
        assertNotNull(users);
        assertEquals(0, users.getUsers().size());
    }

}
