package com.thoughtworks;

import com.thoughtworks.query.QueryContext;
import com.thoughtworks.query.QueryResult;
import com.thoughtworks.sql.MySQLSqlComposer;
import com.thoughtworks.sql.SqlComposer;
import com.thoughtworks.util.SqlUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Model {
    private static SqlComposer sqlComposer = new MySQLSqlComposer();
    private int id;

    private static ThreadLocal<QueryContext> queryContext = new ThreadLocal<QueryContext>() {
        @Override
        protected QueryContext initialValue() {
            return new QueryContext();
        }
    };

    public <T extends Model> T save() throws SQLException {
        boolean isNewRecord = this.id == 0;
        if (isNewRecord) {
            executeUpdate(sqlComposer.getInsertSQL(this));
            this.id = getIntegerQueryResult(sqlComposer.getLastInsertIdSQL());
        } else {
            executeUpdate(sqlComposer.getUpdateSQL(this));
        }

        return (T) this;
    }

    public static <T extends Model> T find_by_id(Object primaryKey) throws SQLException {
        String sql = sqlComposer.getSelectSQL(modelName(), primaryKey);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> QueryList<T> find_all() throws SQLException {
        return new QueryList<T>(getModelClass());
    }

    public static <T extends Model> QueryList<T> find_all(String criteria) throws SQLException {
        return new QueryList<T>(getModelClass(), criteria);
    }

    public static <T extends Model> T find_by_sql(String sql) throws SQLException {
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static <T extends Model> T find_first(String criteria) throws SQLException {
        String sql = sqlComposer.getSelectWithWhereSQL(modelName(), criteria);
        return executeSingleObjectQuery(modelName(), sql);
    }

    public static int count() throws SQLException {
        String countSQL = sqlComposer.getCountSQL(modelName());
        return getIntegerQueryResult(countSQL);
    }

    private static int getIntegerQueryResult(String countSQL) throws SQLException {
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

    public int delete() throws SQLException {
        String deleteInSQL = sqlComposer.getDeleteInSQL(modelName(), this.getId());
        return executeUpdate(deleteInSQL);
    }

    public static int delete(Object[] primaryKeys) throws SQLException {
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

    public String getTableName() {
        return QueryList.guesser.getTableName(getClass().getSimpleName());
    }

    public static void includes(Class eagerLoadClass) {
        queryContext.get().addEagerLoadingModels(getModelClass(), eagerLoadClass);
    }

    private static Class getModelClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            String className = stackTraceElement.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                boolean isModelClass = clazz != null && !clazz.equals(Model.class) && Model.class.isAssignableFrom(clazz);
                if (isModelClass) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        throw new IllegalStateException("Please make sure using the instrument.jar as -javaagent of the running JVM. " +
                "refer https://github.com/whunmr/express-orm/blob/master/README.md for details");
    }

    private static String modelName() {
        return getModelClass().getName();
    }

    public int getId() {
        return id;
    }

    public <T extends Model> List<T> find_all(Class<T> theManyClass) throws SQLException {
        String sql = sqlComposer.getTheManysSQLInOne2ManyAssociation(theManyClass, this);
        return QueryList.executeObjectListQuery(theManyClass, sql);
    }

}
