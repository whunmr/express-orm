package com.thoughtworks;

import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.metadata.ModelMetaData;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Model {
    private static NameGuesser guesser = new DefaultNameGuesser();     //TODO: use ioc
    private static SqlComposer sqlComposer = new MySQLSqlComposer();          //TODO: use ioc
    private static MetaDataProvider metaDataProvider = new MetaDataProvider(); //TODO: ioc

    public boolean save() {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            statement.execute(sqlComposer.getInsertSQL(this));
            return true;
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
        }

        return false;
    }

    public String getTableName() {
        return guesser.getTableName(getClass().getSimpleName());
    }

    public static <T extends Model> T find(Object primaryKey) {
        String modelClassName = getModelClassName();

        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet rs = statement.executeQuery(sqlComposer.getSelectSQL(modelClassName, primaryKey));
            return assembleInstanceBy(rs, modelClassName);
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
        }

        return null;
    }

    private static <T extends Model> T assembleInstanceBy(ResultSet rs, String modelClassName) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        T instance = getNewInstance(modelClassName);
        ModelMetaData metaData = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName));
        List<String> columns = metaData.getColumnNames();
        for (String column : columns) {
            Object value = rs.getObject(column);

            try {
                Field field = instance.getClass().getDeclaredField(column);
                field.setAccessible(true);
                field.set(instance, value);
            } catch (NoSuchFieldException e) {
                System.out.println(e.toString());
                continue;
            } catch (IllegalAccessException e) {
            }
        }
        return instance;
    }

    private static <T extends Model> T getNewInstance(String modelClassName) {
        try {
            return (T) Class.forName(modelClassName).newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
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
