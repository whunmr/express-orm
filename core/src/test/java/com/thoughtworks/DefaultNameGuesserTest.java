package com.thoughtworks;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultNameGuesserTest {
    private DefaultNameGuesser defaultNameGuesser;

    @Before
    public void setUp() {
        defaultNameGuesser = new DefaultNameGuesser();
    }

    @Test
    public void should_return_correct_underscored_tableName() {
        assertThat(defaultNameGuesser.getTableName("User"), is("users"));
        assertThat(defaultNameGuesser.getTableName("UserComment"), is("user_comments"));
        assertThat(defaultNameGuesser.getTableName("UserCommentTag"), is("user_comment_tags"));
    }
}
