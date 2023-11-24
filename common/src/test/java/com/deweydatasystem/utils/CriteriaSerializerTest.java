package com.deweydatasystem.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.deweydatasystem.TestUtils;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Conjunction;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import com.deweydatasystem.model.criterion.Operator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Types;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CriteriaSerializerTest {

    @Spy
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    private final CriteriaSerializer criteriaSerializer = new CriteriaSerializer();

    @Test
    public void serialize_successfullyFlattensAndSerializesNestedCriteria() throws IOException {
        Criterion rootCriterion = TestUtils.buildCriterion(
                TestUtils.buildColumn(Types.VARCHAR),
                new Filter(
                        List.of("value1"),
                        List.of("subQuery1"),
                        List.of("parameter1")
                )
        );
        Criterion childCriterion = new Criterion(
                1,
                rootCriterion,
                Conjunction.And,
                TestUtils.buildColumn(Types.INTEGER),
                Operator.equalTo,
                new Filter(
                        List.of("1"),
                        List.of("subQuery2"),
                        List.of("subQuery2")
                ),
                List.of()
        );
        rootCriterion.setChildCriteria(
                List.of(childCriterion)
        );
        List<Criterion> criteria = List.of(rootCriterion);

        criteriaSerializer.serialize(
                criteria,
                this.jsonGenerator,
                this.serializerProvider
        );

        verify(this.jsonGenerator, times(2)).writeFieldName("id"); // 1x for each criterion.
        verify(this.jsonGenerator, times(1)).writeFieldName("parentId"); // Once for child criterion.
        verify(this.jsonGenerator, times(3)).writeNumber(anyInt()); // 2x for "id" and 1x for "parentId".

        verify(this.jsonGenerator, times(2)).writeFieldName("conjunction"); // 1x for each criterion.
        verify(this.jsonGenerator, times(2)).writeFieldName("operator"); // 1x for each criterion.
        verify(this.jsonGenerator, times(10)).writeString(anyString()); // 2x for each criterion's conjunction and operator, 1x for each criterion's filter's values, subQueries, and paraemters.

        verify(this.jsonGenerator, times(2)).writeFieldName("column"); // 1x for each criterion.
        verify(this.jsonGenerator, times(2)).writeObject(any(Column.class)); // 1x for each criterion.

        verify(this.jsonGenerator, times(2)).writeFieldName("filter");
        verify(this.jsonGenerator, times(2)).writeFieldName("values");
    }

}