package net.querybuilder4j.dao.database.metadata;

import lombok.Getter;
import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.exceptions.CacheRefreshException;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.schema.Schema;
import net.querybuilder4j.model.table.Table;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InMemoryDatabaseMetadataCacheDaoImpl implements DatabaseMetadataCacheDao {

    private final QbConfig qbConfig;

    private final DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao;

    @Getter
    private final Set<Database> cache = new HashSet<>();

    public InMemoryDatabaseMetadataCacheDaoImpl(
            QbConfig qbConfig,
            DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao
    ) {
        this.qbConfig = qbConfig;
        this.databaseMetadataCrawlerDao = databaseMetadataCrawlerDao;

        this.refreshCache();
    }

    /**
     * Run every 24 hours thereafter.  This method calls {@link DatabaseMetadataCrawlerDao#getTargetDataSourceMetadata(List)}
     * which retrieves database metadata (schemas, tables, and columns) and persists the metadata to this class' `cache` field.
     * This class eager loads this metadata.
     *
     * @throws CacheRefreshException If an exception is raised when querying one of the target data sources.
     */
    @Override
    @Scheduled(fixedRate = 8640000000L) // todo:  Try parameterize this somehow and/or create an API endpoint that admin clients can hit to initiate a cache refresh.
    public void refreshCache() throws CacheRefreshException {
        // Populate cache on cache instantiation - which should occur at app start up.
        // Clear cache all at once and save new database metadata list to cache.
        List<QbConfig.TargetDataSource> targetDataSources = this.qbConfig.getTargetDataSources();
        List<Database> databases = this.databaseMetadataCrawlerDao.getTargetDataSourceMetadata(targetDataSources);

        this.cache.clear();
        this.cache.addAll(databases);
    }

    /**
     * A convenience method that makes it clearer that the cache is a {@link Set<Database>}.
     * @return {@link Set<Database>} The databases in the cache.
     */
    @Override
    @LogExecutionTime
    public Set<Database> getDatabases() {
        return this.getCache();
    }

    @Override
    @LogExecutionTime
    public Database findDatabases(String databaseName) {
        return this.cache.stream()
                .filter(database -> database.getDatabaseName().equals(databaseName))
                .findAny()
                .orElseThrow(CacheMissException::new);
    }

    @Override
    @LogExecutionTime
    public List<Schema> findSchemas(String databaseName) {
        return this.findDatabases(databaseName).getSchemas();
    }

    @Override
    @LogExecutionTime
    public List<Table> findTables(String databaseName, String schemaName) {
        return this.findSchemas(databaseName).stream()
                .filter(schema -> schema.getSchemaName().equals(schemaName))
                .map(Schema::getTables)
                .findAny()
                .orElseThrow(CacheMissException::new);
    }

    @Override
    @LogExecutionTime
    public List<Column> findColumns(String databaseName, String schemaName, String tableName) {
        return this.findTables(databaseName, schemaName).stream()
                .filter(table -> table.getTableName().equals(tableName))
                .map(Table::getColumns) // todo: sort alphabetically.
                .findAny()
                .orElseThrow(CacheMissException::new);
    }

    @Override
    @LogExecutionTime
    public int getColumnDataType(Column column) {
        String databaseName = column.getDatabaseName();
        String schemaName = column.getSchemaName();
        String tableName = column.getTableName();
        String columnName = column.getColumnName();

        return this.findColumns(databaseName, schemaName, tableName).stream()
                .filter(col -> col.getColumnName().equals(columnName))
                .map(Column::getDataType)
                .findFirst()
                .orElseThrow(CacheMissException::new);
    }

    @Override
    @LogExecutionTime
    public boolean columnExists(Column column) {
        String databaseName = column.getDatabaseName();
        String schemaName = column.getSchemaName();
        String tableName = column.getTableName();
        String columnName = column.getColumnName();

        return this.findColumns(databaseName, schemaName, tableName).stream()
                .anyMatch(col -> col.getColumnName().equals(columnName));
    }

    @Override
    @LogExecutionTime
    public boolean columnsExist(List<Column> columns) {
        for (Column column : columns) {
            if (! this.columnExists(column)) {
                return false;
            }
        }

        return true;
    }

    @Override
    @LogExecutionTime
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) {
        return this.findColumns(databaseName, schemaName, tableName)
                .stream()
                .filter(column -> column.getColumnName().equals(columnName))
                .findFirst()
                .orElseThrow(CacheMissException::new);
    }

}