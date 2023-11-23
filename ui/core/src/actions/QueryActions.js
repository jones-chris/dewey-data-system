import { store } from "../index";
import { replaceParentCriterionIds } from "../actions/CriteriaActions";
import { removeJoinMetadata } from "../actions/JoinActions";

export const runQuery = () => {
    if (! store.getState().menuBar.dataCatalog.isHidden) {
        let apiUrl = `${store.getState().config.baseApiUrl}/data/${store.getState().query.selectedDatabase.databaseName}/query/query-template`;
        let selectedQueryTemplate = store.getState().dataCatalog.selectedQueryTemplate;
        return fetch(apiUrl, {
            method: 'POST',
            body: JSON.stringify({
                queryTemplateName: selectedQueryTemplate.queryTemplateName,
                version: selectedQueryTemplate.version,
                overrides: selectedQueryTemplate.overrides,
                arguments: selectedQueryTemplate.parametersAndArguments
            }),
            headers: {
                'Content-Type': 'application/json'
            }
        }); 
    } 
    else if (! store.getState().menuBar.rawSql.isHidden) {
        // Send the Raw SQL the user created to the API.
        let apiUrl = `${store.getState().config.baseApiUrl}/data/${store.getState().query.selectedDatabase.databaseName}/query/raw`;
        let rawSql = store.getState().query.rawSql;
        return fetch(apiUrl, {
            method: 'POST',
            body: rawSql,
            headers: {
                'Content-Type': 'text/plain' // Just send the raw SQL string, so Content-Type is not 'application/json'.
            }
        });
    }
    else {
        let statement = buildSelectStatement();
        console.log(statement);
        console.log(JSON.stringify(statement));

        // Send Select Statement to API.
        let apiUrl = `${store.getState().config.baseApiUrl}/data/${store.getState().query.selectedDatabase.databaseName}/query`;
        return fetch(apiUrl, {
            method: 'POST',
            body: JSON.stringify(statement),
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }
};

export const generateSql = () => {
    // If query builder applet is displayed...
    if (store.getState().menuBar.dataCatalog.isHidden && store.getState().menuBar.rawSql.isHidden) {
        let statement = buildSelectStatement();
        console.log(statement);
        console.log(JSON.stringify(statement));

        // Send Select Statement to API.
        let apiUrl = `${store.getState().config.baseApiUrl}/data/${store.getState().query.selectedDatabase.databaseName}/query/dry-run`;
        return fetch(apiUrl, {
            method: 'POST',
            body: JSON.stringify(statement),
            headers: {
                'Content-Type': 'application/json'
            }
        }); 
    // If data catalog applet is displayed...
    } else if (! store.getState().menuBar.dataCatalog.isHidden) {
        let apiUrl = `${store.getState().config.baseApiUrl}/data/${store.getState().query.selectedDatabase.databaseName}/query/query-template/dry-run`;
        let selectedQueryTemplate = store.getState().dataCatalog.selectedQueryTemplate;
        return fetch(apiUrl, {
            method: 'POST',
            body: JSON.stringify({
                queryTemplateName: selectedQueryTemplate.queryTemplateName,
                version: selectedQueryTemplate.version,
                overrides: selectedQueryTemplate.overrides,
                arguments: selectedQueryTemplate.parametersAndArguments
            }),
            headers: {
                'Content-Type': 'application/json'
            }
        }); 
    } else {
        console.error('Unexpected applet state!')
    }
}

export const saveQuery = () => {
    let statement = buildSelectStatement();
    console.log(statement);
    console.log(JSON.stringify(statement));

    // Send query to API.
    let apiUrl = `${store.getState().config.baseApiUrl}/query-template`;
    return fetch(apiUrl, {
        method: 'POST',
        body: JSON.stringify(statement),
        headers: {
            'Content-Type': 'application/json'
        }
    });
};

export const importQuery = (queryName, queryVersion) => {
    let apiUrl = `${store.getState().config.baseApiUrl}/query-template/${queryName}?version=${queryVersion}`;
    return fetch(apiUrl);
}

const buildSelectStatement = () => {
    const currentQueryState = store.getState().query;

    let parentTable;
    if (currentQueryState.selectedTables.length === 1) {
        parentTable = currentQueryState.selectedTables[0];
    } 
    else {
        // If there is more than 1 selected table, the first join's parent table is the select statement's table.
        parentTable = currentQueryState.joins[0].parentTable;
    }

    // Build the common table expressions/sub queries.
    let commonTableExpressions = [];
    currentQueryState.subQueries.forEach(subQuery => {
        commonTableExpressions.push({
            name: subQuery.subQueryName,
            queryName: subQuery.queryTemplateName,
            parametersAndArguments: subQuery.parametersAndArguments,
            version: subQuery.version,
            overrides: subQuery.overrides
        })
    });

    // Build statement object
    let statement = {
        metadata: currentQueryState.metadata,
        database: currentQueryState.selectedDatabase,
        columns: currentQueryState.selectedColumns,
        table: parentTable,
        criteria: replaceParentCriterionIds(currentQueryState.criteria),
        joins: removeJoinMetadata(currentQueryState.joins),
        distinct: currentQueryState.distinct,
        groupBy: false,
        orderBy: false,
        limit: currentQueryState.limit,
        ascending: currentQueryState.ascending,
        offset: currentQueryState.offset,
        suppressNulls: currentQueryState.suppressNulls,
        commonTableExpressions: commonTableExpressions
    };

    return statement;
};
