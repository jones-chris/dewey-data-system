import { store } from "../index";

export const getAvailableSchemas = (selectedDatabaseName) => {
    let apiUrl = `${store.getState().config.baseApiUrl}/metadata/${selectedDatabaseName}/schema`;
    return fetch(apiUrl);
}

export const getAvailableTablesAndViews = (selectedDatabaseName, joinedSchemaString) => {
    let apiUrl = `${store.getState().config.baseApiUrl}/metadata/${selectedDatabaseName}/${joinedSchemaString}/table-and-view`;
    return fetch(apiUrl);
}

export const getAvailableColumns = (tables) => {
    let apiUrl = `${store.getState().config.baseApiUrl}/metadata/database/schema/table/column`;
    return fetch(apiUrl,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(tables)
    })
}