package com.thoughtworks;

import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.sql.Connection;
import java.sql.Statement;

public class Model {
    private NameGuesser guesser = new DefaultNameGuesser();     //TODO: use ioc
    private SqlComposer sqlComposer = new MySQLSqlComposer();   //TODO: use ioc

    public boolean save() {
        try {
            Connection connection = DB.connection();
            Statement statement = connection.createStatement();
            String insertSQL = sqlComposer.getInsertSQL(this);
            statement.execute(insertSQL);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getTableName() {
        return guesser.getTableName(getClass().getSimpleName());
    }

}
