package com.thoughtworks.sql;

import com.thoughtworks.Model;

import java.sql.SQLException;

public interface SqlComposer {
    String getInsertSQL(Model model) throws SQLException;
}
