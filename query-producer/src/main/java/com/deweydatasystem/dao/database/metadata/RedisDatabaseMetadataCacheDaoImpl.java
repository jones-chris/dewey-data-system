package com.deweydatasystem.dao.database.metadata;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.exceptions.CacheMissException;
import com.deweydatasystem.model.DatabaseMetadata;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.schema.Schema;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.utils.Utils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class RedisDatabaseMetadataCacheDaoImpl implements DatabaseMetadataCacheDao {

    static final String DATABASE_REDIS_KEY_TEMPLATE = "database/%s";

    static final String SCHEMA_REDIS_KEY_TEMPLATE = "schema/%s";

    static final String TABLE_REDIS_KEY_TEMPLATE = "table/%s";

    static final String COLUMN_REDIS_KEY_TEMPLATE = "column/%s";

    /**
     * The {@link QbConfig} encapsulating the application context.
     */
    private final QbConfig qbConfig;

    /**
     * The Redis client.
     */
    private final Jedis jedis;

    /**
     * The class responsible for reading metadata from the target databases.
     */
    private final DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao;

    @Autowired
    public RedisDatabaseMetadataCacheDaoImpl(
            QbConfig qbConfig,
            DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao,
            Jedis jedis
    ) {
        this.qbConfig = qbConfig;
        this.databaseMetadataCrawlerDao = databaseMetadataCrawlerDao;
        this.jedis = jedis;
    }

    @Override
    @LogExecutionTime
    public void refreshCache() {
        // Clear Redis.
        this.jedis.flushAll();

        // Loop through each database, get the schema, table/view, and column metadata and write it to Redis.
        // TODO:  Make this loop asynchronous for each database?
        for (QbConfig.TargetDataSource targetDataSource : this.qbConfig.getTargetDataSources()) {
            // Write the database metadata to Redis.
            Database database = new Database(targetDataSource.getName(), targetDataSource.getDatabaseType());
            String databaseKey = this.buildRedisKey(database);
            String databaseValue = Utils.serializeToJson(database);
            log.info("Crawler writing key {} with value {}", databaseKey, databaseValue);
            this.jedis.set(databaseKey, databaseValue);

            // Get schema metadata and write to Redis.
            List<Schema> schemas = this.databaseMetadataCrawlerDao.getSchemas(targetDataSource);
            schemas.forEach(schema -> {
                String schemaKey = this.buildRedisKey(schema);
                String schemaValue = Utils.serializeToJson(schema);
                log.info("Crawler writing key {} with value {}", schemaKey, schemaValue);
                this.jedis.set(schemaKey, schemaValue);
            });

            // Get tables metadata and write to Redis.
            for (Schema schema : schemas) {
                List<Table> tables = this.databaseMetadataCrawlerDao.getTablesAndViews(targetDataSource, schema.getSchemaName());

                tables.forEach(table -> {
                    String tableKey = this.buildRedisKey(table);
                    String tableValue = Utils.serializeToJson(table);
                    log.info("Crawler writing key {} with value {}", tableKey, tableValue);
                    this.jedis.set(tableKey, tableValue);
                });

                // Get columns
                for (Table table : tables) {
                    List<Column> columns = this.databaseMetadataCrawlerDao.getColumns(targetDataSource, table.getSchemaName(), table.getTableName());
                    columns.forEach(column -> {
                        String columnKey = this.buildRedisKey(column);
                        String columnValue = Utils.serializeToJson(column);
                        log.info("Crawler writing key {} with value {}", columnKey, columnValue);
                        this.jedis.set(columnKey, columnValue);
                    });
                }
            }
        }

    }

    @Override
    @LogExecutionTime
    public Set<Database> getDatabases() {
        return this.qbConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toSet());
    }

    @Override
    @LogExecutionTime
    public Database findDatabases(String databaseName) {
        String databaseKeyPattern = String.format(DATABASE_REDIS_KEY_TEMPLATE, databaseName);
        String databaseJson = this.jedis.get(databaseKeyPattern);

        return Utils.deserializeJson(databaseJson, Database.class);
    }

    @Override
    @LogExecutionTime
    public List<Schema> findSchemas(String databaseName) {
        // Get all Redis keys that start with `{databaseName}.*` (notice the trailing period before `*`.
        String schemaKeyPattern = String.format(SCHEMA_REDIS_KEY_TEMPLATE, databaseName) + ".*";
        Set<String> schemasRedisKeys = this.jedis.keys(schemaKeyPattern);

        if (schemasRedisKeys.isEmpty()) {
            throw new CacheMissException(
                    String.format("Could not find %s", schemaKeyPattern)
            );
        }

        // Get the values of the schemas redis keys.
        List<String> schemasJson = this.jedis.mget(schemasRedisKeys.toArray(new String[0]));

        return Utils.deserializeJsons(schemasJson, Schema.class);
    }

    @Override
    @LogExecutionTime
    public List<Table> findTables(String databaseName, String schemaName) {
        String tableKeyPattern = String.format(TABLE_REDIS_KEY_TEMPLATE, databaseName + "." + schemaName) + ".*";
        Set<String> tablesRedisKeys = this.jedis.keys(tableKeyPattern);

        if (tablesRedisKeys.isEmpty()) {
            throw new CacheMissException(
                    String.format("Could not find %s", tableKeyPattern)
            );
        }

        // Get the values of the schemas redis keys.
        List<String> tablesJson = this.jedis.mget(tablesRedisKeys.toArray(new String[0]));

        return Utils.deserializeJsons(tablesJson, Table.class);
    }

    @Override
    @LogExecutionTime
    public List<Column> findColumns(String databaseName, String schemaName, String tableName) {
        String columnKeyPattern = String.format(COLUMN_REDIS_KEY_TEMPLATE, databaseName + "." + schemaName + "." + tableName) + ".*";
//        String columnKeyPattern = String.format("%s.%s.%s.*", databaseName, schemaName, tableName);
        Set<String> columnsRedisKeys = this.jedis.keys(columnKeyPattern);

        if (columnsRedisKeys.isEmpty()) {
            throw new CacheMissException(
                    String.format("Could not find %s", columnKeyPattern)
            );
        }

        // Get the values of the schemas redis keys.
        List<String> columnsJson = this.jedis.mget(columnsRedisKeys.toArray(new String[0]));

        return Utils.deserializeJsons(columnsJson, Column.class);
    }

    @Override
    @LogExecutionTime
    public int getColumnDataType(Column column) {
        String columnJson = this.jedis.get(this.buildRedisKey(column));
        Column deserializedColumn = Utils.deserializeJson(columnJson, Column.class);
        return deserializedColumn.getDataType();
    }

    @Override
    @LogExecutionTime
    public boolean columnExists(Column column) {
        return this.jedis.exists(this.buildRedisKey(column));
    }

    @Override
    @LogExecutionTime
    public boolean columnsExist(List<Column> columns) {
        String[] fullyQualifiedColumnNames = columns.stream()
                .map(this::buildRedisKey)
                .toArray(String[]::new);

        long numberOfExistingKeys = this.jedis.exists(fullyQualifiedColumnNames);

        return numberOfExistingKeys == fullyQualifiedColumnNames.length;
    }

    @Override
    @LogExecutionTime
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) {
        String fullyQualifiedColumnName =  String.format(Column.FULLY_QUALIFIED_NAME_TEMPLATE, databaseName, schemaName, tableName, columnName);
        String key = String.format(COLUMN_REDIS_KEY_TEMPLATE, fullyQualifiedColumnName);

        log.info("Attempting to find column key {}", key);
        String columnJson = this.jedis.get(key);

        return Utils.deserializeJson(columnJson, Column.class);
    }

    private String buildRedisKey(@NonNull DatabaseMetadata databaseMetadata) {
        if (databaseMetadata instanceof Database) {
            return String.format(DATABASE_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName());
        }
        else if (databaseMetadata instanceof Schema) {
            return String.format(SCHEMA_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName());
        }
        else if (databaseMetadata instanceof Table) {
            return String.format(TABLE_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName());
        }
        else if (databaseMetadata instanceof Column) {
            return String.format(COLUMN_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName());
        }

        throw new IllegalArgumentException("Could not build Redis key for type " + databaseMetadata.getClass().getName());
    }

    private String buildRedisSearchKey(@NonNull DatabaseMetadata databaseMetadata) {
        if (databaseMetadata instanceof Database) {
            return String.format(DATABASE_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName()) + ".*";
        }
        else if (databaseMetadata instanceof Schema) {
            return String.format(SCHEMA_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName()) + ".*";
        }
        else if (databaseMetadata instanceof Table) {
            return String.format(TABLE_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName()) + ".*";
        }
        else if (databaseMetadata instanceof Column) {
            return String.format(COLUMN_REDIS_KEY_TEMPLATE, databaseMetadata.getFullyQualifiedName()) + ".*";
        }

        throw new IllegalArgumentException("Could not build Redis search key for type " + databaseMetadata.getClass().getName());
    }

}
