package com.thoughtworks;

import com.thoughtworks.naming.DefaultNameGuesser;
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
        assertThat(guesser.getFieldName("email"), is("email"));
        assertThat(guesser.getFieldName("first_name"), is("firstName"));
        assertThat(guesser.getFieldName("last_name"), is("lastName"));
        assertThat(guesser.getFieldName("f_name"), is("fName"));
    }
}
