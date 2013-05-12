package com.thoughtworks;

import com.thoughtworks.fixture.DummyService;
import com.thoughtworks.fixture.User;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TransactionSupportTest extends BaseDBTest{
    private User user;

    @Before
    public void setUp() {
        user = createUser("Y", "y@y.y");
    }

    @Test
    public void should_able_to_rollback_transaction_when_error_occurs() throws SQLException {
        try {
            new DummyService().saveUserInTransactionAndExceptionOccurs(user);
        } catch (Exception e) {
            reconnect_database_to_avoid_seeing_staled_data();

            Boolean transactionAutoRollbacked = User.find_first("first_name = 'Y'") == null;
            assertThat(transactionAutoRollbacked, is(true));
        }
    }

    @Test
    public void should_able_to_commit_transaction_when_no_error_occurs() throws SQLException {
        new DummyService().saveUserInTransactionAndNoExceptionOccurs(user);

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
}
