package net.querybuilder4j.sql.builder;


import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.validator.SelectStatementValidator;
import net.querybuilder4j.service.QueryTemplateService;

public class MySqlSqlBuilder extends SqlBuilder {

    public MySqlSqlBuilder(
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            QueryTemplateService queryTemplateService,
            SelectStatementValidator selectStatementSqlValidator
    ) {
        super(databaseMetadataCacheDao, queryTemplateService, selectStatementSqlValidator);
        beginningDelimiter = '`';
        endingDelimiter = '`';
    }

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
