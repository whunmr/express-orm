package com.thoughtworks;

import com.thoughtworks.fixture.User;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ModelTest extends BaseDBTest{
    @Test
    public void should_able_to_save_object_to_table() {
        User user = new User();
        user.setEmail("a@b.c");

        boolean save_succeeded = user.save();
        assertThat(save_succeeded, is(true));
        //TODO assertFind
    }


}
