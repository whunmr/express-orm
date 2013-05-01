package com.thoughtworks;

import com.thoughtworks.exceptions.DBInitException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class DB {
    private static ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
    private String dbName;

    public DB(String dbName) {
        this.dbName = dbName;
    }

    public DB() {
        this.dbName = "test";
    }

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

    private void checkExistingConnection(String dbName) {
        if (ConnectionManager.getConnections().containsKey(dbName)) {
            throw new DBInitException("Cannot open a new connection because existing connection is still on current thread, dbName: " + dbName + ", connection instance: " + ConnectionManager.getConnections().get(dbName)
                    + ". This might indicate a logical error in your application.");
        }
    }

    public static Connection getConnection() throws IOException, ClassNotFoundException, SQLException {
        if (connection.get() != null) {
            return connection.get();
        }

        InputStream inputStream = DB.class.getResourceAsStream("/config/database.properties");
        Properties properties = new Properties();

        properties.load(inputStream);
        String driver = checkNotNull(properties.getProperty("jdbc.driver"), "jdbc.driver can not be null");
        String url = checkNotNull(properties.getProperty("jdbc.url"), "jdbc.url can not be null");

        Class.forName(driver);
        connection.set(DriverManager.getConnection(url, properties));

        return connection.get();
    }
}
