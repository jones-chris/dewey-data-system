package com.deweydatasystem.model.cte;

import com.deweydatasystem.TestUtils;
import com.deweydatasystem.model.SelectStatement;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class CommonTableExpressionTest {

    @Test(expected = IllegalStateException.class)
    public void toSql_ifSqlIsNullThenNullPointerExceptionIsThrown() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();

        commonTableExpression.toSql('`', '`');
    }

    @Test
    public void toSql_correctlyFormatsSqlString() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setName("cte1");
        commonTableExpression.setQueryName("get_cte_results");
        commonTableExpression.setParametersAndArguments(Map.of());
//        commonTableExpression.setSelectStatement(new SelectStatement());
        commonTableExpression.setSql(" SELECT col1, col2 FROM table1 ");
        String expectedCteSql = " cte1 AS ( SELECT col1, col2 FROM table1 )";

        String actualCteSql = commonTableExpression.toSql('`', '`');

        assertEquals(expectedCteSql, actualCteSql);
    }

    @Test(expected = IllegalStateException.class)
    public void toSql_nullSqlThrowsNullPointerException() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setSql(null);

        commonTableExpression.toSql('`', '`');
    }

    @Test
    public void isBuilt_nullSqlReturnsFalse() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setSql(null);

        assertFalse(commonTableExpression.isBuilt());
    }

    @Test
    public void isBuilt_emptySqlReturnsFalse() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setSql(" ");

        assertFalse(commonTableExpression.isBuilt());
    }

    @Test
    public void isBuilt_nonEmptySqlReturnsTrue() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setSql(" hello ");

        assertTrue(commonTableExpression.isBuilt());
    }

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setSql(" hello ");
        commonTableExpression.setName("name");
        commonTableExpression.setQueryName("queryName");
//        commonTableExpression.setSelectStatement(TestUtils.buildSelectStatement());
        commonTableExpression.setParametersAndArguments(Map.of());
        commonTableExpression.setVersion(10);
        commonTableExpression.setOverrides(new SelectStatement.PropertyOverrides());

        byte[] bytes = SerializationUtils.serialize(commonTableExpression);
        CommonTableExpression deserializedCommonTableExpression = SerializationUtils.deserialize(bytes);

        assertEquals(commonTableExpression, deserializedCommonTableExpression);
    }

}