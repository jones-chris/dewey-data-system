package net.querybuilder4j.sql.builder;


import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.exceptions.DatabaseTypeNotRecognizedException;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.validator.SelectStatementValidator;
import net.querybuilder4j.service.QueryTemplateService;

import java.util.Optional;

public class SqlBuilderFactory {

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    private QueryTemplateService queryTemplateService;

    private SelectStatementValidator selectStatementSqlValidator;

    public SqlBuilderFactory(
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            QueryTemplateService queryTemplateService,
            SelectStatementValidator selectStatementSqlValidator
    ) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
        this.queryTemplateService = queryTemplateService;
        this.selectStatementSqlValidator = selectStatementSqlValidator;
    }

    /**
     * Builds a {@link SqlBuilder} given a database name.
     *
     * @param databaseName The name of the database to build a {@link SqlBuilder} for.
     * @return {@link SqlBuilder}
     */
    public SqlBuilder buildSqlBuilder(String databaseName) {
        // Get the database type from the cache rather than trusting what the client sends us.
        Optional<Database> database = Optional.ofNullable(this.databaseMetadataCacheDao.findDatabases(databaseName));
        DatabaseType databaseType;
        if (database.isPresent()) {
            databaseType = database.get().getDatabaseType();
        } else {
            throw new CacheMissException("Database not found, " + databaseName);
        }

        switch (databaseType) {
            case MySql:
                return new MySqlSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                );
            case Oracle:
                return new OracleSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                );
            case PostgreSQL:
                return new PostgresSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                );
            case SqlServer:
                return new SqlServerSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                );
            case Sqlite:
                return new SqliteSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                );
            default:
                throw new DatabaseTypeNotRecognizedException(databaseType);
        }
    }

}
