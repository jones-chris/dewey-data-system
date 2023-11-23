package net.querybuilder4j.model.validator;

import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.table.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TableValidatorTest {

    private final TableValidator tableValidator = new TableValidator();

    @Test(expected = IllegalArgumentException.class)
    public void assertIsValid_nullTableThrowsException() {
        SelectStatement selectStatement = new SelectStatement();

        this.tableValidator.isValid(selectStatement.getTable());
    }

    @Test
    public void assertIsValid_nonNullTablePasses() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setTable(
                new Table()
        );

        this.tableValidator.isValid(selectStatement.getTable());
    }

}