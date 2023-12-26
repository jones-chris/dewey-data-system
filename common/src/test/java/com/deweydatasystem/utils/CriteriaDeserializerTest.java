package com.deweydatasystem.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.deweydatasystem.TestUtils;
import com.deweydatasystem.exceptions.JsonDeserializationException;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CriteriaDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @Mock
    private ObjectCodec objectCodec;

    private final CriteriaDeserializer criteriaDeserializer = new CriteriaDeserializer();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void beforeEach() {
        when(this.jsonParser.getCodec())
                .thenReturn(this.objectCodec);
    }

    @Test
    public void deserialize_flatCriteriaDeserializesCorrectly() throws IOException {
        Criterion criterion1 = TestUtils.buildCriterion(
                TestUtils.buildColumn(Types.INTEGER),
                new Filter(
                        List.of("1", "2", "3")
                )
        );
        Criterion criterion2 = TestUtils.buildCriterion(
                TestUtils.buildColumn(Types.VARCHAR),
                new Filter(
                        List.of("bob", "sam", "joe")
                )
        );
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node1 = objectMapper.valueToTree(criterion1);
        JsonNode node2 = objectMapper.valueToTree(criterion2);
        ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
        arrayNode.addAll(
                List.of(node1, node2)
        );
        when(this.objectCodec.readTree(any()))
                .thenReturn(arrayNode);

        List<Criterion> deserializedCriteria = this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(2, deserializedCriteria.size());
        assertTrue(deserializedCriteria.get(0).getChildCriteria().isEmpty());
        assertTrue(deserializedCriteria.get(1).getChildCriteria().isEmpty());
    }

    @Test
    public void deserialize_nestedCriteriaDeserializesCorrectly() throws IOException {
        // Parent node with no children.
        ObjectNode node1 = JsonNodeFactory.instance.objectNode();
        node1.set("id", IntNode.valueOf(0));
        node1.set("conjunction", TextNode.valueOf("And"));
        node1.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
        node1.set("operator", TextNode.valueOf("in"));
        node1.set("filter", JsonNodeFactory.instance.objectNode().set(
                "values", JsonNodeFactory.instance.arrayNode().add("1")
        ));
        node1.set("parentId", null);

        // Parent node with children.
        ObjectNode node2 = JsonNodeFactory.instance.objectNode();
        node2.set("id", IntNode.valueOf(1));
        node2.set("conjunction", TextNode.valueOf("And"));
        node2.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.VARCHAR)));
        node2.set("operator", TextNode.valueOf("in"));
        node2.set("filter", JsonNodeFactory.instance.objectNode().set(
                "values", JsonNodeFactory.instance.arrayNode().add("bob")
        ));
        node2.set("parentId", null);

        // Child node.
        ObjectNode node2_1 = JsonNodeFactory.instance.objectNode();
        node2_1.set("id", IntNode.valueOf(2));
        node2_1.set("conjunction", TextNode.valueOf("And"));
        node2_1.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.BOOLEAN)));
        node2_1.set("operator", TextNode.valueOf("equalTo"));
        node2_1.set("filter", JsonNodeFactory.instance.objectNode().set(
                "values", JsonNodeFactory.instance.arrayNode().add("true")
        ));
        node2_1.set("parentId", IntNode.valueOf(1));

        ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
        arrayNode.addAll(
                List.of(node1, node2, node2_1)
        );
        when(this.objectCodec.readTree(any()))
                .thenReturn(arrayNode);

        List<Criterion> deserializedCriteria = this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(2, deserializedCriteria.size());
        assertTrue(deserializedCriteria.get(0).getChildCriteria().isEmpty());
        assertEquals(1, deserializedCriteria.get(1).getChildCriteria().size());
    }

    @Test
    public void deserialize_emptyFiltersThrowsException() {
        assertThrows(
                JsonDeserializationException.class,
                () -> {
                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    node.set("id", IntNode.valueOf(0));
                    node.set("conjunction", TextNode.valueOf("And"));
                    node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
                    node.set("operator", TextNode.valueOf("in"));
                    node.set("filter", JsonNodeFactory.instance.objectNode().set(
                            "values", JsonNodeFactory.instance.objectNode()
                    ));
                    node.set("parentId", null);

                    ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
                    arrayNode.addAll(
                            List.of(node)
                    );
                    when(this.objectCodec.readTree(any()))
                            .thenReturn(arrayNode);

                    this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);
                }
        );
    }

    @Test
    public void deserialize_newParametersDeserializesCorrectly() throws IOException {
        final String parameterName = "customerName";
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("id", IntNode.valueOf(0));
        node.set("conjunction", TextNode.valueOf("And"));
        node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
        node.set("operator", TextNode.valueOf("in"));
        node.set("filter", JsonNodeFactory.instance.objectNode().set(
                "values", JsonNodeFactory.instance.arrayNode().add("@" + parameterName)
        ));
        node.set("parentId", null);

        ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
        arrayNode.addAll(
                List.of(node)
        );
        when(this.objectCodec.readTree(any()))
                .thenReturn(arrayNode);

        List<Criterion> criteria = this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(1, criteria.size());
        assertFalse(criteria.get(0).getFilter().getParameter().isEmpty());
        assertEquals(parameterName, criteria.get(0).getFilter().getParameter());
    }

    @Test
    public void deserialize_existingParametersDeserializesCorrectly() throws IOException {
        final String parameterName = "customerName";
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("id", IntNode.valueOf(0));
        node.set("conjunction", TextNode.valueOf("And"));
        node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
        node.set("operator", TextNode.valueOf("in"));
        node.set("filter", JsonNodeFactory.instance.objectNode().setAll(
                Map.of(
                        "values", JsonNodeFactory.instance.arrayNode(),
                        "parameters", JsonNodeFactory.instance.arrayNode().add(parameterName)
                )
        ));
        node.set("parentId", null);

        ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
        arrayNode.addAll(
                List.of(node)
        );
        when(this.objectCodec.readTree(any()))
                .thenReturn(arrayNode);

        List<Criterion> criteria = this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(1, criteria.size());
        assertFalse(criteria.get(0).getFilter().getParameter().isEmpty());
        assertEquals(parameterName, criteria.get(0).getFilter().getParameter());
    }

    @Test
    public void deserialize_nonArrayParametersThrowsException() {
        assertThrows(
                JsonDeserializationException.class,
                () -> {
                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    node.set("id", IntNode.valueOf(0));
                    node.set("conjunction", TextNode.valueOf("And"));
                    node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
                    node.set("operator", TextNode.valueOf("in"));
                    node.set("filter", JsonNodeFactory.instance.objectNode().setAll(
                            Map.of(
                                    "values", JsonNodeFactory.instance.arrayNode(),
                                    "parameters", JsonNodeFactory.instance.objectNode()
                            )
                    ));
                    node.set("parentId", null);

                    ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
                    arrayNode.addAll(
                            List.of(node)
                    );
                    when(this.objectCodec.readTree(any()))
                            .thenReturn(arrayNode);

                    this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);
                }
        );
    }

    @Test
    public void deserialize_newSubQueriesDeserializesCorrectly() throws IOException {
        final String subQueryName = "subQuery1";
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("id", IntNode.valueOf(0));
        node.set("conjunction", TextNode.valueOf("And"));
        node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
        node.set("operator", TextNode.valueOf("in"));
        node.set("filter", JsonNodeFactory.instance.objectNode().set(
                "values", JsonNodeFactory.instance.arrayNode().add("$" + subQueryName)
        ));
        node.set("parentId", null);

        ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
        arrayNode.addAll(
                List.of(node)
        );
        when(this.objectCodec.readTree(any()))
                .thenReturn(arrayNode);

        List<Criterion> criteria = this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(1, criteria.size());
        assertFalse(criteria.get(0).getFilter().getSubQueryPlaceholder().isEmpty());
        assertEquals(subQueryName, criteria.get(0).getFilter().getSubQueryPlaceholder());
    }

    @Test
    public void deserialize_existingSubQueriesDeserializesCorrectly() throws IOException {
        final String subQueryName = "subQuery1";
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("id", IntNode.valueOf(0));
        node.set("conjunction", TextNode.valueOf("And"));
        node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
        node.set("operator", TextNode.valueOf("in"));
        node.set("filter", JsonNodeFactory.instance.objectNode().setAll(
                Map.of(
                        "values", JsonNodeFactory.instance.arrayNode(),
                        "subQueries", JsonNodeFactory.instance.arrayNode().add(subQueryName)
                )
        ));
        node.set("parentId", null);

        ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
        arrayNode.addAll(
                List.of(node)
        );
        when(this.objectCodec.readTree(any()))
                .thenReturn(arrayNode);

        List<Criterion> criteria = this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);

        assertEquals(1, criteria.size());
        assertFalse(criteria.get(0).getFilter().getSubQueryPlaceholder().isEmpty());
        assertEquals(subQueryName, criteria.get(0).getFilter().getSubQueryPlaceholder());
    }

    @Test
    public void deserialize_nonArraySubQueriesThrowsException() {
        assertThrows(
                JsonDeserializationException.class,
                () -> {
                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    node.set("id", IntNode.valueOf(0));
                    node.set("conjunction", TextNode.valueOf("And"));
                    node.set("column", this.objectMapper.valueToTree(TestUtils.buildColumn(Types.INTEGER)));
                    node.set("operator", TextNode.valueOf("in"));
                    node.set("filter", JsonNodeFactory.instance.objectNode().setAll(
                            Map.of(
                                    "values", JsonNodeFactory.instance.arrayNode(),
                                    "subQueries", JsonNodeFactory.instance.objectNode()
                            )
                    ));
                    node.set("parentId", null);

                    ArrayNode arrayNode = new ArrayNode(new JsonNodeFactory(true));
                    arrayNode.addAll(
                            List.of(node)
                    );
                    when(this.objectCodec.readTree(any()))
                            .thenReturn(arrayNode);

                    this.criteriaDeserializer.deserialize(jsonParser, deserializationContext);
                }
        );
    }

}