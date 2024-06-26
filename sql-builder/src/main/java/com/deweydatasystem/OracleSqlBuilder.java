package com.deweydatasystem;


import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;

public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder(
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            QueryTemplateService queryTemplateService,
            SelectStatementValidator selectStatementSqlValidator
    ) {
        super(databaseMetadataCacheDao, queryTemplateService, selectStatementSqlValidator);
        beginningDelimiter = '"';
        endingDelimiter = '"';
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

        // Limit is a WHERE clause in Oracle SQL.
        this.createLimitClause();

        this.createGroupByClause();
        this.createOrderByClause();
        this.createOffsetClause();

        return this.stringBuilder.toString();
    }

    @Override
    @LogExecutionTime
    protected void createLimitClause() {
        Long limit = this.selectStatement.getLimit();

        if (limit != null) {
            if (this.selectStatement.getCriteria().isEmpty()) {
                this.stringBuilder.append(" WHERE ROWNUM < ").append(limit);
            } else {
                this.stringBuilder.append(" AND ROWNUM < ").append(limit);
            }

        }
    }

    @Override
    @LogExecutionTime
    protected void createOffsetClause() {
        Long offset = this.selectStatement.getOffset();

        if (offset != null) {
            this.stringBuilder.append(" OFFSET ").append(offset).append(" ROWS ");
        }
    }

}
