package com.deweydatasystem;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;

public class PostgresSqlBuilder extends SqlBuilder {

    public PostgresSqlBuilder(
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
    public String getParameterizedSql() {
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
