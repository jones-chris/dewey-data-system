package com.deweydatasystem.model.database;

import com.deweydatasystem.config.DatabaseType;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        Database database = new Database("databaseName", DatabaseType.PostgreSQL);

        byte[] bytes = SerializationUtils.serialize(database);
        Database deserializedDatabase = SerializationUtils.deserialize(bytes);

        assertEquals(database, deserializedDatabase);
    }

}