package com.thoughtworks;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class DB {
    private static ThreadLocal<Connection> connection = new ThreadLocal<Connection>();

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
