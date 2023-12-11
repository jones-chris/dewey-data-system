package com.deweydatasystem.model.schema;

import com.deweydatasystem.model.DatabaseMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import com.deweydatasystem.model.table.Table;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Schema implements DatabaseMetadata {

    private String fullyQualifiedName;

    private String databaseName;

    private String schemaName;

    @JsonIgnore
    private List<Table> tables = new ArrayList<>();

    public Schema(String databaseName, String schemaName) {
        this.fullyQualifiedName = String.format("%s.%s", databaseName, schemaName);
        this.databaseName = databaseName;
        this.schemaName = schemaName;
    }

}
