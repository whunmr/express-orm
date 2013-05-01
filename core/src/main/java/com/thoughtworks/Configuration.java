package com.thoughtworks;

import com.thoughtworks.exceptions.InitException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private Properties properties = new Properties();

    public Configuration() {
        InputStream in = getClass().getResourceAsStream("/express-orm.properties");
        if (in != null) {
            try {
                properties.load(in);
            } catch (Exception e) {
                throw new InitException();
            }
        }
    }

    public String getDriverName() {
        String driver = properties.getProperty("jdbc.driver");
        return driver != null ? driver : null;
    }

    public String getURL() {
        String url = properties.getProperty("jdbc.url");
        return url != null ? url : null;
    }

    public String getUser() {
        String user = properties.getProperty("jdbc.user");
        return user != null ? user : null;
    }

    public String getPassword() {
        String password = properties.getProperty("jdbc.password");
        return password != null ? password : null;
    }

    public String getDB() {
        String dbName = properties.getProperty("db");
        return dbName != null ? dbName : null;
    }
}
