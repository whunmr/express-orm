package com.thoughtworks;

import com.thoughtworks.fixture.User;
import com.thoughtworks.query.naming.DefaultNameGuesser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultNameGuesserTest {
    private DefaultNameGuesser guesser;

    @Before
    public void setUp() {
        guesser = new DefaultNameGuesser();
    }

    @Test
    public void should_able_to_get_table_name_from_simple_class_name() {
        assertThat(guesser.getTableName("User"), is("users"));
        assertThat(guesser.getTableName("UserComment"), is("user_comments"));
        assertThat(guesser.getTableName("UserCommentTag"), is("user_comment_tags"));
    }

    @Test
    public void should_able_to_get_table_name_from_class_name_with_package() {
        assertThat(guesser.getTableName("com.thoughtworks.fixture.User"), is("users"));
    }

    @Test
    public void should_convert_to_correct_field_name_from_table_column_name() {
        assertThat(guesser.getFieldName(User.class, "email"), is("email"));
        assertThat(guesser.getFieldName(User.class, "first_name"), is("firstName"));
        assertThat(guesser.getFieldName(User.class, "last_name"), is("lastName"));
        assertThat(guesser.getFieldName(User.class, "f_name"), is("fName"));
    }
}
