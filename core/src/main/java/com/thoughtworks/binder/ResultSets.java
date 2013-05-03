package com.thoughtworks.binder;

import com.thoughtworks.DefaultNameGuesser;
import com.thoughtworks.Model;
import com.thoughtworks.NameGuesser;
import com.thoughtworks.metadata.MetaDataProvider;
import com.thoughtworks.metadata.ModelMetaData;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ResultSets {
    private static NameGuesser guesser = new DefaultNameGuesser();     //TODO: use ioc
    private static MetaDataProvider metaDataProvider = new MetaDataProvider(); //TODO: ioc

    public static <T extends Model> T assembleInstanceBy(ResultSet rs, String modelClassName) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        T instance = getNewInstance(modelClassName);
        ModelMetaData metaData = metaDataProvider.getMetaDataOf(guesser.getTableName(modelClassName));
        List<String> columns = metaData.getColumnNames();
        for (String column : columns) {

            Object value = rs.getObject(column);

            try {
                Field field = instance.getClass().getDeclaredField(guesser.getFieldName(column));
                field.setAccessible(true);
                field.set(instance, value);
            } catch (NoSuchFieldException e) {
                System.out.println(e.toString());
                continue;
            } catch (IllegalAccessException e) {
            }
        }
        return instance;
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
