package net.querybuilder4j.model.schema;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaTest {

    @Test
    public void constructor_createsFullyQualifiedNameCorrectly() {
        var schema = new Schema("database1", "schema1");

        assertEquals(
                "database1.schema1",
                schema.getFullyQualifiedName()
        );
    }

}