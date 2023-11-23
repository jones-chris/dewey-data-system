package net.querybuilder4j.exceptions;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonDeserializationExceptionTest {

    @Test
    public void constructor_setsMessageCorrectly() {
        final String expectedMessage = "I am a message";

        var exception = new JsonDeserializationException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void constructor_setsThrowableCorrectly() {
        final Throwable expectedThrowable = new Throwable();

        var exception = new JsonDeserializationException(expectedThrowable);

        assertEquals(expectedThrowable, exception.getCause());
    }

}