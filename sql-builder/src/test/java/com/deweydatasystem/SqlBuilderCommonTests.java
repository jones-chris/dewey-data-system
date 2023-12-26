package com.deweydatasystem;

import com.deweydatasystem.config.DatabaseType;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Conjunction;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import com.deweydatasystem.model.criterion.Operator;
import com.deweydatasystem.model.cte.CommonTableExpression;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.join.Join;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class SqlBuilderCommonTests {

    protected SqlBuilder sqlBuilder;

    protected DatabaseMetadataCacheDao databaseMetadataCacheDao;

    protected QueryTemplateService queryTemplateService;

    protected SelectStatementValidator selectStatementSqlValidator;

    protected char beginningDelimiter;

    protected char endingDelimiter;

    @Test
    public void constructor_SetsFieldsCorrectly() {
        assertEquals(this.databaseMetadataCacheDao, this.sqlBuilder.databaseMetadataCacheDao);
        assertEquals(this.queryTemplateService, this.sqlBuilder.queryTemplateService);
        assertEquals(this.beginningDelimiter, this.sqlBuilder.beginningDelimiter);
        assertEquals(this.endingDelimiter, this.sqlBuilder.endingDelimiter);
    }

    @Test
    public void setStatement_setsFieldsCorrectly() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();

        this.sqlBuilder.withStatement(selectStatement);

        assertEquals(selectStatement, this.sqlBuilder.selectStatement);
    }

    @Test
    public void buildSql_callsAllSqlClauseGenerationMethods() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();

        this.sqlBuilder.withStatement(selectStatement)
                .build()
                .getParameterizedSql();

        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createCommonTableExpressionClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createSelectClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createFromClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createJoinClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createWhereClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createGroupByClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createOrderByClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createLimitClause();
        Mockito.verify(this.sqlBuilder, Mockito.times(1)).createOffsetClause();
    }

    @Test
    public void buildSql_sqlStringContainsBeginningAndEndingDelimitersCharacters() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();

        String sql = this.sqlBuilder.withStatement(selectStatement)
                .build()
                .getParameterizedSql();

        Assert.assertEquals(this.getExpectedSql(), sql.trim());
    }

    @Test
    public void createCommonTableExpressionClause_emptyListResultsInEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createCommonTableExpressionClause();

        assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createCommonTableExpressionClause_nonEmptyListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setName("name");
        commonTableExpression.setQueryName("cte1");
//        commonTableExpression.setSelectStatement(TestUtils.buildSelectStatement());
        selectStatement.getCommonTableExpressions().add(commonTableExpression);
        this.sqlBuilder.withStatement(selectStatement);
        Mockito.when(this.databaseMetadataCacheDao.findDatabases("database"))
                .thenReturn(
                        new Database("database", DatabaseType.MySql)
                );

        this.sqlBuilder.createCommonTableExpressionClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createJoinClause_nonEmptyListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        Join join = new Join();
        join.setJoinType(Join.JoinType.LEFT);
        join.setParentJoinColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        join.setParentTable(
                new Table("database", "schema", "table")
        );
        join.setTargetJoinColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        join.setTargetTable(
                new Table("database", "schema", "table")
        );
        selectStatement.getJoins().add(join);
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createJoinClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createWhereClause_nonEmptyListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column("database", "schema", "table", "column", 4, "alias"),
                        Operator.equalTo,
                        new Filter(
                                List.of("1")
                        ),
                        List.of()
                )
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createWhereClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createGroupByClause_isGroupByAndNonEmptyColumnListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setGroupBy(true);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createGroupByClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOrderByClause_ascendingAndNonEmptyColumnListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(true);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createOrderByClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOrderByClause_descendingAndNonEmptyColumnListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(false);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createOrderByClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createLimitClause_nonNullLimitResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        long limit = 10L;
        selectStatement.setLimit(limit);
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOffsetClause_nonNullOffsetResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        long offset = 10L;
        selectStatement.setOffset(offset);
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createOffsetClause();

        Assert.assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    private String getExpectedSql() {
        return String.format(
                "SELECT  %sschema%s.%stable%s.%scolumn%s AS alias  FROM  %sschema%s.%stable%s",
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter
        );
    }

}
