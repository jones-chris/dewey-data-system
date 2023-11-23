package net.querybuilder4j.controller.database.metadata;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.schema.Schema;
import net.querybuilder4j.model.table.Table;
import net.querybuilder4j.service.database.metadata.DatabaseMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/metadata")
public class DatabaseMetadataController {

    private DatabaseMetaDataService databaseMetaDataService;

    @Autowired
    public DatabaseMetadataController(DatabaseMetaDataService databaseMetaDataService) {
        this.databaseMetaDataService = databaseMetaDataService;
    }

    /**
     * Returns all qb target data sources.
     *
     * @return A ResponseEntity containing a List of Database objects.
     */
    @GetMapping(value = "/database")
    @LogExecutionTime
    public ResponseEntity<Set<Database>> getDatabases() {
        Set<Database> databases = this.databaseMetaDataService.getDatabases();
        return ResponseEntity.ok(databases);
    }

    /**
     * Returns all database schemas that a given user has access to.
     *
     * @return A ResponseEntity containing a List of Schema objects.
     */
    @GetMapping(value = "/{database}/schema")
    @LogExecutionTime
    public ResponseEntity<List<Schema>> getSchemas(@PathVariable String database) {
        List<Schema> schemas = this.databaseMetaDataService.getSchemas(database);
        return ResponseEntity.ok(schemas);
    }

    /**
     * Returns all database tables and views that a given user has access to.
     *
     * @param database The database of the tables and views to retrieve.
     * @param schemas The schemas of the tables and views to retrieve.
     * @return A ResponseEntity containing a List of Table objects.
     */
    @GetMapping(value = "/{database}/{schemas}/table-and-view")
    @LogExecutionTime
    public ResponseEntity<List<Table>> getTablesAndViews(
            @PathVariable String database,
            @PathVariable String schemas
    ) {
        String[] splitSchemas = schemas.split("&");
        List<Table> allTables = new ArrayList<>();
        for (String schema : splitSchemas) {
            List<Table> tables = this.databaseMetaDataService.getTablesAndViews(database, schema);
            allTables.addAll(tables);
        }

        return ResponseEntity.ok(allTables);
    }

    /**
     * Returns all columns for any number of tables or views given a schema name and table/view name (user permissions apply).
     *
     * @param tables A List of Table objects for which to retrieve columns
     * @return A ResponseEntity containing a List of Column objects.
     */
    @PostMapping(value = "/{database}/{schema}/{tables}/column")
    @LogExecutionTime
    public ResponseEntity<List<Column>> getColumns(@RequestBody List<Table> tables) {
        List<Column> allColumns = new ArrayList<>();
        for (Table table : tables) {
            List<Column> columns = this.databaseMetaDataService.getColumns(table.getDatabaseName(), table.getSchemaName(), table.getTableName());
            allColumns.addAll(columns);
        }

        return ResponseEntity.ok(allColumns);
    }

}
