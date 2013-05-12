package com.thoughtworks.metadata;

import com.thoughtworks.query.ORMException;
import com.thoughtworks.queryresult.QueryResult;
import com.thoughtworks.query.QueryUtil;

import java.sql.SQLException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class MetaDataProvider {
    public static final String META_DATE_QUERY = "SELECT * FROM %s LIMIT 1";
    public Map<String, ModelMetaData> cache = newHashMap();

    public ModelMetaData getMetaDataOf(String tableName) {
        if (cache.containsKey(tableName)) {
            return cache.get(tableName);
        }

        String metaDataQuery = String.format(META_DATE_QUERY, tableName);
        QueryResult queryResult = QueryUtil.getQueryResult(metaDataQuery);

        try {
            ModelMetaData modelMetaData = new ModelMetaData(queryResult.getResultSet().getMetaData());
            cache.put(tableName, modelMetaData);
            return modelMetaData;
        } catch (SQLException e) {
            throw new ORMException(e);
        } finally {
            queryResult.close();
        }
    }
}
