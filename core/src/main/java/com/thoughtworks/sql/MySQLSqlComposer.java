package com.thoughtworks.sql;

import com.thoughtworks.Model;
import com.thoughtworks.metadata.MetaDataProvider;
import com.google.common.base.Joiner;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MySQLSqlComposer implements SqlComposer {
    private MetaDataProvider metaDataProvider = new MetaDataProvider(); //TODO: ioc


    public String getInsertSQL(Model model) throws SQLException {
        ResultSetMetaData metaData = metaDataProvider.getMetaDataOf(model);

        String columnNames = Joiner.on(",").join(getColumnNames(metaData));
        String values = buildValues(model, metaData);

        return String.format("INSERT INTO users (%s) values (%s)", columnNames, values);
    }

    private String buildValues(Model model, ResultSetMetaData metaData) {
        return "null, 'x@y.z'";
    }

    private List<String> getColumnNames(ResultSetMetaData metaData) throws SQLException {
        List<String> columns= newArrayList();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnName(i));
        }

        return columns;
    }
}
