import React from "react";
import './ColumnValues.css';
import {store} from "../../index";
import {connect} from "react-redux";
import * as Utils from "../../Utils/Utils";
import _ from 'lodash';
import {assertAllValidations} from "../../Validators/Validators";
import {flattenCriteria} from "../../actions/CriteriaActions";
import * as Constants from '../../Config/Constants';
import Modal from "react-bootstrap/Modal";
import Button from "react-bootstrap/Button";
import Form from 'react-bootstrap/Form';
import Alert from 'react-bootstrap/Alert';

class ColumnValues extends React.Component {

    constructor(props) {
        super(props);
    }

    onNextPage = () => {
            let columnValueModalState = store.getState().modal.columnValueModal;
            let column = columnValueModalState.target.column;
            let databaseName = column.databaseName;
            let schemaName = column.schemaName;
            let tableName = column.tableName;
            let columnName = column.columnName;

            let baseApiUrl = `${store.getState().config.baseApiUrl}/data/${databaseName}/${schemaName}/${tableName}/${columnName}/column-member`;
            let queryParams = `?limit=${columnValueModalState.limit}&offset=${columnValueModalState.offset}&ascending=true`;

            // Concatenate the search text if it exists.
            if (columnValueModalState.search !== null && columnValueModalState.search.length > 0) {
                let escapedSearch = columnValueModalState.search.replaceAll('%', '%25'); // The % sign is a special character, so we need to replace it.
                queryParams += `&search=${escapedSearch}`
            }

            let fullUrl = baseApiUrl + queryParams;

            fetch(fullUrl)
                .then(response => {
                    const json = response.json();

                    if (! response.ok) {
                        if (json.hasOwnProperty('message')) {
                            throw Error(json.message);
                        } 
                        else {
                            throw Error('An error occurred.  Please contact your administrator for more information.');
                        }
                    }

                    return json;
                }).then(json => {
                    console.log(json);

                    // Default to the current availableColumnValues in case there is no data in the response body.
                    let newColumnValues = columnValueModalState.availableColumnValues;
                    let uiMessage = '';
                    let disableNextPageButton = false;
                    let offsetDelta = columnValueModalState.limit;
                    if (json.data.length > 0) {
                        newColumnValues = json.data.flatMap(row => [row[0]]);
                    } else {
                        uiMessage = 'There are no more column values to retrieve';
                        disableNextPageButton = true;
                        offsetDelta = 0;  // If there are no more column values to retrieve, then don't increase/decrease the offset.
                    }

                    this.props.onUpdateAvailableColumnValues({
                        newColumnValues: newColumnValues,
                        uiMessage: uiMessage,
                        disablePriorPageButton: false,
                        disableNextPageButton: disableNextPageButton,
                        offsetDelta: offsetDelta
                    });
                }).catch(error => {
                    this.props.showMessageModal(error.message, Constants.MessageModalType.ERROR)
                });
        }

    render() {
        // Create available column values JSX.
        let availableColumnValuesJsx = [];
        if (this.props.columnValueModal !== null) {
            this.props.columnValueModal.availableColumnValues.forEach((availableColumnValue, index) => {
                availableColumnValuesJsx.push(
                    <option key={index} value={availableColumnValue}>{availableColumnValue}</option>
                )
            });
        }

        // Create selected column values JSX.
        let selectedColumnValuesJsx = [];
        if (this.props.columnValueModal !== null) {
            this.props.columnValueModal.selectedColumnValues.forEach((selectedColumnValue, index) => {
                selectedColumnValuesJsx.push(
                    <option key={index} value={selectedColumnValue}>{selectedColumnValue}</option>
                )
            })
        }

        let alert;
        if (this.props.columnValueModal && this.props.columnValueModal.uiMessage !== '') {
            alert = <Alert variant="danger">{this.props.columnValueModal.uiMessage}</Alert>;
        }

        return (
            <Modal className="column-members-modal" show={true} backdrop='static' size="lg" onHide={this.props.onCloseColumnValues} centered>
                
                <Modal.Header closeButton>
                  <Modal.Title>{`Search ${(this.props.columnValueModal.target) ? "\"" + this.props.columnValueModal.target.column.columnName + "\"" : ''} Column Data`}</Modal.Title>
                </Modal.Header>
                
                <Modal.Body>
                    <Form>
                        <Form.Group controlId="columnMembers.search">
                            <Form.Control 
                                as="input" 
                                placeholder="ex: Cap% will find records starting with 'Cap'" 
                                onChange={(event) => this.props.onSearchChange(event.target.value)}
                            >       
                            </Form.Control>
                        </Form.Group>

                        {alert}

                        <Form.Group controlId="columnMembers.pagination"
                                    className="column-members-modal-pagination"
                        >
                            <Button 
                                variant="outline-secondary" 
                                disabled={(this.props.columnValueModal !== null) ? this.props.columnValueModal.disablePriorPageButton : true }
                                onClick={this.props.onPriorPage}
                            >
                                Prior Page
                            </Button>
                            {' '}
                            <Button 
                                variant="outline-secondary" 
                                disabled={(this.props.columnValueModal !== null) ? this.props.columnValueModal.disableNextPageButton : false }
                                onClick={this.onNextPage}
                            >
                                Next Page
                            </Button>
                        </Form.Group>

                        {/*Member selection area*/}
                        <div className="column-members-selection">

                            <div className="column-members-modal-available-members">
                                <label htmlFor="availableValues">Available Column Members</label>
                                <select id="availableColumnValues" multiple={true} size="20">
                                    {availableColumnValuesJsx}
                                </select>
                            </div>

                            <div className="column-members-selection-button-div">
                                <div>
                                    <Button variant="outline-secondary"
                                            className="column-members-selection-button"
                                            onClick={this.props.onAddSelectColumnValues}
                                    >
                                        →
                                    </Button>
                                </div>

                                <br/>

                                <div>
                                    <Button variant="outline-secondary"
                                            className="column-members-selection-button"
                                            onClick={this.props.onRemoveSelectedColumnValues}
                                    >
                                        &#8592;
                                    </Button>
                                </div>
                            </div>

                            <div className="column-members-modal-selected-members">
                                <label htmlFor="selectedMembers">Selected Column Members</label>
                                <select id="selectedColumnValues" multiple={true} size="20">
                                    {selectedColumnValuesJsx}
                                </select>
                            </div>

                        </div>
                    </Form>
                </Modal.Body>

                <Modal.Footer className="column-members-modal-submit">
                  <Button variant="outline-secondary" onClick={this.props.onCloseColumnValues}>
                    Cancel
                  </Button>
                  <Button variant="outline-primary" onClick={this.props.onSubmitColumnValues}>
                    OK
                  </Button>
                </Modal.Footer>

          </Modal>
        );
    }

}

const mapReduxStateToProps = (reduxState) => {
    return {
        ...reduxState.query,
        ...reduxState.modal
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
        onUpdateAvailableColumnValues: (payload) => {
            dispatch({
                type: 'UPDATE_AVAILABLE_COLUMN_MEMBERS',
                payload: payload
            });
        },
        onSubmitColumnValues: () => {
            let target = store.getState().modal.columnValueModal.target;

            // Get the selected column values that will become the value of the target object ref and target attribute.
            let selectedColumnValues = store.getState().modal.columnValueModal.selectedColumnValues;

            if (target.type === 'CRITERIA') {
                // Get object ref and attribute to update.
                let targetObjectId = target.objectId;

                // Copy the query state's criteria.
                let newCriteria = [...store.getState().query.criteria];

                let flattenedCriteria = flattenCriteria(newCriteria, []);
                flattenedCriteria.forEach(criterion => {
                    if (criterion.metadata.id === targetObjectId) {
                        criterion.filter.values = selectedColumnValues;
                    }
                });

                // Dispatch action to update criteria.
                dispatch({
                    type: 'UPDATE_COLUMN_VALUES_MODAL_TARGET',
                    payload: {
                        newCriteria: newCriteria
                    }
                });
            }
            else if (target.type === 'SUBQUERY') {
                 // Get object ref and attribute to update.
                let targetObjectId = target.objectId;

                let newSubQueries = [...store.getState().query.subQueries];

                newSubQueries.forEach(subQuery => {
                    if (subQuery.id === targetObjectId) {
                        subQuery.parametersAndArguments[target.parameterName] = selectedColumnValues;
                    }
                })
                
                dispatch({
                    type: 'UPDATE_SUBQUERY_PARAMETER_COLUMN_VALUES',
                    payload: {
                        newSubQueries: newSubQueries
                    }
                });
            }
            else if (target.type === 'DATA_CATALOG') {
                let selectedQueryTemplate = {...store.getState().dataCatalog.selectedQueryTemplate};
                selectedQueryTemplate.parametersAndArguments[target.parameterName] = selectedColumnValues;

                dispatch({
                    type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
                    payload: {
                        selectedQueryTemplate: selectedQueryTemplate
                    }
                })
            }

            // Dispatch action to close Column Values modal.
            dispatch({
                type: 'CLOSE_COLUMN_VALUES_MODAL',
            });

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        onCloseColumnValues: () => {
            dispatch({
                type: 'CLOSE_COLUMN_VALUES_MODAL',
            });
        },
        onSearchChange: (search) => {
            dispatch({
                type: 'UPDATE_COLUMN_VALUES_SEARCH',
                payload: {
                    newSearch: search
                }
            })
        },
        onPriorPage: () => {
            let columnValueModalState = store.getState().modal.columnValueModal;
            let column = columnValueModalState.target.column;
            let databaseName = column.databaseName;
            let schemaName = column.schemaName;
            let tableName = column.tableName;
            let columnName = column.columnName;

            let newOffset = (columnValueModalState.offset - columnValueModalState.limit >= 0) ? columnValueModalState.offset - columnValueModalState.limit : 0;

            let baseApiUrl = `${store.getState().config.baseApiUrl}/data/${databaseName}/${schemaName}/${tableName}/${columnName}/column-member`;
            let queryParams = `?limit=${columnValueModalState.limit}&offset=${newOffset}&ascending=true`;

            // Concatenate the search text if it exists.
            if (columnValueModalState.search !== null && columnValueModalState.search.length > 0) {
                let escapedSearch = columnValueModalState.search.replaceAll('%', '%25'); // The % sign is a special character, so we need to replace it.
                queryParams += `&search=${escapedSearch}`
            }

            let fullUrl = baseApiUrl + queryParams;

            fetch(fullUrl)
                .then(response => response.json())
                .then(columnValues => {
                    console.log(columnValues);

                    // Default to the current availableColumnValues if the offset is now 0 - meaning we are at the first page.
                    let newColumnValues = columnValueModalState.availableColumnValues;
                    let uiMessage = '';

                    let disablePriorPageButton = newOffset === 0;  // Disable the prior page if offset is 0.
                    let disableNextPageButton = false;  // When getting the prior page, the Next Page button is always enabled.

                    let offsetDelta = (- columnValueModalState.limit);
                    newColumnValues = columnValues.data.flatMap(row => [row[0]]);

                    dispatch({
                        type: 'UPDATE_AVAILABLE_COLUMN_MEMBERS',
                        payload: {
                            newColumnValues: newColumnValues,
                            uiMessage: uiMessage,
                            disablePriorPageButton: disablePriorPageButton,
                            disableNextPageButton: disableNextPageButton,
                            offsetDelta: offsetDelta
                        }
                    });
                })
        },
        onAddSelectColumnValues: () => {
            let availableColumnValuesElement = document.getElementById('availableColumnValues');
            let selectedAvailableColumnValues = Utils.getSelectedOptions(availableColumnValuesElement);

            // Remove selected available column values that already exist in the `selectedColumnValues` state so that
            // duplicates are prevented.
            selectedAvailableColumnValues = selectedAvailableColumnValues.filter(columnValue => {
                return ! store.getState().modal.columnValueModal.selectedColumnValues.includes(columnValue);
            });

            dispatch({
                type: 'ADD_SELECTED_COLUMN_VALUES',
                payload: {
                    columnValuesToAdd: selectedAvailableColumnValues
                }
            })
        },
        onRemoveSelectedColumnValues: () => {
            let selectedColumnValuesElement = document.getElementById('selectedColumnValues');
            let columnValuesToRemove = Utils.getSelectedOptions(selectedColumnValuesElement);

            dispatch({
                type: 'REMOVE_SELECTED_COLUMN_VALUES',
                payload: {
                    columnValuesToRemove: columnValuesToRemove
                }
            })
        },
        showMessageModal: (messageText, messageType, imageUrl=null) => {
            dispatch({
                type: Constants.SHOW_MESSAGE_MODAL,
                payload: {
                    messageText: messageText,
                    type: messageType,
                    imageUrl: imageUrl
                }
            })
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(ColumnValues);
