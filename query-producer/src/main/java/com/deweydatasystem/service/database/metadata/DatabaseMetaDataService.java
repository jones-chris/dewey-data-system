package com.deweydatasystem.service.database.metadata;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.schema.Schema;
import com.deweydatasystem.model.table.Table;

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
