package com.deweydatasystem.model.cte;

import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.SqlRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class CommonTableExpression implements SqlRepresentation, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the {@link CommonTableExpression}.
     */
    @JsonProperty(value = "name")
    @NotNull
    @EqualsAndHashCode.Include
    private String name;

    /**
     * The name of the query to retrieve from the query template database.
     */
    @JsonProperty(value = "queryName")
    @NotNull
    private String queryName;

    /**
     * The version of the query to retrieve from the query template database.
     */
    @JsonProperty(value = "version")
    @NotNull
    private int version;

    /**
     * A {@link Map} with the parameters being the keys and the arguments being the values.
     */
    @JsonProperty(value = "parametersAndArguments")
    private Map<String, List<String>> parametersAndArguments = new HashMap<>();

    /**
     * The query's optional property overrides.  These differ from {@link SelectStatement}'s criteriaArguments in that every
     * {@link SelectStatement} has these optional property overrides whereas not all {@link SelectStatement} have the
     * same criteria parameters and arguments.
     */
    @JsonProperty(value = "overrides")
    private SelectStatement.PropertyOverrides overrides = new SelectStatement.PropertyOverrides();

    /**
     * The SQL string.  This is the SQL that should form the
     * WITH/Common Table Expression clause.
     */
    @JsonIgnore
    private String sql;

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        if (this.sql == null || this.sql.trim().isEmpty()) {
            throw new IllegalStateException("Common Table Expression's sql is null, a blank string, or an empty string");
        }

        return " " +
                this.name +
                " AS (" +
                this.sql +
                ")";
    }

    @JsonIgnore
    public boolean isBuilt() {
        return this.sql != null && !this.sql.trim().isEmpty();
    }

}
