import {store} from "../index";
import {UiMessage} from "../Models/UiMessage";
import {getJdbcSqlType, BIG_INT, BOOLEAN, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, SMALL_INT, TINY_INT, getCriterionFilterValues} from "../Utils/Utils";
import {flattenCriteria} from "../actions/CriteriaActions";

const NUMERIC_DATA_TYPES = [BIG_INT, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, SMALL_INT, TINY_INT];

const assertQueryTemplateIsCorrect = (queryTemplate) => {
    // A query template is selected
    if (queryTemplate.queryTemplateName === null || queryTemplate.queryTemplateName === '') {
        throw Error('Please select a query template');
    }

    // A version is selected
    if (queryTemplate.version === null || queryTemplate.version === '') {
        throw Error('Please select a version');
    }

    // Parameters and arguments are correct.
    let parameters = store.getState().query.availableSubQueries[queryTemplate.queryTemplateName].versions[queryTemplate.version].metadata.parameters;
    parameters.forEach(parameter => {
        // Check the the right number of arguments exist.
        let args = queryTemplate.parametersAndArguments[parameter.name];
        if (! args || args.length === 0) {
            throw Error(`Parameter, ${parameter.name}, does not have an argument`);
        }

        if (! parameter.allowsMultipleValues) {
            if (args && args.length > 1) {
                throw Error(`Parameter, ${parameter.name}, does not allow multiple arguments`);
            }
        }

        // Check that the data type is correct.
        if (args && args.length >= 1) {
            let jdbcDataType = getJdbcSqlType(parameter.column.dataType);
            if (NUMERIC_DATA_TYPES.includes(jdbcDataType)) {
                args.forEach(arg => {
                    let valueAsNumber = Number(arg);
                    if (isNaN(valueAsNumber)) {
                        throw Error(`The data type of parameter, ${parameter.name}, is ${jdbcDataType}, but the argument, ${arg}, is not a(n) ${jdbcDataType}`);
                    }
                })
            }
        }
    });
}

export const assertDatabaseIsSelected = () => {
    if (store.getState().query.selectedDatabase === null) {
        throw Error('Select 1 database');
    }
};

export const assertSubQueriesAreCorrect = () => {
    let subQueries = store.getState().query.subQueries;

    // Each sub query has a name
    subQueries.forEach(subQuery => {
        if (subQuery.subQueryName === null || subQuery.subQueryName === '') {
            throw Error('Each sub query must have a name');
        }

        if (subQuery.subQueryName.includes(' ')) {
            throw Error('Please remove the whitespace from all sub query names');
        }

        // // A query template is selected
        // if (subQuery.queryTemplateName === null || subQuery.queryTemplateName === '') {
        //     throw Error(`Please select a query template for sub query, ${subQuery.subQueryName}`);
        // }

        // // A version is selected
        // if (subQuery.version === null || subQuery.version === '') {
        //     throw Error(`Please select a version for sub query ${subQuery.version}`);
        // }

        // // Parameters and arguments are correct.
        // let parameters = store.getState().query.availableSubQueries[subQuery.queryTemplateName].versions[subQuery.version].metadata.parameters;
        // parameters.forEach(parameter => {
        //     // Check the the right number of arguments exist.
        //     let args = subQuery.parametersAndArguments[parameter.name];
        //     if (! parameter.allowsMultipleValues) {
        //         if (args && args.length > 1) {
        //             throw Error(`Parameter, ${parameter.name}, in sub query, ${subQuery.subQueryName}, does not allow multiple arguments`);
        //         }
        //     }

        //     // Check that the data type is correct.
        //     if (args && args.length > 1) {
        //         let jdbcDataType = getJdbcSqlType(parameter.column.dataType);
        //         if (NUMERIC_DATA_TYPES.includes(jdbcDataType)) {
        //             args.forEach(arg => {
        //                 let valueAsNumber = Number(arg);
        //                 if (isNaN(valueAsNumber)) {
        //                     throw Error(`In sub query, ${subQuery.subQueryName}, the data type of parameter, ${parameter.name}, is ${jdbcDataType}, 
        //                         but the argument, ${arg}, is not a(n) ${jdbcDataType}`);
        //                 }
        //             })
        //         }
        //     }
        // });

        assertQueryTemplateIsCorrect(subQuery);

        // Each sub query has a unique name.
        let subQueryNamesSorted = subQueries.map(subQuery => subQuery.subQueryName).sort();
        subQueryNamesSorted.forEach((subQueryName, index) => {
            // If this is the last item in the array, continue because adding + 1 to the index in the below 'else if' block will cause an index out of bounds error.
            if (index === subQueryNamesSorted.length - 1) {
                return;
            }
            else if (subQueryName === subQueryNamesSorted[index + 1]) {
                throw Error(`More than one sub query was found with the name, ${subQueryName}.  Each sub query name should be unique.`);
            }
        });
    });
};

export const assertSchemasAreSelected = () => {
    if (store.getState().query.selectedSchemas.length === 0) {
        throw Error('Select 1 or more schema')
    }
};

export const assertTablesAreSelected = () => {
    if (store.getState().query.selectedTables.length === 0) {
        throw Error('Select 1 or more tables');
    }
};

export const assertJoinsExist = () => {
    // Only run this check if there is more than 1 table selected.
    let queryState = store.getState().query;
    if (queryState.selectedTables.length <= 1) {
        return;
    }

    let numOfTablesAndJoinsDiff = queryState.selectedTables.length - queryState.joins.length;

    // There should always be 1 more table than joins.
    // At a minimum, there should be 1 less join than tables, because the user must define the join relationship between
    // the tables that have been selected AND could have self-joins in addition.
    if (numOfTablesAndJoinsDiff > 1) {
        throw Error(`You have ${queryState.selectedTables.length} tables selected, but ${queryState.joins.length} joins.
        There should be at least ${queryState.selectedTables.length - 1} join(s).`)
    }

    let allJoinTableNames = new Set();
    let targetJoinTableNames = [];
    queryState.joins.forEach(join => {
        // Check that each join has at least 1 column pair to join on.
        if (join.parentJoinColumns.length === 0 || join.targetJoinColumns.length === 0) {
            throw Error(`Each join must have at least 1 pair of columns to join on`)
        }

        // Check that this join is not a self-join - meaning, it does not join on the same table.
        if (join.parentTable.fullyQualifiedName === join.targetTable.fullyQualifiedName) {
            throw Error(`A join cannot be performed on the same table, ${join.parentTable.fullyQualifiedName}`);
        }

        // Add parent and target table's fully qualified name to the Set for the next validation check.
        allJoinTableNames.add(join.parentTable.fullyQualifiedName);
        allJoinTableNames.add(join.targetTable.fullyQualifiedName);

        // Check that target tables do not have duplicates.
        if (targetJoinTableNames.includes(join.targetTable.fullyQualifiedName)) {
            throw Error(`${join.targetTable.fullyQualifiedName} is on the right hand side (the target table) of more than 1 join.  It should be on the right side of only 1 join.`);
        }

        targetJoinTableNames.push(join.targetTable.fullyQualifiedName);
    });

    // Check that all selected tables are present in the joins.
    queryState.selectedTables.forEach(selectedTable => {
        if (! allJoinTableNames.has(selectedTable.fullyQualifiedName)) {
            throw Error(`You selected table, ${selectedTable.fullyQualifiedName}, but it is not present in any joins`);
        }
    });
};

export const assertColumnsAreSelected = () => {
    if (store.getState().query.selectedColumns.length === 0) {
        throw Error('You must select at least 1 column');
    }
};

export const assertCriteriaOperatorsAreCorrect = () => {
    let criteria = store.getState().query.criteria;
    criteria = flattenCriteria(criteria, []);
    criteria.forEach(criterion => {
        // IN and NOT IN operator check.
        if (criterion.filter.values.length > 1) {
            if (criterion.operator !== 'in' && criterion.operator !== 'notIn') {
                throw Error('A criterion has multiple values, but does not have an IN or NOT IN operator')
            }
        }

        // LIKE or NOT LIKE operator check.
        if (criterion.operator === 'like' || criterion.operator === 'notLike') {

            // The filter should have exactly 1 value when using LIKE or NOT LIKE.
            if (criterion.filter.values.length !== 1) {
                throw Error(`A criterion uses the ${criterion.operator.toUpperCase()} operator, but does not have exactly
                1 filter value`)
            }
        }

        // If the operator is NOT isNull or isNotNull, then filter values should not be empty.
        if (criterion.operator !== 'isNull' && criterion.operator !== 'isNotNull') {
            if (criterion.filter.values.length === 0) {
                throw Error(`A criterion has an empty filter, but has a ${criterion.operator.toUpperCase()} operator`);
            }
        }
    })
};

export const assertCriteriaFiltersAreCorrect = () => {
    let subQueries = store.getState().query.subQueries;
    let availableSubQueries = store.getState().query.availableSubQueries;

    let criteria = store.getState().query.criteria;
    criteria = flattenCriteria(criteria, []);
    criteria.forEach(criterion => {
        // Check that the criterion's filter's values property does not contain an empty string.
        criterion.filter.values.forEach(value => {
            if (value === '') {
                throw Error('The criterion contains an empty/blank string')
            }
        });

        // If data type is not string, then check that the filter values can be converted to int, double, etc.
        let criterionColumnJdbcDataType = getJdbcSqlType(criterion.column.dataType);
        let numericJdbcTypes = [BIG_INT, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, SMALL_INT, TINY_INT];
        let criterionFilterValuesExcludingParamsAndSubQueries = getCriterionFilterValues(criterion);
        if (numericJdbcTypes.includes(criterionColumnJdbcDataType)) {
            criterionFilterValuesExcludingParamsAndSubQueries.forEach(value => {
                let valueAsNumber = Number(value);
                if (isNaN(valueAsNumber)) {
                    throw Error(`A criterion's column's data type is ${criterionColumnJdbcDataType}, but the filter value, ${value}, is not a(n) ${criterionColumnJdbcDataType}`);
                }
            })
        }

        // Other non-string data type checks.
        if (criterionColumnJdbcDataType === BOOLEAN) {
            criterion.filter.values.forEach(value => {
                let lowerCaseValue = value.toString().toLowerCase();
                if (lowerCaseValue !== 'true' && lowerCaseValue !== 'false') {
                    throw Error(`A criterion's column's data type is ${criterionColumnJdbcDataType}, but the filter value, ${value}, is not a(n) ${criterionColumnJdbcDataType}`);
                }
            })
        }

        // todo:  Add a check for dates and timestamps?

        // Sub query checks.
        criterion.filter.values.forEach(value => {
            if (value.startsWith('$')) {
                let subQueryName = value.substr(1);

                // Check that each sub query can be found.
                let matchingSubQuery = subQueries.find(subQuery => subQuery.subQueryName === subQueryName);
                if (! matchingSubQuery) {
                    throw Error(`The sub query, ${subQueryName}, does not exist`);
                }

                // Check that the sub query only returns one column.
                let availableSubQueryMetadata = store.getState().query.availableSubQueries[matchingSubQuery.queryTemplateName].versions[matchingSubQuery.version].metadata;
                if (availableSubQueryMetadata.numberOfColumnsReturned > 1) {
                    if (matchingSubQuery.overrides.onlyColumns && matchingSubQuery.overrides.onlyColumns.length !== 1) {
                        throw Error(`The sub query, ${subQueryName}, should only return 1 column`);
                    }
                }

                // If the sub query could return more than 1 row, check that the operator is IN or NOT IN. 
                if (availableSubQueryMetadata.maxNumberOfRowsReturned > 1) {
                    if (matchingSubQuery.overrides.limit !== 1) {
                        if (criterion.operator.toLowerCase() !== 'in' && criterion.operator.toLowerCase() !== 'notin') {
                            throw Error(`Sub query, ${subQueryName}, could return more than 1 row but the criterion operator is not IN or NOT IN`);
                        }
                    }
                }

                // If the criterion's column's data type is numeric, check that the sub query data type is also numeric.
                let metadataColumn;
                if (matchingSubQuery.overrides.onlyColumns) {
                    metadataColumn = matchingSubQuery.overrides.onlyColumns[0]; // We already checked that there is only 1 column.
                } else {
                    metadataColumn = availableSubQueryMetadata.columns[0]; // We already checked that there is only 1 column.
                }
                let metadaColumnJdbcType = getJdbcSqlType(metadataColumn.dataType);
                if (NUMERIC_DATA_TYPES.includes(criterionColumnJdbcDataType) && ! NUMERIC_DATA_TYPES.includes(metadaColumnJdbcType)) {
                    throw Error(`The column, ${criterion.column.fullyQualifiedName}, is a(n) ${criterionColumnJdbcDataType}, but the sub query column, 
                        ${metadataColumn.fullyQualifiedName}, is a(n) ${metadaColumnJdbcType}`)
                }
            }
        });
    })
}

export const assertRulesAreCorrect = () => {
    const queryStore = store.getState().query;
    const rules = store.getState().config.rules;

    // Validate number of indexed criteria columns.
    if (rules.numberOfCriteriaUsingIndexedColumns > 0) {
        let criteria = queryStore.criteria;
        const numberOfIndexedCriteriaColumns = flattenCriteria(criteria, [])
            .filter(criterion => criterion.column.isIndexed)
            .length;

        if (numberOfIndexedCriteriaColumns < rules.numberOfCriteriaUsingIndexedColumns) {
            throw Error(`You are required to have ${rules.numberOfCriteriaUsingIndexedColumns} indexed column(s) in your criteria, but you 
                only have ${numberOfIndexedCriteriaColumns} indexed column(s)`);
        }
    }

    // Validate the number of columns is not greater thant the maximum allowed number of columns.
    if (queryStore.selectedColumns.length > rules.maximumAllowedSelectStatementNumberOfColumns) {
        throw Error(`You are required to have no more than ${rules.maximumAllowedSelectStatementNumberOfColumns} columns, but you have 
            selected ${queryStore.selectedColumns.length} columns`);
    }
};

export const assertRawSqlIsCorrect = () => {
    const queryStore = store.getState().query;

    // Check that a database is selected.
    assertDatabaseIsSelected();

    // Check that raw SQL exists.
    if (queryStore.rawSql === null || queryStore.rawSql === undefined || queryStore.rawSql === '') {
        throw Error('The SQL statement is empty.  Please write a SQL statement before running it.');
    }
};

export const assertDataCatalogIsCorrect = () => {
    assertDatabaseIsSelected();
    assertQueryTemplateIsCorrect(store.getState().dataCatalog.selectedQueryTemplate);
};

export const assertAllValidations = () => {
    // If Raw SQL is enabled, then perform validation related to the Raw SQL.
    if (! store.getState().menuBar.rawSql.isHidden) {
        try {
            assertRawSqlIsCorrect();
        } catch(e) {
            return new UiMessage('schemasAndTables', e.message); // todo:  does Raw SQL need it's own name rather than 'schemasAndTables'?
        }
    } 
    else if (! store.getState().menuBar.dataCatalog.isHidden) {
        try {
            assertDataCatalogIsCorrect();
        } catch(e) {
            return new UiMessage('schemasAndTables', e.message); // todo:  does Raw SQL need it's own name rather than 'schemasAndTables'?
        }
    } 
    else {
        // Otherwise, if the Query Builder is enabled, then perform validation related to the Query Builder.
        try{
            assertDatabaseIsSelected();
            assertSchemasAreSelected();
            assertTablesAreSelected();
        } catch (e) {
            return new UiMessage('schemasAndTables', e.message);
        }

        try {
            assertSubQueriesAreCorrect();
        } catch (e) {
            return new UiMessage('subQueries', e.message);
        }

        try {
            assertJoinsExist();
        } catch (e) {
            return new UiMessage('joins', e.message);
        }

        try {
            assertColumnsAreSelected();
        } catch (e) {
            return new UiMessage('columns', e.message);
        }

        try {
            assertCriteriaOperatorsAreCorrect();
        } catch (e) {
            return new UiMessage('criteria', e.message);
        }

        try {
            assertCriteriaFiltersAreCorrect();
        } catch (e) {
            return new UiMessage('criteria', e.message);
        }
        try {
            assertRulesAreCorrect();
        } catch (e) {
            return new UiMessage('rules', e.message);
        }
    }

    return null;
};
