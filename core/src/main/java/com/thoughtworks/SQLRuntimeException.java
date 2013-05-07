package com.thoughtworks;

public class SQLRuntimeException extends RuntimeException{
    public SQLRuntimeException(Exception e) {
        super(e);
    }
}
