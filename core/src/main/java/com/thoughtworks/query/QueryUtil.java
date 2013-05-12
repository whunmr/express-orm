package com.thoughtworks.query;

import com.thoughtworks.DB;
import com.thoughtworks.Model;
import com.thoughtworks.queryresult.QueryResult;
import com.thoughtworks.queryresult.ResultSets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class QueryUtil {
    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
            }
        }
    }

    public static QueryResult getQueryResult(String querySQL)  {
        try {
            Statement statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(querySQL);
            return new QueryResult(resultSet, statement);
        } catch (SQLException e) {
            throw new ORMException(e);
        }
    }

    public static int getIntegerQueryResult(String countSQL) {
        QueryResult queryResult = getQueryResult(countSQL);

        try {
            ResultSet resultSet = queryResult.getResultSet();
            resultSet.next();
            return Integer.valueOf(resultSet.getObject(1).toString());
        } catch (SQLException e) {
            throw new ORMException(e);
        } finally {
            queryResult.close();
        }
    }

    public static int executeUpdate(String sql) {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new ORMException(e);
        } finally {
            close(statement);
        }
    }

    public static <T extends Model> T executeSingleObjectQuery(String modelClassName, String querySQL) {
        QueryResult queryResult = getQueryResult(querySQL);
        T instance = ResultSets.assembleInstanceBy(queryResult.getResultSet(), modelClassName);
        queryResult.close();
        return instance;
    }

    public static <T extends Model> List<T> executeObjectListQuery(String modelClassName, String sql) {
        Statement statement = null;
        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return ResultSets.assembleInstanceListBy(resultSet, modelClassName);
        } catch (Exception e) {
            throw new ORMException(e);
        } finally {
            close(statement);
        }
    }
}
