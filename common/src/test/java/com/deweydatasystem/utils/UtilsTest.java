package com.deweydatasystem.utils;

import com.deweydatasystem.TestUtils;
import com.deweydatasystem.exceptions.JsonDeserializationException;
import com.deweydatasystem.exceptions.SqlTypeNotRecognizedException;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Criterion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(JUnit4.class)
public class UtilsTest {

    @Test
    public void getJdbcSqlType_findsType() {
        String jdbcType = Utils.getJdbcSqlType(12);

        assertEquals("VARCHAR", jdbcType);
    }

    @Test
    public void getJdbcSqlType_exceptionThrownWhenTypeCannotBeFound() {
        assertThrows(SqlTypeNotRecognizedException.class, () -> Utils.getJdbcSqlType(1000000));
    }

    @Test
    public void shouldBeQuoted_exceptionIsThrownWhenTypeCannotBeFound() {
        assertThrows(SqlTypeNotRecognizedException.class, () -> Utils.shouldBeQuoted(1000000));
    }

    @Test
    public void deserializeJson_returnsDeserializedObjectSuccessfully() {
        Column column = TestUtils.buildColumn(Types.VARCHAR);
        String columnJson = Utils.serializeToJson(column);

        Column deserializedColumn = Utils.deserializeJson(columnJson, Column.class);

        assertEquals(column, deserializedColumn);
    }

    @Test
    public void deserializeJson_throwsExceptionWhenDeserializationFails() {
        Column column = TestUtils.buildColumn(Types.VARCHAR);
        String columnJson = Utils.serializeToJson(column);

        assertThrows(JsonDeserializationException.class, () -> Utils.deserializeJson(columnJson, Criterion.class));
    }

    @Test
    public void deserializeJsons_returnsListOfDeserializedObjectsSuccessfully() {
        List<Column> expectedColumns = List.of(
                TestUtils.buildColumn(Types.VARCHAR),
                TestUtils.buildColumn(Types.INTEGER)
        );
        List<String> columnJsons = expectedColumns.stream()
                .map(Utils::serializeToJson)
                .collect(Collectors.toList());

        List<Column> actualColumns = Utils.deserializeJsons(columnJsons, Column.class);

        assertEquals(expectedColumns, actualColumns);
    }

}
