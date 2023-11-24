package com.deweydatasystem.model;

import com.deweydatasystem.TestUtils;
import com.deweydatasystem.model.criterion.CriterionParameter;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.sql.Types;
import java.util.List;

import static org.junit.Assert.*;

public class SelectStatementTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        SelectStatement.Metadata metadata = new SelectStatement.Metadata();
        metadata.setName("name");
        metadata.setVersion(10);
        metadata.setColumns(
                List.of(
                        TestUtils.buildColumn(Types.INTEGER)
                )
        );
        metadata.setMaxNumberOfRowsReturned(100);
        metadata.setNumberOfColumnsReturned(10);
        metadata.setAuthor("author");
        metadata.setCriteriaParameters(
                List.of(
                        new CriterionParameter(
                                "name",
                                TestUtils.buildColumn(Types.INTEGER),
                                false
                        )
                )
        );
        metadata.setDescription("description");
        metadata.setDiscoverable(false);
        selectStatement.setMetadata(metadata);

        byte[] bytes = SerializationUtils.serialize(selectStatement);
        SelectStatement deserializedSelectStatement = SerializationUtils.deserialize(bytes);

        assertEquals(selectStatement, deserializedSelectStatement);
    }

}