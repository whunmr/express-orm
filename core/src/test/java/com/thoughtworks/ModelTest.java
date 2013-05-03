package com.thoughtworks;

import com.thoughtworks.fixture.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ModelTest extends BaseDBTest{

    private User userA;
    private User userB;
    private User userC;

    @Before
    public void setUp() throws ClassNotFoundException, IOException, SQLException {
        super.setUp();
        truncateTable("users");
        userA = createUser("A", "a@a.a").save();
        userB = createUser("B", "b@b.b").save();
        userC = createUser("C", "c@c.c").save();
    }

    @After
    public void tearDown() throws ClassNotFoundException, IOException, SQLException {
        super.tearDown();
        User.delete_all();
    }

    @Test
    public void should_able_to_save_object_to_table() {
        User user = createUser("Z", "z@z.z");
        User savedUser = user.save();
        assertThat(savedUser, is(sameInstance(user)));
    }

    @Test
    public void should_able_to_find_all_records_by_empty_where_clause() throws SQLException {
        List<User> users = User.find_all();
        assertThat(users, containsInAnyOrder(userA, userB, userC));
    }

    @Test
    public void should_able_to_find_object_in_table_by_primary_key() throws ClassNotFoundException, IOException, SQLException {
        truncateTable("users");
        User user = createUser("A", "a@a.a").save();

        User userInDB = User.find(1);

        assertThat(userInDB, equalTo(user));
    }

    @Test
    public void should_able_to_find_object_by_where_clause() throws SQLException {
        User userInDB = User.where("email = 'a@a.a'");
        assertThat(userInDB, equalTo(userA));
    }

    @Test
    public void should_able_to_find_record_object_by_sql() throws SQLException {
        User userInDB = User.find_by_sql("SELECT * FROM users WHERE email = 'a@a.a'");
        assertThat(userInDB, equalTo(userA));
    }

    @Test
    public void should_able_to_get_row_count_in_table() throws SQLException {
        int recordsCount = User.count();

        assertThat(recordsCount, is(User.<User>find_all().size()));
    }

    @Test
    public void should_able_to_delete_record_by_primary_key() throws ClassNotFoundException, IOException, SQLException {
        int userInDB = User.delete(1);
    }

    @Test
    public void should_able_to_delete_multiple_records_by_primary_key_array() {
        //delete ([1, 2, 3])
    }

    @Test
    public void should_able_to_delete_record_by_where_criteria() {
        //delete_all
    }

    @Test
    public void should_able_to_delete_all_records_by_empty_where_criteria() {
        User.delete_all();
    }

    private User createUser(String firstName, String email) {
        User user = new User();
        user.firstName = firstName;
        user.email = email;
        return user;
    }
}
