package com.deweydatasystem.model;

import com.deweydatasystem.model.criterion.Criterion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.CriteriaTreeFlattener;
import com.deweydatasystem.model.criterion.CriterionParameter;
import com.deweydatasystem.model.cte.CommonTableExpression;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.join.Join;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.utils.CriteriaDeserializer;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SelectStatement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Metadata about the {@link SelectStatement}.
     */
    @JsonProperty(value = "metadata")
    private Metadata metadata;

    /**
     * The database metadata object that can be used to find the database that the SelectStatement is intended to be
     * executed against.
     */
    @JsonProperty(value = "database", required = true)
    private Database database;

    /**
     * A {@link List<CommonTableExpression>} or also known as WITH statements.
     */
    @JsonProperty(value = "commonTableExpressions")
    private final List<CommonTableExpression> commonTableExpressions = new ArrayList<>();

    /**
     * The columns in the SELECT SQL clause.
     */
    @JsonProperty(value = "columns", required = true)
    private final List<Column> columns = new ArrayList<>();

    /**
     * The table in the FROM SQL clause.
     */
    @JsonProperty(value = "table", required = true)
    private Table table;

    /**
     * The criteria in the WHERE SQL clause.
     */
    @JsonDeserialize(using = CriteriaDeserializer.class)
    @JsonProperty(value = "criteria")
    private final List<Criterion> criteria = new ArrayList<>();

    /**
     * The joins in the JOIN SQL clause.
     */
    @JsonProperty(value = "joins")
    private final List<Join> joins = new ArrayList<>();

    /**
     * Whether to include DISTINCT in the SELECT SQL clause
     */
    @JsonProperty(value = "distinct")
    private boolean distinct;

    /**
     * Whether to build the GROUP BY SQL clause.
     */
    @JsonProperty(value = "groupBy")
    private boolean groupBy;

    /**
     * Whether to build the ORDER BY SQL clause.
     */
    @JsonProperty(value = "orderBy")
    private boolean orderBy;

    /**
     * Whether to order by ascending or descending in the ORDER BY SQL clause.
     */
    @JsonProperty(value = "ascending")
    private boolean ascending;

    /**
     * The limit in the LIMITS SQL clause.
     */
    @JsonProperty(value = "limit")
    private Long limit = null;

    /**
     * The offset in the OFFSET SQL clause.
     */
    @JsonProperty(value = "offset")
    private Long offset = null;

    /**
     * Whether to add criteria to the WHERE SQL clause to exclude records that would have NULL values for all columns
     * in the SELECT SQL clause.
     */
    @JsonProperty(value = "suppressNulls")
    private boolean suppressNulls;

    /**
     * The query's criteria runtime arguments.  The key is the name of the parameter to find in the query criteria.  The
     * value is what will be passed into the query criteria.
     */
    @JsonProperty(value = "arguments")
    private Map<String, List<String>> criteriaArguments = new HashMap<>();

    /**
     * The query's optional property overrides.  These differ from {@link SelectStatement#criteriaArguments} in that every {@link SelectStatement}
     * has these optional property overrides whereas not all {@link SelectStatement} have the same criteria parameters and arguments.
     */
    @JsonProperty(value = "overrides")
    private PropertyOverrides overrides = new PropertyOverrides();

    @JsonIgnore
    public List<Criterion> getFlattenedCriteria() {
        return CriteriaTreeFlattener.flattenCriteria(this.criteria, new HashMap<>())
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Data
    public static class PropertyOverrides implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty(value = "onlyColumns")
        private List<Column> onlyColumns = new ArrayList<>();

        @JsonProperty(value = "limit")
        private Long limit;

    }

    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class Metadata implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The name of the SelectStatement.
         */
        @JsonProperty(value = "name", required = true)
        @Size(max = 50, message = "Select Statement name cannot exceed 50 characters")
        private String name = "";

        /**
         * The description of the SelectStatement.
         */
        @JsonProperty(value = "description", required = true)
        @Size(max = 1000, message = "Select Statement description cannot exceed 1000 characters")
        private String description;

        /**
         * The version of the SelectStatement.
         */
        private int version;

        /**
         * The author of the SelectStatement.
         */
        private String author = "qb4j"; // todo:  change this to read the request's JWT once auth is added.

        /**
         * Whether the SelectStatement can be discovered by users.
         */
        @JsonProperty(value = "isDiscoverable", required = true)
        private boolean isDiscoverable;

        /**
         * The query's criteria parameters.  The key is the name of the parameter to find in the SelectStatement's criteria.
         * The value is a description of the parameter that users choose in the front end app's UI.
         */
        @JsonProperty(value = "parameters")
        private List<CriterionParameter> criteriaParameters = new ArrayList<>();

        /**
         * The number of columns this query returns.
         */
        @JsonProperty(value = "numberOfColumnsReturned", required = true)
        @Size(min = 1, message = "Select Statement must return at least 1 column")
        private int numberOfColumnsReturned;

        /**
         * The maximum number of rows that could be returned.  This is the same as {@link SelectStatement#limit}.
         */
        @JsonProperty(value = "maxNumberOfRowsReturned", required = true)
        @Size(min = 1, message = "Select Statement must return at least 1 row")
        private long maxNumberOfRowsReturned;

        /**
         * The columns that the query returns.  This is the same as {@link SelectStatement#columns}.
         */
        @JsonProperty(value = "columns", required = true)
        @Size(min = 1, message = "Select Statement must return at least 1 column")
        private List<Column> columns;

    }

}
