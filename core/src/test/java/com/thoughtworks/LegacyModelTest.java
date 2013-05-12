package com.thoughtworks;

import com.thoughtworks.fixture.Article;
import com.thoughtworks.fixture.LegacyUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LegacyModelTest extends BaseDBTest {
    private LegacyUser userA;
    private LegacyUser userB;
    private LegacyUser userC;
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
        LegacyUser.delete_all();
        Article.delete_all();
    }

    @Test
    public void should_able_to_reveal_table_name_through_annotation_for_legacy_code() {
        LegacyUser user = createUser("Z", "z@z.z");
        LegacyUser savedUser = user.save();
        assertThat(savedUser, is(sameInstance(user)));
        assertThat(savedUser.getId(), is(greaterThan(0)));
    }

    @Test
    public void should_able_to_support_limit_in_find_all() {
        List<LegacyUser> users = LegacyUser.find_all().limit(1);
        assertThat(users, containsInAnyOrder(userA));
    }

    private LegacyUser createUser(String firstName, String email) {
        LegacyUser user = new LegacyUser();
        user.__firstName = firstName;
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
