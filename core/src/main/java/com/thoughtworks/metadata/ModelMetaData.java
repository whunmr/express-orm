package com.thoughtworks.metadata;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ModelMetaData {
    private List<String> columnNames;

    public ModelMetaData(ResultSetMetaData metaData) {
        columnNames = getColumnNamesFrom(metaData);
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    private List<String> getColumnNamesFrom(ResultSetMetaData metaData) {
        List<String> columns= newArrayList();

        try {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columns.add(metaData.getColumnName(i));
            }
        } catch (SQLException e) {
        }

        return columns;
    }
}
