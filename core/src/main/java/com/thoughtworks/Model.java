package com.thoughtworks;

import com.thoughtworks.binder.ResultSets;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Model {
    private static NameGuesser guesser = new DefaultNameGuesser();              //TODO: use ioc
    private static SqlComposer sqlComposer = new MySQLSqlComposer();            //TODO: use ioc
    private static MetaDataProvider metaDataProvider = new MetaDataProvider();  //TODO: ioc

    public <T extends Model> T save() {
        //TODO: model has primary key then update
        String insertSQL = sqlComposer.getInsertSQL(this);

        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            statement.executeUpdate(insertSQL);
            //TODO: update primary key value
            return (T)this;
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
        }

        return null;
    }

    public static <T extends Model> T find(Object primaryKey) throws SQLException {
        String sql = sqlComposer.getSelectSQL(modelName(), primaryKey);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> List<T> find_all() throws SQLException {
        //TODO: to String... criteria
        return find_all(null);
    }

    public static <T extends Model> List<T> find_all(String criteria) throws SQLException {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        return executeObjectListQuery(modelName(), sql);
    }

    public static <T extends Model> T find_by_sql(String sql) throws SQLException {
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> T where(String criteria) throws SQLException {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static int delete_all() {
        return delete_all(null);
    }

    public static int delete_all(String criteria) {
        String sql = sqlComposer.getDeleteSQL(modelName(), criteria);
        return executeUpdate(sql);
    }

    public static int delete(Object... primaryKeys) {
        return 0;
    }

    private static int executeUpdate(String sql) {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            return statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();//TODO
        } finally {
            SqlUtil.close(statement);
        }

        return 0;
    }

    private static <T extends Model> T executeSingleObjectQuery(String modelClassName, String querySQL) throws SQLException {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(querySQL);
            return ResultSets.assembleInstanceBy(resultSet, modelClassName);
        } finally {
            SqlUtil.close(statement);
        }
    }

    private static <T extends Model> List<T> executeObjectListQuery(String modelClassName, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSets.assembleInstanceListBy(resultSet, modelClassName);
        } finally {
            SqlUtil.close(statement);
        }
    }

    public String getTableName() {
        return guesser.getTableName(getClass().getSimpleName());
    }

    private static String modelName() {
        //After concrete model class being instrumented, there will be delegate method in
        //the concrete model class. (e.g. save/find)
        //so the call stack trace here will be:
        //[0] = "java.lang.Thread.getStackTrace(Thread.java:1503)"
        //[1] = "com.thoughtworks.Model.getModelClassName(Model.java:52)"
        //[2] = "com.thoughtworks.Model.find(Model.java:32)"
        //[3] = "com.thoughtworks.fixture.User.find(User.java)"
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            String className = stackTraceElement.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                boolean isModelClass = clazz != null && !clazz.equals(Model.class) && Model.class.isAssignableFrom(clazz);
                if (isModelClass) {
                    return className;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        throw new IllegalStateException("Please make sure using the instrument.jar as -javaagent of the running JVM.");
    }
}
