package com.thoughtworks.query;

import com.expressioc.utility.ClassUtility;
import com.thoughtworks.Model;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.naming.DefaultNameGuesser;
import com.thoughtworks.naming.NameGuesser;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ResultSets {
    private static NameGuesser guesser = new DefaultNameGuesser();
    private static MetaDataProvider metaDataProvider = new MetaDataProvider();

    public static <T extends Model> List<T> assembleInstanceListBy(ResultSet resultSet, String modelClassName) throws SQLException {
        List<T> objList = newArrayList();
        List<String> columns = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName)).getColumnNames();

        while (resultSet.next()) {
            objList.add(ResultSets.<T>assembleInstanceFrom(resultSet, columns, modelClassName));
        }

        return objList;
    }

    public static <T extends Model> T assembleInstanceBy(ResultSet resultSet, String modelClassName) {
        try {
            if (!resultSet.next()) {
                return null;
            }
        } catch (SQLException e) {
            throw new ORMException(e);
        }

        List<String> columns = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName)).getColumnNames();
        return assembleInstanceFrom(resultSet, columns, modelClassName);
    }


    private static <T extends Model> T assembleInstanceFrom(ResultSet resultSet, List<String> columns, String modelClassName) {
        T instance = getNewInstance(modelClassName);
        Class<? extends Model> modelClass = instance.getClass();
        Object columnValue = null;

        for (String column : columns) {
            try {
                columnValue = resultSet.getObject(column);
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
}
