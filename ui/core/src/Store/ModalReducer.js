import {store} from "../index";
import * as Constants from "../Config/Constants"; 

const initialState = {
    hideColumnMembersModal: true,
    columnValueModal: {
        target: null,
        id: 0,  // There will only be 1 Column Values modal and state at a time.
        offset: 0,
        limit: 50,
        search: null,
        availableColumnValues: [],
        selectedColumnValues: [],
        disablePriorPageButton: true,
        disableNextPageButton: false,
        uiMessage: ''
    },
    hideSaveQueryModal: true,
    messageModal: {
        isHidden: true,
        messageText: '',
        type: Constants.MessageModalType.INFO,
        imageUrl: null
    },
    queryProgressModal: {
        isHidden: true,
        queryId: null,
        status: 'Not Built',
        timestamp: null,
        message: null
    }
};

const modalReducer = (state = initialState, action) => {
    // let newState = {...initialState};
    let newState = JSON.parse(JSON.stringify(state)); // todo: change to {...initialState}.

    switch (action.type) {
        case 'SHOW_SAVE_QUERY_MODAL':
            newState.hideSaveQueryModal = action.payload.hide;
            return newState;
        case 'HIDE_SAVE_QUERY_MODAL':
            newState.hideSaveQueryModal = action.payload.hide;
            return newState;
        case 'SHOW_COLUMN_VALUES_MODAL':
            newState.columnValueModal.target = action.payload.target;
            newState.hideColumnMembersModal = action.payload.hide;

            return newState;
        case 'UPDATE_COLUMN_VALUES_SEARCH':
            newState.columnValueModal.search = action.payload.newSearch;
            newState.columnValueModal.offset = 0; // Reset pagination.
            newState.columnValueModal.disablePriorPageButton = true;
            newState.columnValueModal.disableNextPageButton = false;
            return newState;
        case 'UPDATE_AVAILABLE_COLUMN_MEMBERS':
            // Add available column members to modal state.
            newState.columnValueModal.availableColumnValues = action.payload.newColumnValues;

            // Update offset.
            let newOffset = newState.columnValueModal.offset + action.payload.offsetDelta;
            if (newOffset < 0) {
                newState.columnValueModal.offset = 0;
            } else {
                newState.columnValueModal.offset = newOffset;
            }
            
            // Update the UI message.
            newState.columnValueModal.uiMessage = action.payload.uiMessage;

            // Enable/disable Prior Page and Next Page buttons.
            newState.columnValueModal.disablePriorPageButton = action.payload.disablePriorPageButton;
            newState.columnValueModal.disableNextPageButton = action.payload.disableNextPageButton;

            return newState;
        case 'ADD_SELECTED_COLUMN_VALUES':
            action.payload.columnValuesToAdd.forEach(columnValue => {
                newState.columnValueModal.selectedColumnValues.push(columnValue);
            });

            return newState;
        case 'REMOVE_SELECTED_COLUMN_VALUES':
            newState.columnValueModal.selectedColumnValues = newState.columnValueModal.selectedColumnValues.filter(columnValue => {
                return ! action.payload.columnValuesToRemove.includes(columnValue);
            });

            return newState;
        case 'CLOSE_COLUMN_VALUES_MODAL':
            return initialState;
        case Constants.SHOW_MESSAGE_MODAL:
            newState.messageModal.isHidden = false;
            newState.messageModal.messageText = action.payload.messageText
            newState.messageModal.type = action.payload.type;
            newState.messageModal.imageUrl = action.payload.imageUrl
            return newState;
        case Constants.HIDE_MESSAGE_MODAL:
            const messageModalInitialState = {...initialState.messageModal};
            newState.messageModal = messageModalInitialState;
            return newState;
        case Constants.SHOW_QUERY_PROGRESS_MODAL:
            newState.queryProgressModal.isHidden = false;
            return newState;
        case Constants.UPDATE_QUERY_ID:
            newState.queryProgressModal.queryId = action.payload.queryId;
            return newState;
        case Constants.HIDE_QUERY_PROGRESS_MODAL:
            const queryProgressModalInitialState = {...initialState.queryProgressModal};
            newState.queryProgressModal = queryProgressModalInitialState;
            return newState;
        case Constants.UPDATE_QUERY_STATUS:
            newState.queryProgressModal.status = action.payload.status;
            newState.queryProgressModal.timestamp = action.payload.timestamp;
            return newState;
        default:
            return state;
    }

};

export default modalReducer;
