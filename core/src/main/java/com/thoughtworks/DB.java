package com.thoughtworks;

import com.thoughtworks.exceptions.DBCloseException;
import com.thoughtworks.exceptions.DBInitException;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
    private String dbName;

    public void open(String driver, String url, String user, String password) {
        checkExistingConnection(dbName);
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, password);
            ConnectionManager.attach(dbName, connection);
        } catch (Exception e) {
            throw new DBInitException();
        }
    }

    public void close() {
        try {
            Connection connection = ConnectionManager.getConnections().get(dbName);
            if (connection == null) {
                throw new DBCloseException("cannot close connection '" + dbName + "' because it is not available");
            } else {
                connection.close();
                System.out.println("Closed connection: " + connection);
                ConnectionManager.detach(dbName);
            }
        } catch (Exception e) {
            System.out.println("Could not close connection: " + e);
        }
    }

    private void checkExistingConnection(String dbName) {
        if (ConnectionManager.getConnections().containsKey(dbName)) {
            throw new DBInitException("Could not open a new connection because existing connection is still on current thread");
        }
    }
}
