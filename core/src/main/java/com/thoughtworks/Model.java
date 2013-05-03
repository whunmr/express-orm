package com.thoughtworks;

import com.thoughtworks.binder.ResultSets;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.sql.ResultSet;
import java.sql.Statement;

public class Model {
    private static NameGuesser guesser = new DefaultNameGuesser();              //TODO: use ioc
    private static SqlComposer sqlComposer = new MySQLSqlComposer();            //TODO: use ioc
    private static MetaDataProvider metaDataProvider = new MetaDataProvider();  //TODO: ioc

    public <T extends Model> T save() {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            statement.execute(sqlComposer.getInsertSQL(this));
            //TODO: update primary key value
            return (T)this;
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
        }

        return null;
    }

    public String getTableName() {
        return guesser.getTableName(getClass().getSimpleName());
    }

    public static <T extends Model> T find(Object primaryKey) {
        String modelClassName = getModelClassName();
        Statement statement = null;

        try {
            String selectSQL = sqlComposer.getSelectSQL(modelClassName, primaryKey);
            statement = DB.connection().createStatement();
            ResultSet rs = statement.executeQuery(selectSQL);
            return ResultSets.assembleInstanceBy(rs, modelClassName);
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
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

    public static <T extends Model> T where(String criteria) {
        String modelClassName = getModelClassName();
        Statement statement = null;

        try {
            String selectSQL = sqlComposer.getSelectWithWhereSQL(modelClassName, criteria);
            statement = DB.connection().createStatement();
            ResultSet rs = statement.executeQuery(selectSQL);
            return ResultSets.assembleInstanceBy(rs, modelClassName);
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
        }

        return null;
    }
}
