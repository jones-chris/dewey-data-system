package com.deweydatasystem.utils;

import com.deweydatasystem.exceptions.JsonDeserializationException;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ExcludeFromJacocoGeneratedReport // todo:  Remove this after researching why Jacoco is not including test coverage for this class.
public class CriteriaDeserializer extends StdDeserializer<List<Criterion>> {

    public CriteriaDeserializer() {
        this(null);
    }

    protected CriteriaDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Criterion> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ArrayNode node = jsonParser.getCodec().readTree(jsonParser);
        List<Criterion> deserializedCriteria = new ArrayList<>();
        node.forEach(jsonNode -> {
            Criterion newCriterion = buildCriterion(jsonNode, deserializedCriteria);

            // Only add root criterion directly to the `deserializedCriteria` list.  Non-root/child criterion will be
            // added to the list by being added to non-root/child criterions' child criteria in the `buildCriterion`
            // method.
            if (newCriterion.isRoot()) {
                deserializedCriteria.add(newCriterion);
            }
        });

        return deserializedCriteria;
    }

    private Criterion buildCriterion(JsonNode criterionJson, List<Criterion> deserializedCriteria) {
        // Conjunction
        Conjunction conjunction = Conjunction.valueOf(criterionJson.get("conjunction").asText());

        // Column
        Column column = new Column(
                criterionJson.get("column").get("databaseName").asText(),
                criterionJson.get("column").get("schemaName").asText(),
                criterionJson.get("column").get("tableName").asText(),
                criterionJson.get("column").get("columnName").asText(),
                criterionJson.get("column").get("dataType").asInt(),
                criterionJson.get("column").get("alias").asText(),
                criterionJson.get("column").get("isIndexed").asBoolean()
        );

        // Operator
        Operator operator = Operator.valueOf(criterionJson.get("operator").asText());

        // Filter
        Filter filter = new Filter();
        JsonNode filterNode = criterionJson.get("filter");
        JsonNode filterValues = filterNode.get("values");
        if (! filterValues.isArray()) {
            throw new JsonDeserializationException("criterion's filter's values must be an array");
        }

        for (JsonNode value : filterValues) {
            String valueText = value.asText();

            // Set the Parameters if they come over from the UI as a Value with the '@' prefix.
            if (valueText.startsWith("@")) {
                String valueWithoutPrefix = valueText.substring(1); // Strip the '@' at the beginning of the value.
                filter.setParameter(valueWithoutPrefix);
//                filter.getParameters().add(valueWithoutPrefix);
            }
            // Set the Sub Queries if they come over from the UI as a Value with the '$' prefix.
            else if (valueText.startsWith("$")) {
                String valueWithoutPrefix = valueText.substring(1); // Strip the '$' at the beginning of the value.
                filter.setSubQueryPlaceholder(valueWithoutPrefix);
//                filter.getSubQueries().add(valueWithoutPrefix);
            }
            // Values
            else {
                filter.getValues().add(valueText);
            }
        }

        // Add the parameters that have already been processed and set in the parameters array in a previous request by
        // the above logic.
        JsonNode filterParams = filterNode.get("parameters");
        if (filterParams != null) {
            if (! filterParams.isArray()) {
                throw new JsonDeserializationException("criterion's filter's parameters must be an array");
            }

            for (JsonNode parameter : filterParams) {
                filter.setParameter(parameter.asText());
//                filter.getParameters().add(parameter.asText());
            }
        }

        // Add the sub queries that have already been processed and set in the subQueries array in a previous request by
        // the above logic.
        JsonNode filterSubQueries = filterNode.get("subQueries");
        if (filterSubQueries != null) {
            if (! filterSubQueries.isArray()) {
                throw new JsonDeserializationException("criterion's filter's subQueries must be an array");
            }

            for (JsonNode subQuery : filterSubQueries) {
                filter.setSubQueryPlaceholder(subQuery.asText());
//                filter.getSubQueries().add(subQuery.asText());
            }
        }

        // Id
        int id = criterionJson.get("id").asInt();

        // Find parent criterion.
        Criterion parentCriterion = null;
        if (criterionJson.get("parentId") != null && ! criterionJson.get("parentId").isNull()) {
            int parentId = criterionJson.get("parentId").asInt();

            List<Criterion> flattenedCriteria = new ArrayList<>();
            CriteriaTreeFlattener.flattenCriteria(deserializedCriteria, new HashMap<>())
                    .forEach((rootIndex, criteria) -> flattenedCriteria.addAll(criteria));

            parentCriterion = flattenedCriteria.stream()
                    .filter(criterion -> criterion.getId() == parentId)
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
        }

        // Instantiate the new criterion.
        Criterion newCriterion = new Criterion(
                id,
                parentCriterion,
                conjunction,
                column,
                operator,
                filter,
                null
        );

        // If the parent criterion is not null, then add the new criterion to it's child criteria.
        if (parentCriterion != null) {
            parentCriterion.getChildCriteria().add(newCriterion);
        }

        return newCriterion;
    }

}
