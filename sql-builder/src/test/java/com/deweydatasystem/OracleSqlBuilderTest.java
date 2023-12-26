package com.deweydatasystem;

import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Conjunction;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import com.deweydatasystem.model.criterion.Operator;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.model.validator.SelectStatementValidator;
import com.deweydatasystem.service.QueryTemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class OracleSqlBuilderTest extends SqlBuilderCommonTests {

    public OracleSqlBuilderTest() {
        this.databaseMetadataCacheDao = Mockito.mock(DatabaseMetadataCacheDao.class);
        this.queryTemplateService = Mockito.mock(QueryTemplateService.class);
        this.selectStatementSqlValidator = Mockito.mock(SelectStatementValidator.class);
        this.sqlBuilder = Mockito.spy(
                new OracleSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                )
        );
        this.beginningDelimiter = '"';
        this.endingDelimiter = '"';
    }

    @Test
    public void createLimitClause_noLimitGeneratesEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        Assert.assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createLimitClause_noCriteriaGeneratesStringWithWhereClause() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long limit = 10L;
        selectStatement.setLimit(limit);
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        Assert.assertEquals(
                String.format(" WHERE ROWNUM < %s", limit),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

    @Test
    public void createLimitClause_criteriaGeneratesStringWithWhereClause() {
        Column column = new Column("database", "schema", "table", "column", 4, "alias");
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(column);
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long limit = 10L;
        selectStatement.setLimit(limit);
        Filter filter = new Filter(
                List.of("1")
        );
        selectStatement.getCriteria().add(
                new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, List.of())
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        Assert.assertEquals(
                String.format(" AND ROWNUM < %s", limit),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

    @Test
    public void createOffsetClause_noOffsetGeneratesEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createOffsetClause();

        Assert.assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOffsetClause_nonNullOffsetGeneratesNonEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long offset = 10L;
        selectStatement.setOffset(offset);
        this.sqlBuilder.withStatement(selectStatement);

        this.sqlBuilder.createOffsetClause();

        Assert.assertEquals(
                String.format(" OFFSET %s ROWS ", offset),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

}