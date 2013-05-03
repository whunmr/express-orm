package com.thoughtworks.binder;

import com.expressioc.utility.ClassUtility;
import com.thoughtworks.DefaultNameGuesser;
import com.thoughtworks.Model;
import com.thoughtworks.NameGuesser;
import com.thoughtworks.metadata.MetaDataProvider;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ResultSets {
    private static NameGuesser guesser = new DefaultNameGuesser();              //TODO: use ioc
    private static MetaDataProvider metaDataProvider = new MetaDataProvider();  //TODO: ioc

    public static <T extends Model> List<T> assembleInstanceListBy(ResultSet resultSet, String modelClassName) throws SQLException {
        List<T> objList = newArrayList();
        List<String> columns = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName)).getColumnNames();

        while (resultSet.next()) {
            objList.add(ResultSets.<T>assembleInstanceFrom(resultSet, columns, modelClassName));
        }

        return objList;
    }

    public static <T extends Model> T assembleInstanceBy(ResultSet resultSet, String modelClassName) throws SQLException {
        if (!resultSet.next()) {
            return null;
        }

        List<String> columns = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName)).getColumnNames();
        return assembleInstanceFrom(resultSet, columns, modelClassName);
    }

    private static <T extends Model> T assembleInstanceFrom(ResultSet resultSet, List<String> columns, String modelClassName) throws SQLException {
        T instance = getNewInstance(modelClassName);

        for (String column : columns) {
            Object columnValue = resultSet.getObject(column);

            try {
                Field field = instance.getClass().getDeclaredField(guesser.getFieldName(column));
                field.setAccessible(true);
                field.set(instance, getTypedValue(columnValue, field.getType()));
            } catch (NoSuchFieldException e) {
                System.out.println(e.toString());
                continue;
            } catch (IllegalAccessException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    private static Object getTypedValue(Object columnValue, Class<?> clazz) throws Exception {
        if (columnValue == null) {
            return null;
        }

        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return !columnValue.toString().equals("0");
        }

        if (clazz.equals(Character.class) || clazz.equals(char.class)) {
            return Character.valueOf(((String)columnValue).charAt(0));
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
