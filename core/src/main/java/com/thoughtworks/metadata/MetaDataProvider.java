package com.thoughtworks.metadata;

import com.thoughtworks.DB;
import com.thoughtworks.Model;
import com.thoughtworks.SqlUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class MetaDataProvider {
    public static final String META_DATE_QUERY = "SELECT * FROM %s LIMIT 1";

    public ResultSetMetaData getMetaDataOf(Model model) {
        String metaDataQuery = String.format(META_DATE_QUERY, model.getTableName());
        Statement statement = null;

        try {
            statement = DB.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(metaDataQuery);
            return resultSet.getMetaData();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.safeClose(statement);
        }

        return null;
    }
}
