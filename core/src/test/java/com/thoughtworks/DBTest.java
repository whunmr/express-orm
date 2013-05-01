package com.thoughtworks;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DBTest {
    private Connection connection;
    private DB db = new DB();

    @Test
    public void should_be_able_to_load_connection_parameters_from_database_properties_file()
            throws SQLException, ClassNotFoundException, IOException {

        connection = db.getConnection();
        assertThat(connection, is(notNullValue()));
        assertThat(connection.isClosed(), is(false));
    }

    @Test
    public void should_be_able_to_return_same_connection_for_same_thread()
            throws ClassNotFoundException, IOException, SQLException {

        connection = db.getConnection();
        Connection connection1 = db.getConnection();
        assertThat(connection, is(sameInstance(connection1)));
    }

    @Test
    public void should_return_different_connection_for_different_thread()
            throws ClassNotFoundException, IOException, SQLException, InterruptedException, ExecutionException {

        connection = db.getConnection();

        Callable<Connection> task = new Callable<Connection>() {
            @Override
            public Connection call() throws Exception {
                return db.getConnection();
            }
        };

        List<Future<Connection>> futures = Executors.newFixedThreadPool(1).invokeAll(Arrays.asList(task));
        Connection connectionInNewThread = futures.get(0).get();

        assertThat(connection, not(sameInstance(connectionInNewThread)));
    }

    @After
    public void tearDown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }
}
