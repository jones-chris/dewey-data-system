package com.deweydatasystem;

import com.deweydatasystem.config.DatabaseType;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.exceptions.CacheMissException;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SqlBuilderFactoryTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Mock
    private QueryTemplateService queryTemplateService;

    @Mock
    private SelectStatementValidator selectStatementValidator;

    @InjectMocks
    private SqlBuilderFactory sqlBuilderFactory;

    @Test(expected = CacheMissException.class)
    public void buildSqlBuilder_databaseNotFoundInCacheThrowsException() {
        Mockito.when(this.databaseMetadataCacheDao.findDatabases(ArgumentMatchers.anyString()))
                .thenReturn(null);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(
                new Database("database", DatabaseType.PostgreSQL)
        );

        this.sqlBuilderFactory.buildSqlBuilder(selectStatement.getDatabase().getDatabaseName());
    }

    @Test
    public void buildSqlBuilder_mySql() {
        Database database = new Database("mySqlDatabase", DatabaseType.MySql);
        Mockito.when(this.databaseMetadataCacheDao.findDatabases(ArgumentMatchers.anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement.getDatabase().getDatabaseName());

        Assert.assertTrue(sqlBuilder instanceof MySqlSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_oracle() {
        Database database = new Database("mySqlDatabase", DatabaseType.Oracle);
        Mockito.when(this.databaseMetadataCacheDao.findDatabases(ArgumentMatchers.anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement.getDatabase().getDatabaseName());

        Assert.assertTrue(sqlBuilder instanceof OracleSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_postgreSql() {
        Database database = new Database("mySqlDatabase", DatabaseType.PostgreSQL);
        Mockito.when(this.databaseMetadataCacheDao.findDatabases(ArgumentMatchers.anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement.getDatabase().getDatabaseName());

        Assert.assertTrue(sqlBuilder instanceof PostgresSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_sqlServer() {
        Database database = new Database("mySqlDatabase", DatabaseType.SqlServer);
        Mockito.when(this.databaseMetadataCacheDao.findDatabases(ArgumentMatchers.anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement.getDatabase().getDatabaseName());

        Assert.assertTrue(sqlBuilder instanceof SqlServerSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_sqlite() {
        Database database = new Database("mySqlDatabase", DatabaseType.Sqlite);
        Mockito.when(this.databaseMetadataCacheDao.findDatabases(ArgumentMatchers.anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement.getDatabase().getDatabaseName());

        Assert.assertTrue(sqlBuilder instanceof SqliteSqlBuilder);
    }

}