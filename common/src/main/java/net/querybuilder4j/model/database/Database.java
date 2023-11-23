package net.querybuilder4j.model.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.model.schema.Schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Database implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private String fullyQualifiedName;

    private String databaseName;

    private DatabaseType databaseType;

    @JsonIgnore
    private List<Schema> schemas = new ArrayList<>();

    public Database(String databaseName, DatabaseType databaseType) {
        this.fullyQualifiedName = String.format("%s", databaseName);
        this.databaseName = databaseName;
        this.databaseType = databaseType;
    }

}
