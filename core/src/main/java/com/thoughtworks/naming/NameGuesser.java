package com.thoughtworks.naming;

public interface NameGuesser {
    String getTableName(String className);

    String getFieldName(String columnName);

    String getForeignKeyOf(String tableName);
}
