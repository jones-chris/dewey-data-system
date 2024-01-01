package com.deweydatasystem.dao.database;

import com.deweydatasystem.TestUtils;
import com.deweydatasystem.exceptions.ResultSetDataExtractionException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class QueryResultTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        QueryResult queryResult = new QueryResult();
//        queryResult.setSelectStatement(TestUtils.buildSelectStatement());
        queryResult.setSql("select * from table");

        byte[] bytes = SerializationUtils.serialize(queryResult);
        QueryResult deserializedQueryResult = SerializationUtils.deserialize(bytes);

        assertEquals(queryResult, deserializedQueryResult);
    }

    @Test(expected = ResultSetDataExtractionException.class)
    public void constructor_sqlExceptionIsHandledCorrectly() throws SQLException {
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        Mockito.when(resultSetMock.getMetaData()).thenThrow(SQLException.class);

        new QueryResult(resultSetMock, "select * from some_table");
    }

    @Test
    public void constructor_setsMessageCorrectly() {
        final String expectedMessage = "I am a message";

        QueryResult queryResult = new QueryResult(expectedMessage);

        assertEquals(expectedMessage, queryResult.getMessage());
    }

}