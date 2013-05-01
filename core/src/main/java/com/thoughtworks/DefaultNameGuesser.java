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
}
