package com.thoughtworks.naming;

public interface NameGuesser {
    String getTableName(String className);

    String getFieldName(String columnName);

    String getForeignKeyNameInDB(String modelClassName);

    String getCollectionFieldName(String fieldModelClassName);

    String getForeignKeyFieldName(String parentModelClassName);
}
