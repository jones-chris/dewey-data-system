package com.deweydatasystem.exceptions;


import com.deweydatasystem.config.DatabaseType;

public class DatabaseTypeNotRecognizedException extends RuntimeException {

    static String MESSAGE_TEMPLATE = "Database type, %s, not recognized";

    public DatabaseTypeNotRecognizedException(DatabaseType databaseType) {
        super(
                String.format(MESSAGE_TEMPLATE, databaseType)
        );
    }

}
