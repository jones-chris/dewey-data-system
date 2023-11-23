const initialState = {
    selectedQueryTemplate: {
        queryTemplateName: '',
        version: null,
        parametersAndArguments: {},
        overrides: {
            onlyColumns: [],
            limit: 10
        }
    }

};

const dataCatalogReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE':
            return {
                ...state,
                selectedQueryTemplate: action.payload.selectedQueryTemplate
            };
        default:
            return state;
    }
};

export default dataCatalogReducer;
