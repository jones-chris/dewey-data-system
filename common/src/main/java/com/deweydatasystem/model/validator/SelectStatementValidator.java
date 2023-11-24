package com.deweydatasystem.model.validator;

import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.CriteriaTreeFlattener;
import com.deweydatasystem.model.criterion.Criterion;
import lombok.NonNull;
import com.deweydatasystem.config.QbConfig;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SelectStatementValidator extends SqlValidator<SelectStatement> {

    private boolean validateRules = true; // Default to validating the rules.

    private final ColumnValidator columnValidator;

    private final TableValidator tableValidator;

    private final CommonTableExpressionValidator commonTableExpressionValidator;

    private final CriterionValidator criterionValidator;

    private final JoinValidator joinValidator;

    private final QbConfig qbConfig;

    public SelectStatementValidator(
            QbConfig qbConfig,
            ColumnValidator columnValidator,
            TableValidator tableSqlValidator,
            CommonTableExpressionValidator commonTableExpressionSqlValidator,
            CriterionValidator criterionSqlValidator,
            JoinValidator joinValidator
    ) {
        this.columnValidator = columnValidator;
        this.tableValidator = tableSqlValidator;
        this.commonTableExpressionValidator = commonTableExpressionSqlValidator;
        this.criterionValidator = criterionSqlValidator;
        this.joinValidator = joinValidator;
        this.qbConfig = qbConfig;
    }

    public void setValidateRules(boolean validateRules) {
        this.validateRules = validateRules;
    }

    @Override
    public void isValid(@NonNull SelectStatement selectStatement) {
        // Columns validation.
        List<Column> allColumns = selectStatement.getCriteria().stream()
                .map(Criterion::getColumn)
                .collect(Collectors.toList());
        allColumns.addAll(selectStatement.getColumns());

        this.columnValidator.isValid(allColumns);

        // Table validation.
        this.tableValidator.isValid(selectStatement.getTable());

        // Join validation.
        this.joinValidator.isValid(selectStatement.getJoins());

        // Criteria validation.
        selectStatement.getCriteria().forEach(this.criterionValidator::isValid);

        // Common Table Expression validation.
        selectStatement.getCommonTableExpressions().forEach(this.commonTableExpressionValidator::isValid);

        // Rules validation.
        if (this.validateRules) {
            this.assertRulesAreValid(selectStatement);
        }
    }

    protected final void assertRulesAreValid(@NonNull SelectStatement selectStatement) {
        // Maximum allowed columns
        int maxAllowedColumns = this.qbConfig.getRules().getMaximumAllowedSelectStatementNumberOfColumns();
        if (selectStatement.getColumns().size() > maxAllowedColumns) {
            throw new IllegalArgumentException("The selectStatement has more than the maximum number of columns, " + maxAllowedColumns);
        }

        // Number of criteria using indexes.
        long indexedColumnsCount = CriteriaTreeFlattener.flattenCriteria(selectStatement.getCriteria(), new HashMap<>())
                .values()
                .stream()
                .flatMap(List::stream)
                .map(Criterion::getColumn)
                .map(Column::getIsIndexed)
                .count();
        int minRequiredIndexedCriteriaColumns = this.qbConfig.getRules().getNumberOfCriteriaUsingIndexedColumns();
        if (indexedColumnsCount < minRequiredIndexedCriteriaColumns) {
            throw new IllegalArgumentException(
                    "The selectStatement has fewer than " + minRequiredIndexedCriteriaColumns + " indexed criteria columns"
            );
        }
    }


}
