package com.deweydatasystem.utils;

import com.deweydatasystem.model.criterion.CriteriaTreeFlattener;
import com.deweydatasystem.model.criterion.Criterion;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.deweydatasystem.exceptions.JsonSerializationException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CriteriaSerializer extends StdSerializer<List<Criterion>> {

    public CriteriaSerializer() {
        this(null);
    }

    protected CriteriaSerializer(Class<List<Criterion>> t) {
        super(t);
    }

    @Override
    public void serialize(
            List<Criterion> criterionList,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        Map<Integer, List<Criterion>> flattenedCriteria = CriteriaTreeFlattener.flattenCriteria(criterionList, new HashMap<>());

        jsonGenerator.writeStartArray();

        // Flatten criteria values, which is are all List<Criterion>.
        List<Criterion> flattenedCriteriaValues = flattenedCriteria.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Write each criterion to the jsonGenerator.
        flattenedCriteriaValues.forEach(criterion -> {
            try {
                jsonGenerator.writeStartObject();

                // Id
                jsonGenerator.writeNumberField("id", criterion.getId());

                // Parent Id
                Criterion parentCriterion = criterion.getParentCriterion();
                if (parentCriterion != null) {
                    jsonGenerator.writeNumberField("parentId", parentCriterion.getId());
                }

                // Conjunction
                jsonGenerator.writeStringField("conjunction", criterion.getConjunction().name());

                // Column
                jsonGenerator.writeObjectField("column", criterion.getColumn());

                // Operator
                jsonGenerator.writeStringField("operator", criterion.getOperator().name());

                // Filter
                jsonGenerator.writeObjectFieldStart("filter");

                // Filter values array.  Combine values, sub queries, and parameters into the values array, which matches
                // how the API receives all of these in requests.
                jsonGenerator.writeArrayFieldStart("values");
                for (String filterValue : criterion.getFilter().getValues()) {
                    jsonGenerator.writeString(filterValue);
                }
                for (String subQuery : criterion.getFilter().getSubQueries()) {
                    jsonGenerator.writeString("$" + subQuery);
                }
                for (String parameter : criterion.getFilter().getParameters()) {
                    jsonGenerator.writeString("@" + parameter);
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeEndObject();

                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new JsonSerializationException(e);
            }
        });

        jsonGenerator.writeEndArray();
    }

}
