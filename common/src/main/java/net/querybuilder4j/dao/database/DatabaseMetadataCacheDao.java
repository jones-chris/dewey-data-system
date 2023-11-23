package net.querybuilder4j.dao.database;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.schema.Schema;
import net.querybuilder4j.model.table.Table;

import java.util.List;
import java.util.Set;

public interface DatabaseMetadataCacheDao {

    @LogExecutionTime
    void refreshCache() throws Exception;

    @LogExecutionTime
    Set<Database> getDatabases();

    @LogExecutionTime
    Database findDatabases(String databaseName);

    @LogExecutionTime
    List<Schema> findSchemas(String databaseName);

    @LogExecutionTime
    List<Table> findTables(String databaseName, String schemaName);

    @LogExecutionTime
    List<Column> findColumns(String databaseName, String schemaName, String tableName);

    @LogExecutionTime
    int getColumnDataType(Column column);

    @LogExecutionTime
    boolean columnExists(Column column);

    @LogExecutionTime
    boolean columnsExist(List<Column> columns);

    @LogExecutionTime
    Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName);

}
