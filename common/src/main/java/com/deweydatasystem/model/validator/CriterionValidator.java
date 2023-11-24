package com.deweydatasystem.model.validator;

import com.deweydatasystem.model.criterion.Criterion;
import lombok.NonNull;
import com.deweydatasystem.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import com.deweydatasystem.utils.Utils;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

import static com.deweydatasystem.model.criterion.Operator.isNotNull;
import static com.deweydatasystem.model.criterion.Operator.isNull;

public class CriterionValidator extends SqlValidator<Criterion> {

    @Override
    public void isValid(@NonNull Criterion obj) {
        // Column and operator must always be non-null.
        if (obj.getColumn() == null || obj.getOperator() == null) {
            throw new IllegalStateException("Criterion column and operator are null");
        }

        /*
         If operator is not `isNotNull` and `isNull` and filter is null or an empty string, then criterion is not valid.
         In other words, the criterion has an operator that expects a non-null or non-empty filter, but the filter is
         null or an empty string.
         */
        if (! obj.getOperator().equals(isNotNull) && ! obj.getOperator().equals(isNull)) {
            if (obj.getFilter().getValues().isEmpty() || obj.getFilter().getValues().contains("")) {
                if (obj.getFilter().getSubQueries().isEmpty()) {
                    throw new IllegalStateException("Criterion filter values are empty or contain an empty string but the operator " +
                            "is not isNull or isNotNull and there are no sub queries");
                }
            }
        }

        // If the criterion's column's data type is a non-string type, check that the values can be converted to the data type.
        int jdbcDataType = obj.getColumn().getDataType();
        if (jdbcDataType == Types.BIGINT || jdbcDataType == Types.DECIMAL || jdbcDataType == Types.DOUBLE ||
                jdbcDataType == Types.FLOAT || jdbcDataType == Types.INTEGER || jdbcDataType == Types.NUMERIC ||
                jdbcDataType == Types.SMALLINT || jdbcDataType == Types.TINYINT) {
            for (String value : obj.getFilter().getValues()) {
                try {
                    BigDecimal.valueOf(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    throw new CriterionColumnDataTypeAndFilterMismatchException(
                            Utils.getJdbcSqlType(jdbcDataType),
                            value
                    );
                }
            }
        }
        else if (jdbcDataType == Types.BOOLEAN) {
            for (String value : obj.getFilter().getValues()) {
                if (! Boolean.TRUE.toString().toLowerCase().equals(value) || ! Boolean.FALSE.toString().toLowerCase().equals(value)) {
                    throw new CriterionColumnDataTypeAndFilterMismatchException("BOOLEAN", value);
                }
            }
        }

        // Now check that the criterion's filter does not contain SQL injection attempts.
        assertSqlIsClean(obj);
    }

    public static void assertSqlIsClean(Criterion criterion) {
        /*
         * Only perform this validation on the criterion's filter's values.  Therefore, we assume the criterion's filter's
         * sub queries and parameters have been interpolated into the criterion's filter's values already.
         */
        List<String> values = criterion.getFilter().getValues();
        if (! values.isEmpty()) {
            for (String value : values) {
                assertSqlIsClean(value);
            }
        }
    }

}
