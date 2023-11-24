package com.deweydatasystem.exceptions;

import com.deweydatasystem.model.SelectStatement;
import lombok.Getter;
import lombok.Setter;

public class QueryFailureException extends Exception {

    // todo: Make this a text block when upgrading to Java 17.
    static final String MESSAGE_TEMPLATE = "A SQL query was successfully built but it failed when run against the target database.  \n" +
            "\n" +
            "Here is the SQL that was built and/or run:  \n" +
            "\n" +
            "%s  \n" +
            "\n" +
            "The database error was:  \n" +
            "\n" +
            "%s";

    private final Throwable throwable;

    @Getter
    @Setter
    private SelectStatement selectStatement; // todo:  Does this need to be logged somewhere?

    @Getter
    private final String sql;

    public QueryFailureException(Throwable t, String sql) {
        this.throwable = t;
        this.sql = sql;
    }

    @Override
    public String getMessage() {
        final String throwableMessage = (this.throwable == null) ? null : this.throwable.getMessage();

        return String.format(MESSAGE_TEMPLATE, this.sql, throwableMessage);
    }
}
