package com.deweydatasystem;


import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;

public class SqlServerSqlBuilder extends SqlBuilder {

    public SqlServerSqlBuilder(
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            QueryTemplateService queryTemplateService,
            SelectStatementValidator selectStatementSqlValidator
    ) {
        super(databaseMetadataCacheDao, queryTemplateService, selectStatementSqlValidator);
        beginningDelimiter = '[';
        endingDelimiter = ']';
    }

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
        this.createOffsetClause();
        this.createLimitClause();

        return this.stringBuilder.toString();
    }

    @Override
    @LogExecutionTime
    protected void createOffsetClause() {
        Long offset = this.selectStatement.getOffset();

        if (offset != null) {
            this.stringBuilder.append(" OFFSET ").append(offset).append(" ROWS ");
        }
    }

    @Override
    @LogExecutionTime
    protected void createLimitClause() {
        Long limit = this.selectStatement.getLimit();

        if (limit != null) {
            this.stringBuilder.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY ");
        }
    }

}
