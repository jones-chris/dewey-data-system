package net.querybuilder4j.model.table;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class TableTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        Table table = new Table("databaseName", "schemaName", "tableName");

        byte[] bytes = SerializationUtils.serialize(table);
        Table deserializedTable = SerializationUtils.deserialize(bytes);

        assertEquals(table, deserializedTable);
    }

    @Test
    public void toSql_nullSchemaProducesCorrectlyFormattedSqlString() {
        var table = new Table("database1", "null", "table1");

        var sql = table.toSql('"', '"');

        assertEquals(
                " \"table1\" ",
                sql
        );
    }

    @Test
    public void toSql_nonNullSchemaProducesCorrectlyFormattedSqlString() {
        var table = new Table("database1", "schema1", "table1");

        var sql = table.toSql('"', '"');

        assertEquals(
                " \"schema1\".\"table1\" ",
                sql
        );
    }

}