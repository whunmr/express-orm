package com.thoughtworks.metadata;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ModelMetaData {
    private final ResultSetMetaData metaData;

    public ModelMetaData(ResultSetMetaData metaData) {
        this.metaData = metaData;
    }

    public List<String> getColumnNames() {
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
