package com.thoughtworks.metadata;

import com.thoughtworks.DB;
import com.thoughtworks.util.SqlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

public class MetaDataProvider {
    public static final String META_DATE_QUERY = "SELECT * FROM %s LIMIT 1";

    public ModelMetaData getMetaDataOf(String tableName) {
        //TODO: add cache
        String metaDataQuery = String.format(META_DATE_QUERY, tableName);
        Statement statement = null;

        try {
            statement = DB.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(metaDataQuery);
            return new ModelMetaData(resultSet.getMetaData());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.close(statement);
        }

        return null;
    }
}
