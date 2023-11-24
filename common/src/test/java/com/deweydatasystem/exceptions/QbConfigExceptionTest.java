package com.deweydatasystem.exceptions;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QbConfigExceptionTest {

    @Test
    public void constructor_setsMessageCorrectly() {
        final String expectedMessage = "I am a message";

        var exception = new QbConfigException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }

}