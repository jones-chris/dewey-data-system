import * as Constants from '../Config/Constants';
import * as Utils from "../Utils/Utils";

const initialState = {
    rawSql: null,
    rawSqlParameters: [],
    metadata: {
        name: '',
        description: '',
        version: 0,
        author: '',
        isDiscoverable: false
    },
    availableDatabases: [],
    selectedDatabase: null,
    availableSchemas: [],
    selectedSchemas: [],
    availableTables: [],
    selectedTables: [],
    availableColumns: [],
    selectedColumns: [],
    criteria: [],
    joins: [],
    distinct: false,
    suppressNulls: false,
    limit: 10,
    offset: 0,
    ascending: false,
    availableSubQueries: {},
    subQueries: []
};

const queryReducer = (state = initialState, action) => {
    let newState = {...state};

    switch (action.type) {
        case 'UPDATE_AVAILABLE_DATABASES':
            newState.availableDatabases = action.payload.availableDatabases;
            return newState;
        case 'CHANGE_SELECTED_DATABASE':
            newState.availableDatabases = state.availableDatabases;
            newState.selectedDatabase = action.payload.selectedDatabase;
            return newState;
        case 'UPDATE_AVAILABLE_SCHEMAS':
            newState.availableSchemas = action.payload.availableSchemas;
            return newState;
        case 'SELECT_SCHEMA':
            newState.selectedSchemas = action.payload.selectedSchemas;
            newState.availableTables = action.payload.tables;
            return newState;
        case 'SELECT_TABLE':
            newState.selectedTables = action.payload.selectedTables;
            newState.availableColumns = action.payload.availableColumns;
            return newState;
        case 'ADD_SELECTED_COLUMN':
            newState.selectedColumns = action.payload.selectedColumns;
            return newState;
        case 'REMOVE_SELECTED_COLUMN':
            newState.selectedColumns = action.payload.selectedColumns;
            return newState;
        case 'UPDATE_DISTINCT':
            newState.distinct = ! state.distinct;
            return newState;
        case 'UPDATE_SUPPRESS_NULLS':
            newState.suppressNulls = ! state.suppressNulls;
            return newState;
        case 'UPDATE_LIMIT':
            newState.limit = action.payload.newLimit;
            return newState;
        case 'UPDATE_OFFSET':
            newState.offset = action.payload.newOffset;
            return newState;
        case 'ADD_CRITERIA':
            newState.criteria = action.payload.newCriteria;
            return newState;
        case 'UPDATE_CRITERIA':
            newState.criteria = action.payload.newCriteria;
            return newState;
        case 'UPDATE_COLUMN_VALUES_MODAL_TARGET':
            newState.criteria = action.payload.newCriteria;
            return newState;
        case 'UPDATE_SUBQUERY_PARAMETER_COLUMN_VALUES':
            newState.subQueries = action.payload.newSubQueries;
            return newState;
        case Constants.IMPORT_QUERY_TEMPLATE:
            let queryTemplate = action.payload.queryTemplate;
            queryTemplate.rawSql = newState.rawSql;
            queryTemplate.availableDatabases = newState.availableDatabases;
            queryTemplate.availableSubQueries = newState.availableSubQueries;

            newState = action.payload.queryTemplate;

            return newState;
        case 'UPDATE_QUERY_METADATA':
            let attributeNameToUpdate = action.payload.attribute;
            let value = action.payload.value;

            newState.metadata[attributeNameToUpdate] = value;
            return newState;
        case 'UPDATE_AVAIALABLE_SUBQUERIES':
            newState.availableSubQueries = action.payload.availableSubQueries;
            return newState;
        case 'UPDATE_SUBQUERIES':
            newState.subQueries = action.payload.subQueries;
            return newState;
        case Constants.UPDATE_RAW_SQL:
            newState.rawSql = action.payload.rawSql;
            newState.rawSqlParameters = action.payload.parameters;
            return newState;
        case 'ADD_JOIN':
            newState.joins = action.payload.joins;
            return newState;
        case 'DELETE_JOIN':
            let joinId = parseInt(action.payload.joinId);

            // Create new array of joins that excludes the id being deleted.
            let newJoins = newState.joins.filter(join => join.metadata.id !== joinId);

            // Renumber join ids.
            newJoins.forEach((join, index) => join.metadata.id = index);

            return {
                ...newState,
                joins: newJoins
            };
        case 'CHANGE_JOIN_TYPE':
            newState.joins = action.payload.joins;
            return newState;
        case 'CHANGE_TABLE':
            newState.joins = action.payload.joins;
            return newState;
        case 'CHANGE_COLUMN':
            newState.joins = action.payload.joins
            return newState;
        case 'ADD_JOIN_COLUMN':
            newState.joins = action.payload.joins;
            return newState;
        case 'DELETE_JOIN_COLUMN':
            newState.joins = action.payload.joins;
            return newState;
        default:
            return state;
    }
};

export default queryReducer;
