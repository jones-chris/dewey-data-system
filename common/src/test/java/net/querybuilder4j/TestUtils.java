package net.querybuilder4j;

import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.model.criterion.Conjunction;
import net.querybuilder4j.model.criterion.Criterion;
import net.querybuilder4j.model.criterion.Filter;
import net.querybuilder4j.model.criterion.Operator;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.model.table.Table;

import java.util.List;

public class TestUtils {

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

    public static Column buildColumn(int dataType) {
        return new Column("database", "schema", "table", "column", dataType, "alias");
    }

    public static Criterion buildCriterion(Column column, Filter filter) {
        return new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, List.of());
    }

}
