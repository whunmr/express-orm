package com.thoughtworks.query;

import com.thoughtworks.util.SqlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

public class QueryResult {
    private final ResultSet resultSet;
    private final Statement statement;

    public QueryResult(ResultSet resultSet, Statement statement) {
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void close() {
        SqlUtil.close(resultSet);
        SqlUtil.close(statement);
    }
}
