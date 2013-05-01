package com.thoughtworks;

import com.thoughtworks.exceptions.InitException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private Properties properties = new Properties();

    public Configuration() {
        InputStream in = getClass().getResourceAsStream("express-orm.properties");
        if (in != null) {
            try {
                properties.load(in);
            } catch (Exception e) {
                //throw new InitException(e);
            }
        }
    }
}
