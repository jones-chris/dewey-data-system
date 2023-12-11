package com.deweydatasystem.model.column;

import com.deweydatasystem.model.DatabaseMetadata;
import com.deweydatasystem.model.SqlRepresentation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.deweydatasystem.model.validator.SqlValidator;

import java.io.Serializable;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Column implements SqlRepresentation, Serializable, DatabaseMetadata {

    public static final String FULLY_QUALIFIED_NAME_TEMPLATE = "%s.%s.%s.%s";

    private static final long serialVersionUID = 1L;

    @JsonProperty(required = true)
    @Getter
    @Setter
    private String fullyQualifiedName;

    @JsonProperty(required = true)
    @Getter
    @Setter
    private String databaseName;

    @JsonProperty(required = true)
    @Getter
    private String schemaName;

    @JsonProperty(required = true)
    @Getter
    @Setter
    private String tableName;

    @JsonProperty(required = true)
    @Getter
    @Setter
    private String columnName;

    @Getter
    @Setter
    private int dataType;

    private String alias;

    @JsonProperty
    @Getter
    @Setter
    private Boolean isIndexed; // Needs to be serialized for responses, but not requests.

    public Column(
            String databaseName,
            String schemaName,
            String tableName,
            String columnName,
            int dataType,
            String alias
    ) {
        this(databaseName, schemaName, tableName, columnName, dataType, alias, null);
    }

    public Column(
            String databaseName,
            String schemaName,
            String tableName,
            String columnName,
            int dataType,
            String alias,
            Boolean isIndexed
    ) {
        // Some databases, like SQLite, do not have schemas, so change the schema name to "null" instead of null because
        // SQLite primary keys (which is used for the cache) cannot have null values.
        if (schemaName == null) {
            schemaName = "null";
        }

        this.fullyQualifiedName = String.format("%s.%s.%s.%s", databaseName, schemaName, tableName, columnName);
        this.setDatabaseName(databaseName);
        this.setSchemaName(schemaName);
        this.setTableName(tableName);
        this.setColumnName(columnName);
        this.setDataType(dataType);
        this.setAlias(alias);
        this.setIsIndexed(isIndexed);
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = (schemaName == null) ? "null" : schemaName;
    }

    public String getAlias() {
        if (this.alias == null || this.alias.isBlank()) {
            if (this.columnName == null || this.columnName.isBlank()) {
                throw new IllegalStateException("columnName and alias cannot both be null");
            } else {
                return this.columnName;
            }
        } else {
            return this.alias;
        }
    }

    public void setAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            this.alias = this.columnName;
        } else {
            this.alias = alias;
        }
    }

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        if (this.schemaName == null || this.schemaName.equals("null")) {
            return String.format(" %s%s%s.%s%s%s AS %s ",
                    beginningDelimiter, SqlValidator.escape(this.tableName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.columnName), endingDelimiter,
                    this.getAlias());
        } else {
            return String.format(" %s%s%s.%s%s%s.%s%s%s AS %s ",
                    beginningDelimiter, SqlValidator.escape(this.schemaName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.tableName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.columnName), endingDelimiter,
                    this.getAlias());
        }

    }

    /**
     * Returns the SQL representation of the column without an Alias clause (`AS {column_alias}`)
     *
     * @param beginningDelimiter The beginning delimiter for the SQL dialect.
     * @param endingDelimiter The ending delimiter for the SQL dialect.
     * @return The SQL representation of the {@link Column}.
     */
    public String toSqlWithoutAlias(char beginningDelimiter, char endingDelimiter) {
        if (this.schemaName == null || this.schemaName.equals("null")) {
            return String.format(" %s%s%s.%s%s%s ",
                    beginningDelimiter, SqlValidator.escape(this.tableName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.columnName), endingDelimiter);
        } else {
            return String.format(" %s%s%s.%s%s%s.%s%s%s ",
                    beginningDelimiter, SqlValidator.escape(this.schemaName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.tableName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.columnName), endingDelimiter);
        }
    }

}
