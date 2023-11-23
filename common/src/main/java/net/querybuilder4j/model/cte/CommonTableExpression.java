package net.querybuilder4j.model.cte;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.SqlRepresentation;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Getter
    @Setter
    @EqualsAndHashCode.Include
    private String name;

    /**
     * The name of the query to retrieve from the query template database.
     */
    @JsonProperty(value = "queryName")
    @NotNull
    @Getter
    @Setter
    private String queryName;

    /**
     * The version of the query to retrieve from the query template database.
     */
    @JsonProperty(value = "version")
    @NotNull
    @Getter
    @Setter
    private int version;

    /**
     * A {@link Map} with the parameters being the keys and the arguments being the values.
     */
    @JsonProperty(value = "parametersAndArguments")
    @Getter
    @Setter
    private Map<String, List<String>> parametersAndArguments = new HashMap<>();

    /**
     * The Common Table Expression's {@link SelectStatement}.
     */
    @JsonIgnore
    @Getter
    @Setter
    private SelectStatement selectStatement;

    /**
     * The query's optional property overrides.  These differ from {@link SelectStatement}'s criteriaArguments in that every
     * {@link SelectStatement} has these optional property overrides whereas not all {@link SelectStatement} have the
     * same criteria parameters and arguments.
     */
    @JsonProperty(value = "overrides")
    @Getter
    @Setter
    private SelectStatement.PropertyOverrides overrides = new SelectStatement.PropertyOverrides();

    /**
     * The {@link CommonTableExpression#selectStatement} built as a SQL string.  This is the SQL that should form the
     * WITH/Common Table Expression clause.
     */
    @JsonIgnore
    @Getter
    @Setter
    private String sql;

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        if (this.sql == null || this.sql.trim().equals("")) {
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
        return this.sql != null && ! this.sql.trim().equals("");
    }

}
