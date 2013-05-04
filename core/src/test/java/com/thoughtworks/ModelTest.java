package com.thoughtworks;

import com.thoughtworks.fixture.DayEnum;
import com.thoughtworks.fixture.Misc;
import com.thoughtworks.fixture.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
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
    public void should_able_to_save_object_to_table() throws SQLException {
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
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        int rowDeleted = User.delete(1);
        assertThat(rowDeleted, equalTo(1));
        assertThat(User.<User>find_all(), containsInAnyOrder(userB, userC));
    }

    @Test
    public void should_able_to_delete_multiple_records_by_primary_key_array() throws SQLException {
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        User.delete(1, 3);
        assertThat(User.<User>find_all(), containsInAnyOrder(userB));
    }

    @Test
    public void should_able_to_delete_record_by_where_criteria() throws SQLException {
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        User.delete_all("email = 'a@a.a'");
        assertThat(User.<User>find_all(), containsInAnyOrder(userB, userC));
    }

    @Test
    public void should_able_to_delete_all_records_by_empty_where_criteria() throws SQLException {
        User.delete_all();
    }

    /////////////////////////////////////////////////////////

    @Test
    public void should_able_to_mapping_primitive_members_to_db_columns() throws SQLException {
        truncateTable("miscs");
        Misc misc = new Misc();
        misc.boolValue = true;
        misc.charValue = 'c';
        misc.byteValue = 4;
        misc.shortValue = 8;
        misc.integerValue = 15;
        misc.longValue = 16L;
        misc.floatValue = 23.0f;
        misc.doubleValue = 42.0d;
        misc.stringValue = "LOST";

        misc.primitiveIntValue = 4;
        misc.primitiveDoubleValue = 8;
        misc.primitiveFloatValue = 15.0f;
        misc.primitiveBooleanValue = true;
        misc.primitiveShortValue = 23;
        misc.primitiveLongValue = 42L;
        misc.primitiveByteValue = 0;

        misc.day = DayEnum.MONDAY;

        misc.save();

        Misc miscInDB = Misc.find(1);
        assertThat(miscInDB, equalTo(misc));
    }

    private User createUser(String firstName, String email) {
        User user = new User();
        user.firstName = firstName;
        user.email = email;
        return user;
    }
}
