package com.deweydatasystem;


import com.deweydatasystem.config.DatabaseType;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.exceptions.CacheMissException;
import com.deweydatasystem.exceptions.DatabaseTypeNotRecognizedException;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;

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
