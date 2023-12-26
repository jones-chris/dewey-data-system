package com.deweydatasystem.model.validator;

import com.deweydatasystem.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Conjunction;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import com.deweydatasystem.model.criterion.Operator;
import com.deweydatasystem.model.table.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CriterionValidatorTest {

    private SqlValidator<Criterion> criterionSqlValidator = new CriterionValidator();

    @Test(expected = IllegalStateException.class)
    public void isValid_criteriaColumnIsNullThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        null,
                        Operator.equalTo,
                        new Filter(List.of("1")),
                        null
                )
        );

        selectStatement.getCriteria().forEach(this.criterionSqlValidator::isValid);
    }

    @Test(expected = IllegalStateException.class)
    public void isValid_criteriaOperatorIsNullThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(),
                        null,
                        new Filter(List.of("1")),
                        null
                )
        );

        selectStatement.getCriteria().forEach(this.criterionSqlValidator::isValid);
    }

    @Test(expected = IllegalStateException.class)
    public void isValid_criteriaFilterValuesAndSubQueriesAreEmptyThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(),
                        Operator.equalTo,
                        new Filter(List.of()),
                        null
                )
        );

        selectStatement.getCriteria().forEach(this.criterionSqlValidator::isValid);
    }

    @Test(expected = CriterionColumnDataTypeAndFilterMismatchException.class)
    public void isValid_criteriaNumericalDataTypeCannotBeParsedThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column("database", "schema", "table", "column", 4, "alias"),
                        Operator.equalTo,
                        new Filter(List.of("bob")),
                        null
                )
        );

        selectStatement.getCriteria().forEach(this.criterionSqlValidator::isValid);
    }

    @Test(expected = CriterionColumnDataTypeAndFilterMismatchException.class)
    public void isValid_criteriaBooleanDataTypeCannotBeParsedThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column("database", "schema", "table", "column", 16, "alias"),
                        Operator.equalTo,
                        new Filter(List.of("bob")),
                        null
                )
        );

        selectStatement.getCriteria().forEach(this.criterionSqlValidator::isValid);
    }

}