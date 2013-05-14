package com.thoughtworks.queryresult;

import com.expressioc.utility.ClassUtility;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.thoughtworks.Model;
import com.thoughtworks.query.ORMException;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.query.naming.DefaultNameGuesser;
import com.thoughtworks.query.naming.NameGuesser;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ResultSets {
    private static NameGuesser guesser = new DefaultNameGuesser();
    private static MetaDataProvider metaDataProvider = new MetaDataProvider();

    public static <T extends Model> List<T> assembleInstanceListBy(ResultSet resultSet, String modelClassName) throws SQLException {
        List<T> objList = newArrayList();
        List<String> columns = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName)).getColumnNames();

        while (resultSet.next()) {
            List<Object> values = getColumnValuesFrom(resultSet, columns);
            objList.add(ResultSets.<T>assembleInstanceFrom(values, columns, modelClassName));
        }

        return objList;
    }

    public static <T extends Model> T assembleInstanceBy(final ResultSet resultSet, String modelClassName) {
        try {
            if (!resultSet.next()) {
                return null;
            }
        } catch (SQLException e) {
            throw new ORMException(e);
        }

        List<String> columns = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName)).getColumnNames();

        List<Object> values = getColumnValuesFrom(resultSet, columns);

        return assembleInstanceFrom(values, columns, modelClassName);
    }

    private static List<Object> getColumnValuesFrom(final ResultSet resultSet, List<String> columns) {
        return Lists.transform(columns, new Function<String, Object>() {
            @Override
            public Object apply(String column) {
                try {
                    return resultSet.getObject(column);
                } catch (SQLException e) {
                    return null;
                }
            }
        });
    }


    private static <T extends Model> T assembleInstanceFrom(List<Object> columnValues, List<String> columns, String modelClassName) {
        T instance = getNewInstance(modelClassName);
        Class<? extends Model> modelClass = instance.getClass();
        String column = null;
        Object columnValue = null;

        for (int i = 0; i < columns.size(); i++) {
            try {
                column = columns.get(i);
                columnValue = columnValues.get(i);
                setFieldValue(instance, column, columnValue, modelClass);
            } catch (NoSuchFieldException e) {
                try {
                    setFieldValue(instance, column, columnValue, modelClass.getSuperclass());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
            }
        }

        return instance;
    }

    private static <T extends Model> void setFieldValue(T instance, String column, Object columnValue, Class<?> modelClass) throws Exception {
        try {
            Field field = modelClass.getDeclaredField(guesser.getFieldName(modelClass, column));
            field.setAccessible(true);
            field.set(instance, getTypedValue(columnValue, field.getType()));
        } catch (IllegalAccessException e) {
        }
    }

    private static Object getTypedValue(Object columnValue, Class<?> clazz) throws Exception {
        if (columnValue == null) {
            return null;
        }

        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return !columnValue.toString().equals("0");
        }

        return ClassUtility.assembleParameter(columnValue.toString(), clazz);
    }

    private static <T extends Model> T getNewInstance(String modelClassName) {
        try {
            return (T) Class.forName(modelClassName).newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        }

        return null;
    }

    public static <T extends Model> T assembleInstanceBy(String modelClassName, String... params) {
        ArrayList<String> columns = newArrayList();
        ArrayList<Object> columnValues = newArrayList();

        for (int i = 0; i < params.length; i += 2) {
            columns.add(params[i]);
            columnValues.add(params[i + 1]);
        }

        return assembleInstanceFrom(columnValues, columns, modelClassName);
    }
}
