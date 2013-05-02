package com.thoughtworks;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultNameGuesser implements NameGuesser {

    @Override
    public String getTableName(String className) {
        return underscore(checkNotNull(className));
    }

    private String underscore(String camel) {
        return camel.replaceAll("[A-Z]", "_$0").substring(1).toLowerCase() + "s";
    }

    @Override
    public String getFieldName(String columnName) {
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
}
