package net.querybuilder4j.model.criterion;

import net.querybuilder4j.TestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.*;

public class CriterionParameterTest {

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        CriterionParameter criterionParameter = new CriterionParameter(
                "name",
                TestUtils.buildColumn(Types.INTEGER),
                false
        );

        byte[] bytes = SerializationUtils.serialize(criterionParameter);
        CriterionParameter deserializedCriterionParameter = SerializationUtils.deserialize(bytes);

        assertEquals(criterionParameter, deserializedCriterionParameter);
    }

}