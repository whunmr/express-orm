package com.thoughtworks.naming;

import com.thoughtworks.annotation.Table;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultNameGuesser implements NameGuesser {

    @Override
    public String getTableName(String className) {
        checkNotNull(className);

        try {
            Class<?> clazz = Class.forName(className);
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            if (tableAnnotation != null) {
                return tableAnnotation.value();
            }
        } catch (ClassNotFoundException e) {
        }

        String classNameWithoutPackage = className.replaceAll("(.*\\.)", "");
        return underscore(classNameWithoutPackage) + "s";
    }

    private String underscore(String camel) {
        return camel.replaceAll("[A-Z]", "_$0").substring(1).toLowerCase();
    }

    @Override
    public String getFieldName(Class<?> modelClass, String columnName) {
        checkNotNull(columnName);



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
}
