import React from 'react';
import { connect } from 'react-redux';
import { store } from "../index";
import './DataCatalog.css';
import Form from "react-bootstrap/Form";
import Table from 'react-bootstrap/Table';
import { assertAllValidations } from "../Validators/Validators";
import { getJdbcSqlType } from '../Utils/Utils';
import InputGroup from 'react-bootstrap/InputGroup';


/**
* A convenience method that attempts to walk the available sub query object tree to get the parameters object.
* If a property cannot be found, then a TypeError will be thrown, which will be caught and an empty array will 
* be returned because, essentially, parameters could not be found.  
**/
const getAvailableSubQueryParameters = (subQuery) => {
	try {
		let availableSubQueries = {...store.getState().query.availableSubQueries};
		let parameters = availableSubQueries[subQuery.queryTemplateName].versions[subQuery.version].metadata.parameters;
		return (parameters) ? parameters : []; // Check that the last property in the chain did not return undefined.
	} catch (typeError) {
		console.error('Error while attempting to get available sub query parameters');
		return [];
	}
};

const getAvailableSubQueryColumns = (subQuery) => {
	try {
		let availableSubQueries = {...store.getState().query.availableSubQueries};
		let columns = availableSubQueries[subQuery.queryTemplateName].versions[subQuery.version].metadata.columns;
		return (columns) ? columns : []
	} catch (typeError) {
		console.error('Error while attempting to get available sub query columns');
		return [];
	}
};

/**
 * Resets a query template's overrides and parameters and arguments to their default values.
 * 
 * @param {*} queryTemplate 
 * @returns The query template after resetting it.
 */
const resetQueryTemplate = (queryTemplate, clearQueryTemplateName = false, clearVersion = false) =>{
	queryTemplate.overrides = {
		onlyColumns: [],
		limit: 10
	};

	queryTemplate.parametersAndArguments = {};

	if (clearVersion) {
		queryTemplate.version = null;
	}

	if (clearQueryTemplateName) {
		queryTemplate.queryTemplateName = '';
	}

	return queryTemplate;
};

// export const DataCatalog = (props) => {
// 	// getAvailableSubQueries();

// 	return (
// 		<div hidden={props.hidden === 'true'} className="data-catalog">
// 			<QueryTemplate 
// 				subQuery={props.selectedQueryTemplate}
// 			>
// 			</QueryTemplate>
// 		</div>
// 	);
// }

export const DataCatalog = (props) => {
	let queryTemplateNamesOptionsJsx = [];
	let selectedQueryTemplate = store.getState().dataCatalog.selectedQueryTemplate;
	queryTemplateNamesOptionsJsx.push(<option default=""></option>)
	Object.keys(store.getState().query.availableSubQueries).forEach(availableSubQuery => {
		queryTemplateNamesOptionsJsx.push(
			<option key={availableSubQuery}
					value={availableSubQuery}
					selected={selectedQueryTemplate.queryTemplateName === availableSubQuery}
			>
				{availableSubQuery}
			</option>
		);
	});

	let versionsOptionsJsx = [];
	versionsOptionsJsx.push(<option default=""></option>);
	let queryTemplateName = selectedQueryTemplate.queryTemplateName;
	if (queryTemplateName !== '') {
		let versions = store.getState().query.availableSubQueries[queryTemplateName].versions;
		Object.keys(versions).forEach(version => {
			versionsOptionsJsx.push(
				<option key={version}
				        value={version}
				        selected={selectedQueryTemplate.version === version}
				>
					{version}
				</option>
			);
		}); 
	}

	let parametersAndArgumentsJsx = [];
	let availableSubQueryVersionParameters = getAvailableSubQueryParameters(selectedQueryTemplate);
	if (availableSubQueryVersionParameters.length > 0) {
		availableSubQueryVersionParameters.forEach(parameter => {
			let parameterName = parameter.name;

			// Get argument, if one exists.
			let args = selectedQueryTemplate.parametersAndArguments[parameterName];

			// Create JSX.
			parametersAndArgumentsJsx.push(
				<tr>
					<td>{parameterName}</td>
					<td>{(parameter.allowsMultipleValues) ? 'true' : 'false'}</td>
					<td>{getJdbcSqlType(parameter.column.dataType)}</td>
					<td>
						<InputGroup className="mb-3">
							<Form.Control
								as="input"
								size="sm"
								value={(args && args.length > 0) ? args.join(',') : ''}
								onChange={(event) => props.onUpdateArgument(parameterName, event)}
							>
							</Form.Control>
							<InputGroup.Append>
								<InputGroup.Text
									id="basic-addon2"
									className="hover-pointer"
									onClick={() => props.onShowColumnValuesModal(null, parameter.column, 'DATA_CATALOG', parameterName)}
								>
									Values
								</InputGroup.Text>
							</InputGroup.Append>
						</InputGroup>
					</td>
				</tr>
			);
		});
	}

	let availableSubQueryColumnsJsx = [];
	let availableSubQueryVersionColumns = getAvailableSubQueryColumns(selectedQueryTemplate);
	if (availableSubQueryVersionColumns.length > 0) {
		availableSubQueryVersionColumns.forEach(column => {
			availableSubQueryColumnsJsx.push(
				<option value={column.fullyQualifiedName}>{column.fullyQualifiedName}</option>
			);
		})
	}

	return (
		<div hidden={props.hidden === 'true'} className="data-catalog">
			<Form>
				<Form.Group>
					<Table striped bordered hover>
						<tbody>
							<tr>
								<td>Query Template (Required)</td>
								<td>
									<Form.Control 
										as="select" 
										size="sm"
										onChange={(event) => props.onChangeQueryTemplateName(event)}
									>
										{queryTemplateNamesOptionsJsx}
									</Form.Control>
								</td>
							</tr>

							<tr>
								<td>Version (Required)</td>
								<td>
									<Form.Control 
										as="select" 
										size="sm"
										onChange={(event) => props.onChangeVersion(event)}
									>	
										{versionsOptionsJsx}
									</Form.Control>
								</td>
								
							</tr>

							<tr>
								<td>Only Columns (Optional)</td>
								<td>
									<Form.Control 
										as="select" 
										size="sm"
										multiple={true}
										onChange={(event) => props.onChangeColumnsOverride(event, selectedQueryTemplate.queryTemplateName, selectedQueryTemplate.version)}
									>	
										{availableSubQueryColumnsJsx}
									</Form.Control>
								</td>
							</tr>

							<tr>
								<td>Limit (Optional)</td>
								<td>
									<Form.Control 
										as="input"
										type="number" 
										size="sm"
										value={selectedQueryTemplate.overrides.limit}
										onChange={(event) => props.onChangeLimitOverride(event)}
									>	
									</Form.Control>
								</td>
							</tr>
						</tbody>
					</Table>
				</Form.Group>

				{
					parametersAndArgumentsJsx.length > 0 &&
					<Form.Group controlId="subQuery.parameters">
						<Table striped bordered hover>
							<thead>
								<tr>
									<th>Parameter</th>
									<th>Allows Multiple Values</th>
									<th>Data Type</th>
									<th>Argument</th>
								</tr>
							</thead>
							<tbody>
								{parametersAndArgumentsJsx}
							</tbody>
						</Table>
					</Form.Group>
				}
			</Form>
		</div>
	);
}

const mapReduxStateToProps = (reduxState) => {
    return {
        ...reduxState.query,
		...reduxState.dataCatalog
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
    	onShowColumnValuesModal: (objectId, column, type, parameterName) => {
            dispatch({
                type: 'SHOW_COLUMN_VALUES_MODAL',
                payload: {
                    hide: false,
                    target: {
                        column: column,
                        type: type, // 'CRITERIA | SUBQUERY' | 'DATA_CATALOG' // -> use this in a switch block in the onSubmit modal method to get the store's criteria or subqueries.
                        objectId: objectId,
                        parameterName: parameterName
                    }
                }
            });

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
    	onChangeQueryTemplateName: async (event) => {
			let selectedQueryTemplate = {...store.getState().dataCatalog.selectedQueryTemplate};

    		let newQueryTemplateName = event.target.value;
    		if (newQueryTemplateName === '') {
    			console.error('newQueryTemplateName is an empty string');

				resetQueryTemplate(selectedQueryTemplate, true, true);
				
				dispatch({
					type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
					payload: {
						selectedQueryTemplate: selectedQueryTemplate
					}
				});

				dispatch({
					type: 'UPDATE_UI_MESSAGES',
					payload: {
						uiMessages: assertAllValidations()
					}
				});

    			return;
    		}

    		// If the sub query has no versions, then get them from the API.
    		let availableSubQueries = {...store.getState().query.availableSubQueries};
    		let availableSubQuery = availableSubQueries[newQueryTemplateName];
    		if (Object.keys(availableSubQuery.versions).length === 0) {
    			let apiUrl = `${store.getState().config.baseApiUrl}/query-template/${newQueryTemplateName}/versions`;
	            await fetch(apiUrl)
	                .then(response => response.json())
	                .then(versions => {
	                    console.log(versions);

	                    versions.forEach(version => {
	                    	availableSubQuery.versions[version] = {
	                    		'metadata': {}
	                    	}
	                    });
	                });
    		}

			selectedQueryTemplate.queryTemplateName = newQueryTemplateName;
			resetQueryTemplate(selectedQueryTemplate, false, true);
			dispatch({
				type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
				payload: {
					selectedQueryTemplate: selectedQueryTemplate
				}
			});

    		dispatch({
    			type: 'UPDATE_AVAIALABLE_SUBQUERIES',
    			payload: {
    				availableSubQueries: availableSubQueries
    			}
    		});

    		dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
    	},
    	onChangeVersion: async (event) => {
			let selectedQueryTemplate = {...store.getState().dataCatalog.selectedQueryTemplate};

    		let newVersion = parseInt(event.target.value);
    		if (isNaN(newVersion)) {
    			console.error('newVersion is not a number');

				resetQueryTemplate(selectedQueryTemplate, false, true);

				dispatch({
					type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
					payload: {
						selectedQueryTemplate: selectedQueryTemplate
					}
				});

				dispatch({
					type: 'UPDATE_UI_MESSAGES',
					payload: {
						uiMessages: assertAllValidations()
					}
				});

    			return;
    		}

    		// Update the selected query template's version and reset the overrides and parameters and arguments.
    		selectedQueryTemplate.version = newVersion;
			resetQueryTemplate(selectedQueryTemplate);

    		// Update available sub query meta data if it has not been cached on the client.
    		let availableSubQueries = {...store.getState().query.availableSubQueries};
    		let availableSubQuery = availableSubQueries[selectedQueryTemplate.queryTemplateName];
    		let availableSubQueryVersion = availableSubQuery.versions[newVersion];
    		if (Object.keys(availableSubQueryVersion.metadata).length === 0) {
    			let apiUrl = `${store.getState().config.baseApiUrl}/query-template/${selectedQueryTemplate.queryTemplateName}/metadata?version=${newVersion}`;
	            await fetch(apiUrl)
	                .then(response => response.json())
	                .then(metadata => {
	                    console.log(metadata);

	                    availableSubQueryVersion.metadata = metadata;
	                });
    		}

    		dispatch({
				type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
				payload: {
					selectedQueryTemplate: selectedQueryTemplate
				}
			});

    		dispatch({
    			type: 'UPDATE_AVAIALABLE_SUBQUERIES',
    			payload: {
    				availableSubQueries: availableSubQueries
    			}
    		});

    		dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
    	},
    	onUpdateArgument: (parameterName, event) => {
			let args = event.target.value.split(',');
			args = args.filter(arg => arg !== '');

    		let selectedQueryTemplate = {...store.getState().dataCatalog.selectedQueryTemplate};
    		selectedQueryTemplate.parametersAndArguments[parameterName] = args;

			dispatch({
				type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
				payload: {
					selectedQueryTemplate: selectedQueryTemplate
				}
			});

    		dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
    	},
    	onChangeColumnsOverride: (event, queryTemplateName, version) => {
    		// Get the selected option values, which are the fully qualified column names.
    		let selectedColumnsFullyQualifiedNames = Array.from(event.target.selectedOptions)
    		    .map(option => option.value);

    		// Use the fully qualified column names to get the selected column objects.
    		let availableSubQueries = {...store.getState().query.availableSubQueries};
    		let selectedColumnsObjects = availableSubQueries[queryTemplateName].versions[version].metadata.columns
    		    .filter(selectedColumn => {
    		   		return selectedColumnsFullyQualifiedNames.includes(selectedColumn.fullyQualifiedName);
    		    });

            let selectedQueryTemplate = {...store.getState().dataCatalog.selectedQueryTemplate};
            selectedQueryTemplate.overrides.onlyColumns = selectedColumnsObjects;

			dispatch({
				type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
				payload: {
					selectedQueryTemplate: selectedQueryTemplate
				}
			});

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
    	},
    	onChangeLimitOverride: (event) => {
    		let selectedQueryTemplate = {...store.getState().dataCatalog.selectedQueryTemplate};
    		selectedQueryTemplate.overrides.limit = parseInt(event.target.value);

			dispatch({
				type: 'UPDATE_DATA_CATALOG_SELECTED_QUERY_TEMPLATE',
				payload: {
					selectedQueryTemplate: selectedQueryTemplate
				}
			});

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
    	}
	}
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(DataCatalog);