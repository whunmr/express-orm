package com.thoughtworks.query;

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
        QueryUtil.close(resultSet);
        QueryUtil.close(statement);
    }
}
