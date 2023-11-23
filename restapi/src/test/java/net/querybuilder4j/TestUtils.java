package net.querybuilder4j;

import net.querybuilder4j.config.DataConfigTest;
import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.criterion.Conjunction;
import net.querybuilder4j.model.criterion.Criterion;
import net.querybuilder4j.model.criterion.Filter;
import net.querybuilder4j.model.criterion.Operator;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.table.Table;
import net.querybuilder4j.sql.builder.SqlBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestUtils {

    /**
     * Builds a mock {@link SqlBuilder} with mocked results for all the {@link SqlBuilder} methods, except for {@link SqlBuilder#getSql()}
     * so that the {@link SqlBuilder} builder pattern can be tested easily throughout the test suite.
     *
     * @return {@link SqlBuilder}
     */
    public static SqlBuilder buildSqlBuilderMock(String getSqlResult) {
        SqlBuilder sqlBuilder = Mockito.mock(SqlBuilder.class);
        when(sqlBuilder.withoutRulesValidation())
                .thenReturn(sqlBuilder);
        when(sqlBuilder.withStatement(any(SelectStatement.class)))
                .thenReturn(sqlBuilder);
        when(sqlBuilder.build())
                .thenReturn(sqlBuilder);
        when(sqlBuilder.getSql())
                .thenReturn(getSqlResult);

        return sqlBuilder;
    }

    public static void loadSystemProperties() {
        URL fileUrl = DataConfigTest.class.getClassLoader().getResource("4ajr.yaml");
        assertNotNull(fileUrl);

        System.setProperty("CONFIG_FILE_PATH", fileUrl.getPath());
    }

    // todo:  remove this duplicated method and use the common TestUtils method instead.
    public static SelectStatement buildSelectStatement() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(
                new Database("database", DatabaseType.MySql)
        );
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        selectStatement.setMetadata(
                new SelectStatement.Metadata()
        );

        return selectStatement;
    }

    // todo:  remove this duplicated method and use the common TestUtils method instead.
    public static Column buildColumn(int dataType) {
        return new Column("database", "schema", "table", "column", dataType, "alias");
    }

    // todo:  remove this duplicated method and use the sql-builder TestUtils method instead.
    public static Criterion buildCriterion(Column column, Filter filter) {
        return new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, List.of());
    }

    public static class UuidMatcher extends TypeSafeMatcher<String> {

        @Override
        protected boolean matchesSafely(String s) {
            // Remove wrapping double quotes if they exist.
            if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                s = s.replaceAll("\"", "");
            }

            try {
                UUID.fromString(s);
                return true;
            }
            catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("only strings that can be converted to a UUID using UUID#fromString");
        }

        public static Matcher<String> isUuid() {
            return new UuidMatcher();
        }

    }

}
