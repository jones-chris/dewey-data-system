package com.deweydatasystem.model.validator;

import com.deweydatasystem.exceptions.UncleanSqlException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SqlValidatorTest {

    @Test(expected = UncleanSqlException.class)
    public void assertSqlIsClean_reservedOperatorThrowsException() {
        SqlValidator.assertSqlIsClean("+blah+blah");
    }

    @Test(expected = UncleanSqlException.class)
    public void assertSqlIsClean_forbiddenMarksThrowsException() {
        SqlValidator.assertSqlIsClean("`blah`blah");
    }

    @Test(expected = UncleanSqlException.class)
    public void assertSqlIsClean_ansiKeywordsThrowsException() {
        SqlValidator.assertSqlIsClean("blahUPDATE blah blah");
    }

    @Test
    public void assertSqlIsClean_ansiKeywordsWithoutSurroundingWhitespaceReturnsTrue() {
        SqlValidator.assertSqlIsClean("blahUPDATEblah blah");
    }

}