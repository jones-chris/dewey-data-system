package com.deweydatasystem.exceptions;

import com.deweydatasystem.config.DatabaseType;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTypeNotRecognizedExceptionTest {

    @Test
    public void constructor_setsMessageCorrectly() {
        final DatabaseType databaseType = DatabaseType.MySql;

        var exception = new DatabaseTypeNotRecognizedException(databaseType);

        assertEquals(
                String.format(DatabaseTypeNotRecognizedException.MESSAGE_TEMPLATE, databaseType),
                exception.getMessage()
        );
    }

}