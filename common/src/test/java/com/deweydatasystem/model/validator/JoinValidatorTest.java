package com.deweydatasystem.model.validator;

import com.deweydatasystem.TestUtils;
import com.deweydatasystem.model.join.Join;
import com.deweydatasystem.model.table.Table;
import org.junit.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class JoinValidatorTest {

    private final JoinValidator joinValidator = new JoinValidator();

    @Test
    public void isValid_sameNumberOfParentAndTargetJoinColumnsDoesNotThrowException() {
        List<Join> joins = new ArrayList<>();
        Join join = new Join();
        join.setParentTable(
                new Table("database1", "schema1", "table1")
        );
        join.setTargetTable(
                new Table("database1", "schema2", "table2")
        );
        join.getParentJoinColumns().add(TestUtils.buildColumn(Types.VARCHAR));
        join.getTargetJoinColumns().add(TestUtils.buildColumn(Types.VARCHAR));
        joins.add(join);

        joinValidator.isValid(joins);

        assertTrue(true);
    }

    @Test(expected = IllegalStateException.class)
    public void isValid_differentNumberOfParentAndTargetJoinColumnsThrowsException() {
        List<Join> joins = new ArrayList<>();
        Join join = new Join();
        join.setParentTable(
                new Table("database1", "schema1", "table1")
        );
        join.setTargetTable(
                new Table("database1", "schema2", "table2")
        );
        join.getParentJoinColumns().add(TestUtils.buildColumn(Types.VARCHAR));
        join.getParentJoinColumns().add(TestUtils.buildColumn(Types.VARCHAR));
        join.getTargetJoinColumns().add(TestUtils.buildColumn(Types.VARCHAR));
        joins.add(join);

        joinValidator.isValid(joins);
    }

    @Test(expected = IllegalStateException.class)
    public void isValid_ifParentAndTargetJoinTablesAreTheSameThenExceptionIsThrown() {
        final String databaseName = "database";
        final String schemaName = "schema";
        final String tableName = "table";
        List<Join> joins = new ArrayList<>();
        Join join = new Join();
        join.setParentTable(
                new Table(databaseName, schemaName, tableName)
        );
        join.setTargetTable(
                new Table(databaseName, schemaName, tableName)
        );
        joins.add(join);

        joinValidator.isValid(joins);
    }

}