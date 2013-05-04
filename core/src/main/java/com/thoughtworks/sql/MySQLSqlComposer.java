package com.thoughtworks.sql;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.thoughtworks.Model;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.metadata.ModelMetaData;
import com.thoughtworks.naming.DefaultNameGuesser;
import com.thoughtworks.naming.NameGuesser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class MySQLSqlComposer implements SqlComposer {
    public static final String ID = "id";
    private MetaDataProvider metaDataProvider = new MetaDataProvider(); //TODO: ioc
    private NameGuesser guesser = new DefaultNameGuesser();             //TODO: ioc

    public String getInsertSQL(Model model) {
        ModelMetaData metaData = metaDataProvider.getMetaDataOf(model.getTableName());

        List<String> columns = metaData.getColumnNames();
        String values = Joiner.on(",").join(getValuesList(model, columns));

        return String.format("INSERT INTO %s (%s) values (%s)", model.getTableName(), Joiner.on(",").join(columns), values);
    }

    @Override
    public String getSelectSQL(String modelClassName, Object primaryKey) {
        return "SELECT * FROM " + guesser.getTableName(modelClassName) + " WHERE id=" + primaryKey.toString();
    }

    @Override
    public String getSelectWithWhereSQL(String modelClassName, String criteria) {
        return getWhereSQL(modelClassName, criteria, "SELECT * FROM %s");
    }

    private String getWhereSQL(String modelClassName, String criteria, String basicSQL) {
        String selectAllSQL = String.format(basicSQL, guesser.getTableName(modelClassName));
        if (Strings.isNullOrEmpty(criteria)) {
            return selectAllSQL;
        }

        return String.format("%s WHERE %s", selectAllSQL, criteria);
    }

    @Override
    public String getDeleteSQL(String modelClassName, String criteria) {
       return getWhereSQL(modelClassName, criteria, "DELETE FROM %s");
    }

    @Override
    public String getCountSQL(String modelClassName) {
        checkNotNull(modelClassName);

        String tableName = guesser.getTableName(modelClassName);
        return "SELECT COUNT(*) FROM " + tableName;
    }

    @Override
    public String getDeleteInSQL(String modelClassName, Object... primaryKeys) {
        String tableName = guesser.getTableName(modelClassName);
        return "DELETE FROM " + tableName + " WHERE id IN (" + Joiner.on(",").join(primaryKeys) + ")";
    }

    @Override
    public String getLastInsertIdSQL() {
        return "SELECT LAST_INSERT_ID()";
    }

    @Override
    public String getUpdateSQL(Model model) {
        Map<String, String> valuesMap = buildValuesMap(model);
        String valuesToSet = Joiner.on(",").withKeyValueSeparator("=").join(valuesMap);
        String format = String.format("UPDATE %s SET %s WHERE id = %s", model.getTableName(), valuesToSet, model.getId());

        return format;
    }

    private Map<String, String> buildValuesMap(Model model) {
        ModelMetaData metaData = metaDataProvider.getMetaDataOf(model.getTableName());
        List<String> columns = metaData.getColumnNames();
        List<String> valuesList = getValuesList(model, columns);

        Map<String, String> valuesMap = newHashMap();
        for (int i = 1; i < columns.size(); i++) {
            valuesMap.put(columns.get(i), valuesList.get(i));
        }

        return valuesMap;
    }

    private List<String> getValuesList(Model model, List<String> columnNames) {
        List<String> values = newArrayList();

        for (int i = 0; i < columnNames.size(); i++) {
            Object fieldValue = getFieldValue(model, columnNames.get(i));
            values.add(getFieldStringValue(fieldValue));
        }

        return values;
    }

    private String getFieldStringValue(Object fieldValue) {
        if (fieldValue == null) {
            return "null";
        }

        Class<? extends Object> fieldClass = fieldValue.getClass();
        if (fieldClass == Boolean.class || fieldClass == boolean.class) {
            return Boolean.valueOf(fieldValue.toString()) ? "1" : "0";
        }

        return "'" + fieldValue.toString() + "'";
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
