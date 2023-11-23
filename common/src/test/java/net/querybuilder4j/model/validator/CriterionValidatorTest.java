package net.querybuilder4j.model.validator;

import net.querybuilder4j.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.criterion.Conjunction;
import net.querybuilder4j.model.criterion.Criterion;
import net.querybuilder4j.model.criterion.Filter;
import net.querybuilder4j.model.criterion.Operator;
import net.querybuilder4j.model.table.Table;
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
                        new Filter(List.of("1"), List.of(), List.of()),
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
                        new Filter(List.of("1"), List.of(), List.of()),
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
                        new Filter(List.of(), List.of(), List.of()),
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
                        new Filter(List.of("bob"), List.of(), List.of()),
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
                        new Filter(List.of("bob"), List.of(), List.of()),
                        null
                )
        );

        selectStatement.getCriteria().forEach(this.criterionSqlValidator::isValid);
    }

}