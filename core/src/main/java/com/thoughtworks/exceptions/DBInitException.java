package com.thoughtworks.exceptions;

public class DBInitException extends RuntimeException {
    public DBInitException(String errorMessage) {
        super(errorMessage);
    }

    public DBInitException() {
    }
}
