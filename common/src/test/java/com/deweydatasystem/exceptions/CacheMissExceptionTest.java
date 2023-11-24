package com.deweydatasystem.exceptions;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CacheMissExceptionTest {

    @Test
    public void constructor_setsMessageCorrectly() {
        final String expectedMessage = "I am a message";

        CacheMissException cacheMissException = new CacheMissException(expectedMessage);

        assertEquals(expectedMessage, cacheMissException.getMessage());
    }

}