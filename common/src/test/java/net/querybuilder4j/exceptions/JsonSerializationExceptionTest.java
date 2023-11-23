package net.querybuilder4j.exceptions;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSerializationExceptionTest {

    @Test
    public void constructor_setsThrowableCorrectly() {
        final Throwable expectedThrowable = new Throwable();

        var exception = new JsonSerializationException(expectedThrowable);

        assertEquals(expectedThrowable, exception.getCause());
    }

}