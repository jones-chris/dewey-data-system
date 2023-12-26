package com.deweydatasystem.model.validator;

import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Conjunction;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import com.deweydatasystem.model.criterion.Operator;
import com.deweydatasystem.model.cte.CommonTableExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SelectStatementValidatorTest {

    @Mock
    private ColumnValidator columnValidator;

    @Mock
    private TableValidator tableValidator;

    @Mock
    private CommonTableExpressionValidator commonTableExpressionValidator;

    @Mock
    private CriterionValidator criterionValidator;

    @Mock
    private JoinValidator joinValidator;

    @Mock
    private QbConfig qbConfig;

    @InjectMocks
    private SelectStatementValidator selectStatementValidator;

    @Before
    public void beforeEach() {
        QbConfig.Rules rules = new QbConfig.Rules();
        rules.setMaximumAllowedSelectStatementNumberOfColumns(20);
        rules.setNumberOfCriteriaUsingIndexedColumns(2);

        when(this.qbConfig.getRules())
                .thenReturn(rules);

        doNothing()
                .when(this.columnValidator).isValid(anyList());

        doNothing()
                .when(this.tableValidator).isValid(any());

        doNothing()
                .when(this.commonTableExpressionValidator).isValid(any());

        doNothing()
                .when(this.criterionValidator).isValid(any());

        doNothing()
                .when(this.joinValidator).isValid(anyList());
    }

    @Test(expected = NullPointerException.class)
    public void isValid_nullSelectStatementThrowsException() {
        this.selectStatementValidator.isValid(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertRulesAreValid_moreColumnsThanMaximumAllowedColumns() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().addAll(
                Collections.nCopies(
                        100,
                        new Column()
                )
        );

        this.selectStatementValidator.assertRulesAreValid(selectStatement);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertRulesAreValid_fewerCriteriaIndexedColumnsThanMinimumRequired() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(
                                "database",
                                "schema",
                                "table",
                                "column",
                                Types.VARCHAR,
                                "alias",
                                false
                        ),
                        Operator.equalTo,
                        new Filter(List.of("bob")),
                        List.of()
                )
        );

        this.selectStatementValidator.assertRulesAreValid(selectStatement);
    }

    @Test
    public void isValid_callsAllDependencyValidators() {
        SelectStatement selectStatement = new SelectStatement();
        // Add 2 indexed criterion columns.
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(
                                "database",
                                "schema",
                                "table",
                                "column",
                                Types.VARCHAR,
                                "alias",
                                true
                        ),
                        Operator.equalTo,
                        new Filter(List.of("bob")),
                        List.of()
                )
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(
                                "database",
                                "schema",
                                "table",
                                "column",
                                Types.VARCHAR,
                                "alias",
                                true
                        ),
                        Operator.equalTo,
                        new Filter(List.of("bob")),
                        List.of()
                )
        );
        selectStatement.getCommonTableExpressions().add(
                new CommonTableExpression()
        );

        this.selectStatementValidator.isValid(selectStatement);

        verify(this.columnValidator, times(1)).isValid(anyList());
        verify(this.tableValidator, times(1)).isValid(any());
        verify(this.commonTableExpressionValidator, times(1)).isValid(any());
        verify(this.criterionValidator, times(2)).isValid(any());
        verify(this.joinValidator, times(1)).isValid(anyList());
    }

}