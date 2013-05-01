package com.thoughtworks;

import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.sql.Connection;
import java.sql.Statement;

public class Model {
    private NameGuesser guesser = new DefaultNameGuesser();     //TOOD: use ioc
    private SqlComposer sqlComposer = new MySQLSqlComposer();   //TOOD: use ioc

    public boolean save() {
        try {
            Connection connection = DB.getConnection();
            Statement statement = connection.createStatement();
            String insertSQL = sqlComposer.getInsertSQL(this);
            System.out.println(insertSQL);
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
