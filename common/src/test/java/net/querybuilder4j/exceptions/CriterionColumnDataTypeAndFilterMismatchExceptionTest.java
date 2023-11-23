package net.querybuilder4j.exceptions;

import org.junit.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

public class CriterionColumnDataTypeAndFilterMismatchExceptionTest {

    @Test
    public void constructor_setsMessageCorrectly() {
        final String jdbcType = "varchar";
        final String filterValue = "My filter";

        var exception = new CriterionColumnDataTypeAndFilterMismatchException(jdbcType, filterValue);

        assertEquals(
                String.format(CriterionColumnDataTypeAndFilterMismatchException.MESSAGE_TEMPLATE, jdbcType, filterValue, jdbcType),
                exception.getMessage()
        );
    }

}