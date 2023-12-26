package com.deweydatasystem.model.validator;

import com.deweydatasystem.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.utils.Utils;
import lombok.NonNull;

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
                if (obj.getFilter().getSubQueryPlaceholder().isEmpty()) {  // todo:  check this logic.
                    throw new IllegalStateException("Criterion filter values are empty or contain an empty string but the operator " +
                            "is not isNull or isNotNull and there are no sub queries");
                }
            }
        }

        // If the criterion's column's data type is a non-string type, check that the values can be converted to the data type.
        int jdbcDataType = obj.getColumn().getDataType();
        for (String value : obj.getFilter().getValues()) {
            if (! Utils.isOfJdbcType(value, jdbcDataType)) {
                throw new CriterionColumnDataTypeAndFilterMismatchException(
                        Utils.getJdbcSqlType(jdbcDataType),
                        value
                );
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
