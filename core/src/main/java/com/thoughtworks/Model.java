package com.thoughtworks;

import com.thoughtworks.binder.ResultSets;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Model {
    private static NameGuesser guesser = new DefaultNameGuesser();              //TODO: use ioc
    private static SqlComposer sqlComposer = new MySQLSqlComposer();            //TODO: use ioc

    public <T extends Model> T save() throws SQLException {
        //TODO: model has primary key then update
        String insertSQL = sqlComposer.getInsertSQL(this);

        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            statement.executeUpdate(insertSQL);
            //TODO: update primary key value
            return (T)this;
        } finally {
            SqlUtil.close(statement);
        }
    }

    public static <T extends Model> T find(Object primaryKey) throws SQLException {
        String sql = sqlComposer.getSelectSQL(modelName(), primaryKey);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> List<T> find_all() throws SQLException {
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

    public static int count() throws SQLException {
        String countSQL = sqlComposer.getCountSQL(modelName());
        QueryResult queryResult = getQueryResult(countSQL);

        try {
            ResultSet resultSet = queryResult.getResultSet();
            resultSet.next();
            return Integer.valueOf(resultSet.getObject(1).toString());
        } finally {
            queryResult.close();
        }
    }

    public static int delete_all() throws SQLException {
        return delete_all(null);
    }

    public static int delete_all(String criteria) throws SQLException {
        String deleteAllSQL = sqlComposer.getDeleteSQL(modelName(), criteria);
        return executeUpdate(deleteAllSQL);
    }

    public static int delete(Object... primaryKeys) throws SQLException {
        checkNotNull(primaryKeys);
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), primaryKeys);
        return executeUpdate(deleteInSQL);
    }

    private static int executeUpdate(String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            return statement.executeUpdate(sql);
        } finally {
            SqlUtil.close(statement);
        }
    }

    private static <T extends Model> T executeSingleObjectQuery(String modelClassName, String querySQL) throws SQLException {
        QueryResult queryResult = getQueryResult(querySQL);
        T instance = ResultSets.assembleInstanceBy(queryResult.getResultSet(), modelClassName);
        queryResult.close();
        return instance;
    }

    private static QueryResult getQueryResult(String querySQL) throws SQLException {
        Statement statement = DB.connection().createStatement();
        ResultSet resultSet = statement.executeQuery(querySQL);
        return new QueryResult(resultSet, statement);
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

        throw new IllegalStateException("Please make sure using the instrument.jar as -javaagent of the running JVM. " +
                "refer https://github.com/whunmr/express-orm/blob/master/README.md for details");
    }

    static class QueryResult {
        private final ResultSet resultSet;
        private final Statement statement;

        QueryResult(ResultSet resultSet, Statement statement) {
            this.resultSet = resultSet;
            this.statement = statement;
        }

        public ResultSet getResultSet() {
            return resultSet;
        }

        void close() {
            SqlUtil.close(resultSet);
            SqlUtil.close(statement);
        }
    }
}
