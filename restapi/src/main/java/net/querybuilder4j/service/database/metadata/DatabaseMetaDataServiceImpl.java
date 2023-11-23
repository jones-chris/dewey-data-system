package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.schema.Schema;
import net.querybuilder4j.model.table.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DatabaseMetaDataServiceImpl implements DatabaseMetaDataService {

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Autowired
    public DatabaseMetaDataServiceImpl(DatabaseMetadataCacheDao databaseMetadataCacheDao) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
    }

    @Override
    @LogExecutionTime
    public Set<Database> getDatabases() {
        return this.databaseMetadataCacheDao.getDatabases();
    }

    /**
     *
     * @param databaseName The database name.
     * @return {@link List<Schema>}
     */
    @Override
    @LogExecutionTime
    public List<Schema> getSchemas(String databaseName) {
        return this.databaseMetadataCacheDao.findSchemas(databaseName);
    }

    /**
     * Gets tables and views.
     *
     * @param databaseName The database name.
     * @param schemaName The schema name.
     * @return {@link List<Table>}
     */
    @Override
    @LogExecutionTime
    public List<Table> getTablesAndViews(String databaseName, String schemaName) {
        return this.databaseMetadataCacheDao.findTables(databaseName, schemaName);
    }

    /**
     * Because this service gets data from a SQLite database and SQLite does not have a concise SQL query for getting all table
     * columns, I have to write Java code to concatenate the table columns with the table name.
     *
     * @param databaseName The database name.
     * @param schemaName The schema name.
     * @param tableName The table name.
     * @return {@link List<Column>}
     */
    @Override
    @LogExecutionTime
    public List<Column> getColumns(String databaseName, String schemaName, String tableName) {
        return this.databaseMetadataCacheDao.findColumns(databaseName, schemaName, tableName);
    }

}
