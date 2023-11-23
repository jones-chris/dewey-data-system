package net.querybuilder4j.model.criterion;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.column.Column;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CriterionTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Test
    public void hasSearchOperator_trueForLikeOperator() {
        Column column = createMockColumn("test", false);
        Filter filter = new Filter(List.of("hello%"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, null, column, Operator.like, filter, null);

        assertTrue(criterion.hasSearchOperator());
    }

    @Test
    public void hasSearchOperator_trueForNotLikeOperator() {
        Column column = createMockColumn("test", false);
        Filter filter = new Filter(List.of("hello%"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, null, column, Operator.notLike, filter, null);

        assertTrue(criterion.hasSearchOperator());
    }

    @Test
    public void hasSearchOperator_falseForEqualToOperator() {
        Column column = createMockColumn("test",false);
        Filter filter = new Filter(List.of("hello%"), List.of(), List.of());
        Criterion criterion = new Criterion(0,null, null, column, Operator.equalTo, filter, null);

        assertFalse(criterion.hasSearchOperator());
    }

//    @Test
//    public void isValid_trueForNotNullOperatorWithEmptyStringFilter() {
//        Column column = createMockColumn("test",true);
//        Filter filter = new Filter(List.of(""), List.of(), List.of());
//        Criterion criterion = new Criterion(0, null, null, column, Operator.isNotNull, filter, null);
//
//        assertTrue(criterion.isValid());
//    }
//
//    @Test
//    public void isValid_falseForEqualToOperatorWithEmptyStringFilter() {
//        Column column = createMockColumn("test",true);
//        Filter filter = new Filter(List.of(""), List.of(), List.of());
//        Criterion criterion = new Criterion(0, null, null, column, Operator.equalTo, filter, null);
//
//        assertFalse(criterion.isValid());
//    }
//
//    @Test
//    public void isValid_trueForEqualToOperatorWithNonEmptyStringFilter() {
//        Column column = createMockColumn("test",true);
//        Filter filter = new Filter(List.of("test"), List.of(), List.of());
//        Criterion criterion = new Criterion(0, null, null, column, Operator.equalTo, filter, null);
//
//        assertTrue(criterion.isValid());
//    }

    @Test
    public void toSql_nullSchema() {
        Column column = createMockColumn(null, true);
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, null);
        String expectedSql = " AND `test`.`test` = (test) ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSql_nullStringSchema() {
        Column column = createMockColumn("null", true);
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, null);
        String expectedSql = " AND `test`.`test` = (test) ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSql_nonNullSchema() {
        Column column = createMockColumn("my_schema", true);
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, null);
        String expectedSql = " AND `my_schema`.`test`.`test` = (test) ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root1
     *      - child1
     */
    @Test
    public void toSqlDeep_oneBranchWithOneNodeDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(0, null, column, null);
        Criterion childCriterion = createMockCriterion(1, rootCriterion, column, null);
        rootCriterion.getChildCriteria().add(childCriterion);
        String expectedSql = "  (`test`.`test` = ('test')   AND `test`.`test` = ('test')) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root1
     *      - child1
     *      - child2
     */
    @Test
    public void toSqlDeep_twoBranchesWithOneNodeDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(0, null, column, null);
        Criterion childCriterion1 = createMockCriterion(1, rootCriterion, column, null);
        Criterion childCriterion2 = createMockCriterion(2, rootCriterion, column, null);
        rootCriterion.setChildCriteria(Arrays.asList(childCriterion1, childCriterion2));
        String expectedSql = "  (`test`.`test` = ('test')   AND `test`.`test` = ('test')   AND `test`.`test` = ('test')) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root
     *      - child1
     *          - child1_1
     */
    @Test
    public void toSqlDeep_oneBranchWithTwoNodesDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(0, null, column, null);
        Criterion childCriterion1 = createMockCriterion(1, rootCriterion, column, null);
        Criterion childCriterion1_1 = createMockCriterion(2, childCriterion1, column, null);
        childCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        rootCriterion.setChildCriteria(Collections.singletonList(childCriterion1));
        String expectedSql = "  (`test`.`test` = ('test')   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root
     *      - child1
     *          - child1_1
     *      - child2
     *          - child2_1
     */
    @Test
    public void toSqlDeep_oneBranchWithTwoNestedBranches() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(0, null, column, null);
        Criterion childCriterion1 = createMockCriterion(1, rootCriterion, column, null);
        Criterion childCriterion1_1 = createMockCriterion(2, childCriterion1, column, null);
        childCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        Criterion childCriterion2 = createMockCriterion(3, rootCriterion, column, null);
        Criterion childCriterion2_1 = createMockCriterion(4, childCriterion2, column, null);
        childCriterion2.setChildCriteria(Collections.singletonList(childCriterion2_1));
        rootCriterion.setChildCriteria(Arrays.asList(childCriterion1, childCriterion2));
        String expectedSql = "  (`test`.`test` = ('test')   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root1
     *      - child1
     * - root2
     *      - child2
     *          - child2_1
     */
    @Test
    public void toSqlDeep_twoRoots() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion1 = createMockCriterion(0, null,  column, null);
        Criterion rootCriterion2 = createMockCriterion(1, null, column, Conjunction.And);
        Criterion childCriterion1_1 = createMockCriterion(2, rootCriterion1, column, null);
        Criterion childCriterion2_1 = createMockCriterion(3, rootCriterion2, column, null);
        Criterion childCriterion2_1_1 = createMockCriterion(4, childCriterion2_1, column, null);
        childCriterion2_1.setChildCriteria(Collections.singletonList(childCriterion2_1_1));
        rootCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        rootCriterion2.setChildCriteria(Collections.singletonList(childCriterion2_1));
        List<Criterion> rootCriteria = Arrays.asList(rootCriterion1, rootCriterion2);
        String expectedSql = "  (`test`.`test` = ('test')   AND `test`.`test` = ('test'))   AND (`test`.`test` = ('test')   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                rootCriteria,
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    @Test
    public void serializeAndDeserializeAllFieldsSuccessfully() {
        Criterion criterion = new Criterion(
                1,
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        TestUtils.buildColumn(Types.VARCHAR),
                        Operator.equalTo,
                        new Filter(
                                List.of("bob", "sam"),
                                List.of(),
                                List.of()
                        ),
                        List.of()
                ),
                Conjunction.And,
                TestUtils.buildColumn(Types.VARCHAR),
                Operator.equalTo,
                new Filter(
                        List.of("sally", "sue"),
                        List.of(),
                        List.of()
                ),
                List.of(
                        TestUtils.buildCriterion(
                                TestUtils.buildColumn(Types.VARCHAR),
                                new Filter(
                                        List.of("bob", "sam"),
                                        List.of(),
                                        List.of()
                                )
                        )
                )
        );

        byte[] bytes = SerializationUtils.serialize(criterion);
        Criterion deserializedCriterion = SerializationUtils.deserialize(bytes);

        assertEquals(criterion, deserializedCriterion);
    }

    private Column createMockColumn(String schema, boolean hasSingleQuotedColumn) {
        int dataType = (hasSingleQuotedColumn) ? Types.VARCHAR : Types.INTEGER;
        return new Column("test", schema, "test", "test", dataType, null);
    }

    private Criterion createMockCriterion(int id, Criterion parentCriterion, Column column, Conjunction parentCriterionConjunction) {
        // If no parentCriterion parameter, then return a root criterion.
        // Else, return a child criterion.
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        if (parentCriterion == null) {
            return new Criterion(id, null, parentCriterionConjunction, column, Operator.equalTo, filter, null);
        } else {
            return new Criterion(id, parentCriterion, Conjunction.And, column, Operator.equalTo, filter, null);
        }
    }

}
