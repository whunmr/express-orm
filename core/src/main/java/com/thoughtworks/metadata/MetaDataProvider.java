package com.thoughtworks.metadata;

import com.thoughtworks.query.ORMException;
import com.thoughtworks.query.QueryResult;
import com.thoughtworks.query.QueryUtil;

import java.sql.SQLException;

public class MetaDataProvider {
    public static final String META_DATE_QUERY = "SELECT * FROM %s LIMIT 1";

    public ModelMetaData getMetaDataOf(String tableName) {
        String metaDataQuery = String.format(META_DATE_QUERY, tableName);
        QueryResult queryResult = QueryUtil.getQueryResult(metaDataQuery);

        try {
            ModelMetaData modelMetaData = new ModelMetaData(queryResult.getResultSet().getMetaData());
            return modelMetaData;
        } catch (SQLException e) {
            throw new ORMException(e);
        } finally {
            queryResult.close();
        }
    }
}
