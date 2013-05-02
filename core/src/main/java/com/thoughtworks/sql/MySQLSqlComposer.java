package com.thoughtworks.sql;

import com.thoughtworks.DefaultNameGuesser;
import com.thoughtworks.Model;
import com.thoughtworks.NameGuesser;
import com.thoughtworks.metadata.MetaDataProvider;
import com.google.common.base.Joiner;

import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MySQLSqlComposer implements SqlComposer {
    public static final String ID = "id";
    private MetaDataProvider metaDataProvider = new MetaDataProvider(); //TODO: ioc
    private NameGuesser guesser = new DefaultNameGuesser();             //TODO: ioc

    public String getInsertSQL(Model model) throws SQLException {
        ResultSetMetaData metaData = metaDataProvider.getMetaDataOf(model);

        List<String> columns = getColumnNames(metaData);
        String values = buildValues(model, metaData, columns);

        return String.format("INSERT INTO users (%s) values (%s)", Joiner.on(",").join(columns), values);
    }

    private String buildValues(Model model, ResultSetMetaData metaData, List<String> columnNames) {
        List<String> values = newArrayList();

        for (int i = 0; i < columnNames.size(); i++) {
            Object fieldValue = getFieldValue(model, columnNames.get(i));
            values.add(getFieldStringValue(fieldValue));
        }

        return Joiner.on(",").join(values);
    }

    private String getFieldStringValue(Object fieldValue) {
        return fieldValue == null ? "null" : "'" + fieldValue.toString() + "'";
    }

    private Object getFieldValue(Model model, String columnName) {
        try {
            Field field = model.getClass().getDeclaredField(guesser.getFieldName(columnName));
            field.setAccessible(true);
            return field.get(model);
        } catch (NoSuchFieldException e) {
            if (!ID.equals(columnName)) {
                e.printStackTrace();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
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
