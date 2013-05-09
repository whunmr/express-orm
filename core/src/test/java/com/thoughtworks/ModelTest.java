package com.thoughtworks;

import com.thoughtworks.fixture.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ModelTest extends BaseDBTest{
    private User userA;
    private User userB;
    private User userC;
    private Article articleA;

    @Before
    public void setUp() throws ClassNotFoundException, IOException, SQLException {
        super.setUp();
        truncateTable("users");
        userA = createUser("A", "a@a.a").save();
        userB = createUser("B", "b@b.b").save();
        userC = createUser("C", "c@c.c").save();
        articleA = createArticle(userA.getId()).save();
    }

    @After
    public void tearDown() {
        super.tearDown();
        User.delete_all();
        Article.delete_all();
    }

    @Test
    public void should_able_to_save_object_to_table() {
        User user = createUser("Z", "z@z.z");
        User savedUser = user.save();
        assertThat(savedUser, is(sameInstance(user)));
        assertThat(savedUser.getId(), is(greaterThan(0)));
    }

    @Test
    public void should_able_to_update_object_by_save_method() {
        userA.email = "newA@newA.newA";

        userA.save();

        User updatedUserInDB = User.find_first("email = 'newA@newA.newA'");
        assertThat(updatedUserInDB, is(equalTo(userA)));
    }

    @Test
    public void should_able_to_find_all_records_by_empty_where_clause() {
        List<User> users = User.find_all();
        assertThat(users, containsInAnyOrder(userA, userB, userC));
    }

    @Test
    public void should_able_to_support_limit_in_find_all() {
        List<User> users = User.find_all().limit(1);
        assertThat(users, containsInAnyOrder(userA));
    }

    @Test
    public void should_able_to_support_offset_in_find_all() {
        List<User> users = User.find_all().limit(2).offset(1);
        assertThat(users, containsInAnyOrder(userB, userC));
    }

    @Test
    public void should_able_to_find_object_in_table_by_primary_key() throws ClassNotFoundException, IOException, SQLException {
        truncateTable("users");
        User user = createUser("A", "a@a.a").save();

        User userInDB = User.find_by_id(1);

        assertThat(userInDB, equalTo(user));
    }

    @Test
    public void should_able_to_find_all_one_to_many_records_of_instance() {
        List<Article> articlesOfUserA = userA.find_all(Article.class);
        assertThat(articlesOfUserA, containsInAnyOrder(articleA));
    }

    @Test
    public void should_able_to_eager_loading_during_find_all() {
        List<User> users = User.find_all().includes(Article.class);
        assertThat(newArrayList(users.get(0).articles), contains(articleA));
    }

    @Test
    public void should_able_to_eager_loading_during_find_all_with_criteria() {
        List<User> users = User.find_all("id = 1").includes(Article.class);
        assertThat(newArrayList(users.get(0).articles), contains(articleA));
    }

    @Test
    public void should_able_to_find_object_by_where_clause() {
        User userInDB = User.find_first("email = 'a@a.a'");
        assertThat(userInDB, equalTo(userA));
    }

    @Test
    public void should_able_to_find_record_object_by_sql() {
        User userInDB = User.find_by_sql("SELECT * FROM users WHERE email = 'a@a.a'");
        assertThat(userInDB, equalTo(userA));
    }

    @Test
    public void should_able_to_select_columns_of_record_by_sql() {
        User userInDB = User.find_by_sql("SELECT email FROM users WHERE email = 'a@a.a'");
        assertThat(userInDB.firstName, is(nullValue()));
        assertThat(userInDB.email, is(userA.email));
    }

    @Test
    public void should_able_to_get_row_count_in_table() {
        int recordsCount = User.count();
        assertThat(recordsCount, is(User.find_all().size()));
    }

    @Test
    public void should_able_to_delete_record_by_primary_key() {
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        int rowDeleted = userA.delete();
        assertThat(rowDeleted, equalTo(1));
        assertThat(User.<User>find_all(), containsInAnyOrder(userB, userC));
    }

    @Test
    public void should_able_to_delete_object() {
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        userA.delete();
        assertThat(User.<User>find_all(), containsInAnyOrder(userB, userC));
    }

    @Test
    public void should_able_to_delete_multiple_records_by_primary_key_array() {
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        User.delete(new Object[] {1, 3});
        assertThat(User.<User>find_all(), containsInAnyOrder(userB));
    }

    @Test
    public void should_able_to_delete_record_by_where_criteria() {
        assertThat(User.<User>find_all(), containsInAnyOrder(userA, userB, userC));
        User.delete_all("email = 'a@a.a'");
        assertThat(User.<User>find_all(), containsInAnyOrder(userB, userC));
    }

    @Test
    public void should_able_to_delete_all_records_by_empty_where_criteria() {
        User.delete_all();
        assertThat(User.count(), is(0));
    }

    @Test
    public void should_able_to_mapping_primitive_members_to_db_columns() {
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

        Misc miscInDB = Misc.find_by_id(1);
        assertThat(miscInDB, equalTo(misc));
    }

    @Test
    public void should_able_to_rollback_transaction_when_error_occurs() throws SQLException {
        try {
            new DummyService().saveUserInTransactionAndExceptionOccurs(createUser("Y", "y@y.y"));
        } catch (Exception e) {

            reconnect_database_to_avoid_seeing_staled_data();

            Boolean userSaveRollBacked = User.find_first("first_name = 'Y'") == null;
            assertThat(userSaveRollBacked, is(true));
        }
    }

    @Test
    public void should_able_to_commit_transaction_when_no_error_occurs() throws SQLException {
        new DummyService().saveUserInTransactionAndNoExceptionOccurs(createUser("Y", "y@y.y"));

        reconnect_database_to_avoid_seeing_staled_data();

        Boolean transactionAutoCommitted = User.find_first("first_name = 'Y'") != null;
        assertThat(transactionAutoCommitted, is(true));
    }

    private void reconnect_database_to_avoid_seeing_staled_data() throws SQLException {
        DB.connection().close();
    }

    private User createUser(String firstName, String email) {
        User user = new User();
        user.firstName = firstName;
        user.email = email;
        return user;
    }

    private Article createArticle(int id) {
        Article article = new Article();
        article.userId = id;
        article.title = "_title";
        article.content = "_content";
        return article;
    }
}
