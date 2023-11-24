package com.deweydatasystem.exceptions;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryFailureExceptionTest {

    @Test
    public void constructor_getMessageFormattingIsCorrectWhenThrowableHasMessage() {
        final String expectedSql = "select * from table";
        final Throwable expectedThrowable = new Throwable("Something went wrong!");

        var exception = new QueryFailureException(expectedThrowable, expectedSql);

        assertEquals(
                String.format(QueryFailureException.MESSAGE_TEMPLATE, expectedSql, expectedThrowable.getMessage()),
                exception.getMessage()
        );
    }

    @Test
    public void constructor_getMessageFormattingIsCorrectWhenThrowableHasNoMessage() {
        final String expectedSql = "select * from table";
        final Throwable expectedThrowable = new Throwable();

        var exception = new QueryFailureException(expectedThrowable, expectedSql);

        assertEquals(
                String.format(QueryFailureException.MESSAGE_TEMPLATE, expectedSql, expectedThrowable.getMessage()),
                exception.getMessage()
        );
    }

}