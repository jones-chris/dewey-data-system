package com.deweydatasystem.dao.database;

import com.deweydatasystem.TestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class IdentifiedQueryResultTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        IdentifiedQueryResult identifiedQueryResult = new IdentifiedQueryResult(UUID.randomUUID());
//        identifiedQueryResult.setSelectStatement(TestUtils.buildSelectStatement());
        identifiedQueryResult.setSql("select * from table");

        byte[] bytes = SerializationUtils.serialize(identifiedQueryResult);
        IdentifiedQueryResult deserializedIdentifiedQueryResult = SerializationUtils.deserialize(bytes);

        assertEquals(identifiedQueryResult, deserializedIdentifiedQueryResult);
    }

    @Test
    public void constructor_setsMessagesCorrectly() {
        final String expectedMessage = "I am a message";

        IdentifiedQueryResult identifiedQueryResult = new IdentifiedQueryResult(expectedMessage);

        assertEquals(expectedMessage, identifiedQueryResult.getMessage());
    }

    @Test
    public void updateStatusStartTime_addsStatus() {
        final IdentifiedQueryResult.Status expectedStatus = IdentifiedQueryResult.Status.QUEUED;
        IdentifiedQueryResult identifiedQueryResult = new IdentifiedQueryResult(UUID.randomUUID());

        identifiedQueryResult.updateStatusStartTime(expectedStatus);

        assertEquals(1, identifiedQueryResult.getStatusStartTimes().size());
        assertTrue(identifiedQueryResult.getStatusStartTimes().containsKey(expectedStatus));
    }

}