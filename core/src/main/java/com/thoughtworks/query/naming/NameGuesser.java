package com.thoughtworks.query.naming;

public interface NameGuesser {
    String getTableName(String className);

    String getFieldName(Class<?> modelClass, String columnName);

    String getForeignKeyNameInDB(String modelClassName);

    String getCollectionFieldName(String fieldModelClassName);

    String getForeignKeyFieldName(String parentModelClassName);
}
