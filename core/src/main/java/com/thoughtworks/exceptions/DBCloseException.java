package com.thoughtworks.exceptions;

public class DBCloseException extends RuntimeException {
    public DBCloseException(String s) {
        super(s);
    }
}
