package com.deweydatasystem.dao.database;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.schema.Schema;
import com.deweydatasystem.model.table.Table;

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
