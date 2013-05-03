package com.thoughtworks;

import com.thoughtworks.fixture.User;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ModelTest extends BaseDBTest{
    @Test
    public void should_able_to_save_object_to_table() {
        User user = createUser("name", "a@b.c");

        User savedUser = user.save();

        assertThat(savedUser, is(notNullValue()));
        assertThat(savedUser, is(sameInstance(user)));
    }

    @Test
    public void should_able_to_find_object_in_table_by_primary_key() throws ClassNotFoundException, IOException, SQLException {
        resetDatabase();
        User user = createUser("name", "a@b.c").save();

        User userInDB = User.find(1);

        assertThat(userInDB.firstName, is(user.firstName));
        assertThat(userInDB.email, is(user.email));
    }

    @Test
    public void should_able_to_find_object_by_where_clause() {
        User userA = createUser("A", "a@a.a").save();
        User userB = createUser("B", "b@b.b").save();

        User userA_InDB = User.where("email = 'a@a.a'");

        assertThat(userA_InDB.firstName, is(userA.firstName));
        assertThat(userA_InDB.email, is(userA.email));
    }

    private User createUser(String firstName, String email) {
        User user = new User();
        user.firstName = firstName;
        user.email = email;
        return user;
    }

}
