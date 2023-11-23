package net.querybuilder4j;

import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.criterion.Conjunction;
import net.querybuilder4j.model.criterion.Criterion;
import net.querybuilder4j.model.criterion.Filter;
import net.querybuilder4j.model.criterion.Operator;
import net.querybuilder4j.model.cte.CommonTableExpression;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.table.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestUtils {

    // todo: Remove this and use the common module's method.
    public static Column buildColumn(int dataType) {
        return new Column("database", "schema", "table", "column", dataType, "alias");
    }

    public static Criterion buildCriterion(Column column, Filter filter) {
        return new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, List.of());
    }

    public static List<CommonTableExpression> buildCommonTableExpressions(List<String> names) {
        List<CommonTableExpression> commonTableExpressions = new ArrayList<>();
        for (String name : names) {
            CommonTableExpression commonTableExpression = new CommonTableExpression();
            commonTableExpression.setName(name);
            commonTableExpression.setQueryName("query_" + name);
            commonTableExpression.setSql("SELECT col1, col2 FROM table_" + name + " ");

            commonTableExpressions.add(commonTableExpression);
        }

        return commonTableExpressions;
    }

    public static Map<String, List<String>> buildRuntimeArguments(List<String> parameters) {
        return parameters.stream()
                .map(parameter -> parameter + "_arg")
                .collect(
                        Collectors.groupingBy(
                                s -> s.substring(0, s.indexOf("_arg"))
                        )
                );
    }

    // todo:  remove this duplciated method and use the common TestUtils method instead.
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

}
