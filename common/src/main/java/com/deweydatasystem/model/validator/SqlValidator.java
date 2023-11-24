package com.deweydatasystem.model.validator;

import com.deweydatasystem.model.SelectStatement;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.exceptions.UncleanSqlException;

/**
 * This class contains functions that validate a {@link SelectStatement} before it is built.
 */
@Slf4j
public abstract class SqlValidator<T> {

    // These characters are the only characters that should be escaped because they can be expected to be in query criteria.
    //  Ex:  SELECT * FROM restaurants WHERE name = 'Tiffany''s';
    private static final Character[] charsNeedingEscaping = new Character[] {'\''};

    // SQL arithmetic, bitwise, comparison, and compound operators per https://www.w3schools.com/sql/sql_operators.asp
    // If any of these strings are contained in SelectStatement, then return false.
    // NOTE:  On 2/6/2022, I removed "-" so that UUIDs are not flagged as unclean SQL.
    private static final String[] reservedOperators = new String[] {"+", "*", "/", "&", "|", "^", "=", ">", "<", "!=",
            "<>", ">=", "<=", "+=", "-=", "*=", "/=", "%=", "&=", "^-=", "|*="
    };

    // If any of these characters are contained in SelectStatement, then return false.
    private static final String[] forbiddenMarks = new String[] {";", "`", "\""};

    // Forbidden ANSI keywords that retrieve or change data.  These should not be present in the criterion filter values.
    private static final String[] ansiKeywords = new String[] {
            "UPDATE", "INSERT", "DROP", "DELETE", "SELECT"
    };

    private static final String[] destructiveKeywords = new String[] {
            "UPDATE", "INSERT", "DROP", "DELETE"
    };

    public abstract void isValid(@NonNull T obj);

    /**
     * Escapes SQL characters that need escaping.
     *
     * @param sql {@link String}
     * @return The escaped {@link String}
     */
    public static String escape(String sql) {
        for (Character c : charsNeedingEscaping) {
            sql = sql.replaceAll(c.toString(), c.toString() + c.toString());
        }

        return sql;
    }

    /**
     * Checks that a {@link String} does not contain reserved ANSI operators, forbidden marks, and keywords.  This is
     * not intended to validate raw SQL {@link String}s, but rather {@link SelectStatement} (and it's dependent objects)
     * fields.
     *
     * @param str {@link String}
     */
    public static void assertSqlIsClean(String str) {
        final String upperCaseStr = str.toUpperCase();

        for (String opr : reservedOperators) {
            if (upperCaseStr.contains(opr)) {
                log.error("A reserved operator, {}, was found in {}", opr, upperCaseStr);
                throw new UncleanSqlException();
            }
        }

        for (String mark : forbiddenMarks) {
            if (upperCaseStr.contains(mark)) {
                log.error("A forbidden mark, {}, was found in {}", mark, upperCaseStr);
                throw new UncleanSqlException();
            }
        }

        for (String keyword : ansiKeywords) {
            String upperCaseKeywordWithTrailingSpace = keyword.toUpperCase() + " ";
            String upperCaseKeywordWithStartingSpace = " " + keyword.toUpperCase();
            if (upperCaseStr.contains(upperCaseKeywordWithStartingSpace) || upperCaseStr.contains(upperCaseKeywordWithTrailingSpace)) {
                log.error("A keyword, {}, was found in {}", keyword, upperCaseStr);
                throw new UncleanSqlException();
            }
        }
    }

    /**
     * Only checks that a {@link String} does not contain "destructive" ANSI keywords.  This is intended to be validate
     * raw SQL {@link String}s.
     *
     * @param str The raw SQL {@link String}
     */
    public static void assertSqlIsNotDestructive(String str) {
        final String upperCaseStr = str.toUpperCase();

        for (String keyword : destructiveKeywords) {
            String upperCaseKeywordWithTrailingSpace = keyword.toUpperCase() + " ";
            String upperCaseKeywordWithStartingSpace = " " + keyword.toUpperCase();
            if (upperCaseStr.contains(upperCaseKeywordWithStartingSpace) || upperCaseStr.contains(upperCaseKeywordWithTrailingSpace)) {
                log.error("A destructive keyword, {}, was found in {}", keyword, upperCaseStr);
                throw new UncleanSqlException();
            }
        }
    }

}
