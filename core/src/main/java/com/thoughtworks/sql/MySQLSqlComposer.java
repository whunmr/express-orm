package com.thoughtworks.sql;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.thoughtworks.DefaultNameGuesser;
import com.thoughtworks.Model;
import com.thoughtworks.NameGuesser;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.metadata.ModelMetaData;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class MySQLSqlComposer implements SqlComposer {
    public static final String ID = "id";
    private MetaDataProvider metaDataProvider = new MetaDataProvider(); //TODO: ioc
    private NameGuesser guesser = new DefaultNameGuesser();             //TODO: ioc

    public String getInsertSQL(Model model) {
        ModelMetaData metaData = metaDataProvider.getMetaDataOf(model.getTableName());

        List<String> columns = metaData.getColumnNames();
        String values = buildValues(model, columns);

        return String.format("INSERT INTO users (%s) values (%s)", Joiner.on(",").join(columns), values);
    }

    @Override
    public String getSelectSQL(String modelClassName, Object primaryKey) {
        //TODO:
        return "SELECT * FROM users where id=1";
    }

    @Override
    public String getSelectWithWhereSQL(String modelClassName, String criteria) {
        //TODO:
        if (Strings.isNullOrEmpty(criteria)) {
            return "SELECT * FROM users";
        }

        return "SELECT * FROM users where " + criteria;
    }

    @Override
    public String getDeleteSQL(String modelClassName, String criteria) {
        //TODO:
        if (Strings.isNullOrEmpty(criteria)) {
            return "DELETE FROM users";
        }
        return "DELETE FROM users WHERE " + criteria;
    }

    @Override
    public String getCountSQL(String modelClassName) {
        checkNotNull(modelClassName);

        String tableName = guesser.getTableName(modelClassName);
        return "SELECT COUNT(*) FROM " + tableName;
    }

    private String buildValues(Model model, List<String> columnNames) {
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
}
