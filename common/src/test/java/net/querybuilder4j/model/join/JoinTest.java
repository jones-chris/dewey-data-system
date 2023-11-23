package net.querybuilder4j.model.join;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.model.table.Table;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.sql.Types;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class JoinTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        Join join = new Join();
        join.setJoinType(Join.JoinType.LEFT_EXCLUDING);
        join.setParentJoinColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );
        join.setTargetJoinColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );
        join.setParentTable(
                new Table("databaseName", "schemaName", "tableName")
        );
        join.setTargetTable(
                new Table("databaseName", "schemaName", "tableName")
        );

        byte[] bytes = SerializationUtils.serialize(join);
        Join deserializedJoin = SerializationUtils.deserialize(bytes);

        assertEquals(join, deserializedJoin);
    }

    @Test
    public void toSql_throwsExceptionIfNumberOfParentJoinTablesAndTargetJoinTablesIsUnequal() {
        assertThrows(
                RuntimeException.class,
                () -> {
                    var join = new Join();
                    join.getTargetJoinColumns().add(
                            TestUtils.buildColumn(Types.INTEGER)
                    );

                    join.toSql('"', '"');
                }
        );
    }

    @Test
    public void toSql_nullSchemaProducesCorrectlyFormattedSqlString() {
        var join = new Join();
        join.setJoinType(Join.JoinType.LEFT);
        join.setTargetTable(new Table("database1", "null", "table1"));
        join.setParentJoinColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );
        join.setTargetJoinColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );

        String sql = join.toSql('"', '"');

        assertEquals(
                " LEFT JOIN  \"table1\"  ON  \"schema\".\"table\".\"column\"  =  \"schema\".\"table\".\"column\"  ",
                sql
        );
    }

    @Test
    public void toSql_nonNullSchemaProducesCorrectlyFormattedSqlString() {
        var join = new Join();
        join.setJoinType(Join.JoinType.LEFT);
        join.setTargetTable(new Table("database1", "schema1", "table1"));
        join.setParentJoinColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );
        join.setTargetJoinColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );

        String sql = join.toSql('"', '"');

        assertEquals(
                " LEFT JOIN  \"schema1\".\"table1\"  ON  \"schema\".\"table\".\"column\"  =  \"schema\".\"table\".\"column\"  ",
                sql
        );
    }

    @Test
    public void joinType_toStringLeftExcludingIsFormattedCorrectly() {
        assertEquals(" LEFT JOIN ", Join.JoinType.LEFT_EXCLUDING.toString());
    }

    @Test
    public void joinType_toStringLeftIsFormattedCorrectly() {
        assertEquals(" LEFT JOIN ", Join.JoinType.LEFT.toString());
    }

    @Test
    public void joinType_toStringInnerIsFormattedCorrectly() {
        assertEquals(" INNER JOIN ", Join.JoinType.INNER.toString());
    }

    @Test
    public void joinType_toStringFullOuterIsFormattedCorrectly() {
        assertEquals(" FULL OUTER JOIN ", Join.JoinType.FULL_OUTER.toString());
    }

    @Test
    public void joinType_toStringFullOuterExcludingIsFormattedCorrectly() {
        assertEquals(" FULL OUTER JOIN ", Join.JoinType.FULL_OUTER_EXCLUDING.toString());
    }

    @Test
    public void joinType_toStringRightExcludingIsFormattedCorrectly() {
        assertEquals(" RIGHT JOIN ", Join.JoinType.RIGHT_EXCLUDING.toString());
    }

    @Test
    public void joinType_toStringRightIsFormattedCorrectly() {
        assertEquals(" RIGHT JOIN ", Join.JoinType.RIGHT.toString());
    }

}