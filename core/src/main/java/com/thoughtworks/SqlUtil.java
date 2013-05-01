package com.thoughtworks;

import java.sql.SQLException;
import java.sql.Statement;

public class SqlUtil {
    public static void safeClose(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
    }
}
