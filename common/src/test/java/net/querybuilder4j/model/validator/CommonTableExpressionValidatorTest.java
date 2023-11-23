package net.querybuilder4j.model.validator;

import net.querybuilder4j.exceptions.UncleanSqlException;
import net.querybuilder4j.model.cte.CommonTableExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class CommonTableExpressionValidatorTest {

    private final CommonTableExpressionValidator commonTableExpressionValidator = new CommonTableExpressionValidator();

    @Test(expected = UncleanSqlException.class)
    public void isValid_failsIfNameIsNotCleanSql() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setName("DeLETe FrOm users WHere 1=1; --");

        commonTableExpressionValidator.isValid(commonTableExpression);
    }

    @Test
    public void isValid_noExceptionIsThrownIfNameIsCleanSql() {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setName("thisIsClean");

        commonTableExpressionValidator.isValid(commonTableExpression);

        assertTrue(true);
    }

}