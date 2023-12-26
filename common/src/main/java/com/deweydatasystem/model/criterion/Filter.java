package com.deweydatasystem.model.criterion;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.deweydatasystem.model.criterion.Operator.isNotNull;
import static com.deweydatasystem.model.criterion.Operator.isNull;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class Filter implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * The values of the filter, such as 1, 2, 3 or "bob", "sally", etc.
     */
    private List<String> values = new ArrayList<>();

    /**
     * Sub query placeholders, which act as a placeholder/flag for the relevant SQL string to be added to the values field.
     */
    private String subQueryPlaceholder = "";

    /**
     * A placeholder for a criterion parameter.  Note that sub query placeholders (prefixed with "$") should have
     * already been replaced with the relevant SQL string in the values field.
     */
    private String parameter = "";

    public Filter(List<String> values) {
        this.values = values;
    }

    public void replaceParameter(List<String> valuesToAdd) {
        this.parameter = "";
        this.values.addAll(valuesToAdd);
    }

    public void replaceSubQueryPlaceholder(String subQuerySql) {
        this.subQueryPlaceholder = "";
        this.values.add(subQuerySql);
    }

    public boolean isEmpty() {
        return this.values.isEmpty() && this.subQueryPlaceholder.isEmpty() && this.parameter.isEmpty();
    }

    public boolean hasSubQueries() {
        return ! this.subQueryPlaceholder.isEmpty();
    }

    public String toSql(Operator operator) {
        StringBuilder sql = new StringBuilder();

        if (! this.parameter.isEmpty()) {
            return ":" + this.parameter + "";
        }

        if (this.values.isEmpty()) {
            return "";
        }

        if (operator.equals(isNull) || operator.equals(isNotNull)) {
            return "";
        }
        // todo:  Add this BETWEEN and NOT BETWEEN logic eventually.  Check that this logic is correct!
//        else if (operator.equals(between) || operator.equals(notBetween)) {
//            return sql.append(this.values.get(0)).append(" AND ").append(this.values.get(1))
//                    .toString();
//        }
        else {
            return sql.append("(").append(String.join(", ", this.values)).append(")")
                    .toString();
        }

    }

}
