const initialState = {
    parentWindow: null,
    parentWindowUrl: '',
    baseApiUrl: '',
    rules: {}
};

const configReducer = (state = initialState, action) => {

    switch (action.type) {
        case 'ADD_BASE_API_URL':
            return {
                ...state,
                parentWindow: action.payload.parentWindow,
                parentWindowUrl: action.payload.parentWindowUrl,
                baseApiUrl: action.payload.baseApiUrl
            };
        case 'UPDATE_RULES':
            return {
                ...state,
                rules: action.payload.rules
            }
        default:
            return state;
    }

};

export default configReducer;
