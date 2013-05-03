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

    public static <T extends Model> T find(Object primaryKey) {
        String modelClassName = getModelClassName();



        try {
            return (T) Class.forName(modelClassName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getModelClassName() {
        //After concrete model class being instrumented, there will be delegate method in
        //the concrete model class. (e.g. save/find)
        //so the call stack trace here will be:
        //[0] = "java.lang.Thread.getStackTrace(Thread.java:1503)"
        //[1] = "com.thoughtworks.Model.getModelClassName(Model.java:52)"
        //[2] = "com.thoughtworks.Model.find(Model.java:32)"
        //[3] = "com.thoughtworks.fixture.User.find(User.java)"

        return Thread.currentThread().getStackTrace()[3].getClassName();
    }
}
