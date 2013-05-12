package com.thoughtworks.query.naming;

import com.thoughtworks.annotation.Column;
import com.thoughtworks.annotation.Table;

import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultNameGuesser implements NameGuesser {

    @Override
    public String getTableName(String className) {
        checkNotNull(className);

        String tableName = getTableNameFromAnnotation(className);
        if (tableName != null) {
            return tableName;
        }

        String classNameWithoutPackage = className.replaceAll("(.*\\.)", "");
        return underscore(classNameWithoutPackage) + "s";
    }

    @Override
    public String getFieldName(Class<?> modelClass, String columnName) {
        checkNotNull(columnName);

        String fieldName = getFieldNameFromAnnotation(modelClass, columnName);
        if (fieldName != null) {
            return fieldName;
        }

        String[] parts = columnName.split("_");
        if (parts.length == 1) {
            return columnName;
        }

        StringBuilder stringBuilder = new StringBuilder(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            String captialized = Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
            stringBuilder.append(captialized);
        }

        return stringBuilder.toString();
    }

    private String getFieldNameFromAnnotation(Class<?> modelClass, String columnName) {
        Field[] declaredFields = modelClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Column annotation = field.getAnnotation(Column.class);
            if (annotation != null) {
                String columnNameOfField = annotation.value();

                if (columnNameOfField.equals(columnName)) {
                    return field.getName();
                }
            }
        }

        return null;
    }

    @Override
    public String getForeignKeyNameInDB(String modelClassName) {
        return underscore(modelClassName.replaceAll("(.*\\.)", "")) + "_id";
    }

    @Override
    public String getCollectionFieldName(String fieldModelClassName) {
        return getTableName(fieldModelClassName);
    }

    @Override
    public String getForeignKeyFieldName(String parentModelClassName) {
        String simpleClassName = parentModelClassName.replaceAll("(.*\\.)", "");
        char firstChar = Character.toLowerCase(simpleClassName.charAt(0));
        return String.format("%s%s%s", firstChar, simpleClassName.substring(1), "Id");
    }

    private String getTableNameFromAnnotation(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            if (tableAnnotation != null) {
                return tableAnnotation.value();
            }
        } catch (ClassNotFoundException e) {
        }

        return null;
    }

    private String underscore(String camel) {
        return camel.replaceAll("[A-Z]", "_$0").substring(1).toLowerCase();
    }
}
