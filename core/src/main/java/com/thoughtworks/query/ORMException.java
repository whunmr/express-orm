package com.thoughtworks.query;

public class ORMException extends RuntimeException{
    public ORMException(Exception e) {
        super(e);
    }
}
