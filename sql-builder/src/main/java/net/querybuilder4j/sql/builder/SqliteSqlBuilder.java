package net.querybuilder4j.sql.builder;


import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.validator.SelectStatementValidator;
import net.querybuilder4j.service.QueryTemplateService;

public class SqliteSqlBuilder extends SqlBuilder {

    public SqliteSqlBuilder(
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            QueryTemplateService queryTemplateService,
            SelectStatementValidator selectStatementSqlValidator
    ) {
        super(databaseMetadataCacheDao, queryTemplateService, selectStatementSqlValidator);
        beginningDelimiter = '"';
        endingDelimiter = '"';
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    @LogExecutionTime
    public String getSql() {
        this.assertIsBuilt();

        this.createCommonTableExpressionClause();
        this.createSelectClause();
        this.createFromClause();
        this.createJoinClause();
        this.createWhereClause();
        this.createGroupByClause();
        this.createOrderByClause();
        this.createLimitClause();
        this.createOffsetClause();

        return this.stringBuilder.toString();
    }

}
