package com.deweydatasystem.model.column;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ColumnTest {

    @Test(expected = IllegalStateException.class)
    public void getAlias_illegalStateExceptionIsThrownIfAliasAndColumnNameAreBothNull() {
        Column column = new Column();
        column.getAlias();
    }

    @Test(expected = IllegalStateException.class)
    public void getAlias_illegalStateExceptionIsThrownIfAliasIsNullAndColumnNameIsAnEmptyString() {
        Column column = new Column();
        column.setColumnName(" ");
        column.getAlias();
    }

    @Test
    public void getAlias_returnsColumnNameIfAliasIsNull() {
        Column column = new Column();
        column.setAlias(null);
        column.setColumnName("column1");

        String alias = column.getAlias();

        assertEquals("column1", alias);
    }

    @Test
    public void getAlias_returnsAliasIfAliasIsNotNull() {
        Column column = new Column();
        column.setColumnName("column1");
        column.setAlias("alias1");

        String alias = column.getAlias();

        assertEquals("alias1", alias);
    }

    @Test
    public void toSql_usesColumnNameAsTheAliasIfAliasIsNull() {
        String expectedSql = " \"schema\".\"table\".\"column\" AS column ";
        Column column = this.buildColumn(null, "schema");

        String sql = column.toSql('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void toSql_usesColumnNameAsTheAliasIfAliasIsAnEmptyString() {
        String expectedSql = " \"schema\".\"table\".\"column\" AS column ";
        Column column = this.buildColumn(" ", "schema");

        String sql = column.toSql('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void toSql_usesAliasAsTheAliasIfAliasIsNotNull() {
        String expectedSql = " \"schema\".\"table\".\"column\" AS alias ";
        Column column = this.buildColumn("alias", "schema");

        String sql = column.toSql('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void toSql_schemaIsNotIncludedInSqlIfSchemaIsNull() {
        String expectedSql = " \"table\".\"column\" AS alias ";
        Column column = this.buildColumn("alias", null);

        String sql = column.toSql('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void toSql_withoutAliasAndWithSchema() {
        String expectedSql = " \"schema\".\"table\".\"column\" ";
        Column column = this.buildColumn("alias", "schema");

        String sql = column.toSqlWithoutAlias('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void toSql_withoutAliasAndWithoutSchema() {
        String expectedSql = " \"table\".\"column\" ";
        Column column = this.buildColumn("alias", null);

        String sql = column.toSqlWithoutAlias('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void toSql_withAliasAndWithSchema() {
        String expectedSql = " \"schema\".\"table\".\"column\" AS alias ";
        Column column = this.buildColumn("alias", "schema");

        String sql = column.toSql('"', '"');

        assertEquals(expectedSql, sql);
    }

    @Test
    public void constructor_nullSchemaSetsSchemaNameToNullString() {
        Column column = new Column("database", null, "table", "column", Types.INTEGER, "alias");

        assertEquals("null", column.getSchemaName());
        assertEquals("database.null.table.column", column.getFullyQualifiedName());
        assertEquals("alias", column.getAlias());
    }

    @Test
    public void constructor_nonNullSchemaSetsSchemaNameToTheNonNullString() {
        Column column = new Column("database", "schema", "table", "column", Types.INTEGER, "alias");

        assertEquals("schema", column.getSchemaName());
        assertEquals("database.schema.table.column", column.getFullyQualifiedName());
        assertEquals("alias", column.getAlias());
    }

    @Test
    public void serializedAndDeserializedAllFieldsSuccessfully() {
        Column column = new Column();
        column.setAlias("alias");
        column.setColumnName("column");
        column.setDatabaseName("database");
        column.setDataType(10);
        column.setIsIndexed(true);
        column.setSchemaName("schema");
        column.setTableName("table");
        column.setFullyQualifiedName("database.schema.table.column");

        byte[] bytes = SerializationUtils.serialize(column);
        Column deserializedColumn = SerializationUtils.deserialize(bytes);

        assertEquals(column, deserializedColumn);
    }

    private Column buildColumn(String alias, String schema) {
        String fullyQualifiedName;
        if (schema == null || schema.equals("null")) {
            fullyQualifiedName = "database.null.table.column";
        } else {
            fullyQualifiedName = "database.schema.table.column";
        }

        Column column = new Column();
        column.setFullyQualifiedName(fullyQualifiedName);
        column.setDatabaseName("database");
        column.setSchemaName(schema);
        column.setTableName("table");
        column.setColumnName("column");
        column.setDataType(Types.INTEGER);
        column.setAlias(alias);

        return column;
    }

}