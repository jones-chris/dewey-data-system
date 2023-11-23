package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.schema.Schema;
import net.querybuilder4j.model.table.Table;

import java.util.List;
import java.util.Set;

public interface DatabaseMetaDataService {

    @LogExecutionTime
    Set<Database> getDatabases();

    @LogExecutionTime
    List<Schema> getSchemas(String databaseName);

    @LogExecutionTime
    List<Table> getTablesAndViews(String databaseName, String schema);

    @LogExecutionTime
    List<Column> getColumns(String databaseName, String schema, String table) ;

}
