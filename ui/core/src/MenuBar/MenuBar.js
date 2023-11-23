import React, { Component } from 'react';
import './MenuBar.css';
import * as Constants from '../Config/Constants';
import { connect } from 'react-redux'
import { store } from "../index";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import NavDropdown from "react-bootstrap/NavDropdown";
import Button from "react-bootstrap/Button";
import { importQuery, generateSql } from "../actions/QueryActions";
import { assertAllValidations } from "../Validators/Validators";
import { getAvailableSubQueries } from '../actions/SubQueryActions';
import { getAvailableSchemas, getAvailableTablesAndViews, getAvailableColumns } from "../actions/DatabaseMetadataActions";
import { unflattenCriteria, setCriterionMetadata } from "../actions/CriteriaActions";
import Dropdown from 'react-bootstrap/Dropdown';


class MenuBar extends Component {

    constructor(props) {
        super(props);

        // Get target databases so they can be added to the drop down nav bar.
        let apiUrl = `${store.getState().config.baseApiUrl}/metadata/database`;
        fetch(apiUrl)
            .then(response => {
                if (! response.ok) {
                    throw Error('Could not retrieve databases')
                }

                return response.json()
            }).then(databases => {
                console.log(`Data Sources: ${databases}`);
                this.props.updateAvailableDatabases(databases);
            })
            .catch(reason => {
                console.log(reason);
            });
    }

    onImportQueryHandler = () => {
        let queryTemplateName = store.getState().dataCatalog.selectedQueryTemplate.queryTemplateName;
        let queryTemplateVersion = store.getState().dataCatalog.selectedQueryTemplate.version;
        importQuery(queryTemplateName, queryTemplateVersion)
            .then(async response => {
                if (response.ok) {
                    const data = await response.json();
                    return data;
                }

                // If the response is not 200.
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.indexOf("application/json") !== -1) {
                    const data = await response.json();
                    if (data.hasOwnProperty('message')) {
                        throw Error(data.message);
                    }
                }

                throw Error('An error occurred when importing the query.  Please contact your administrator for more information.');
            }).then(async json => {
                console.log(json);

                // Get available schemas.
                const availableSchemasResponse = await getAvailableSchemas(json.database.databaseName);
                const availableSchemas = await availableSchemasResponse.json();

                // Get available tables.
                const selectedSchemas = [...new Set(json.columns.map(column => column.schemaName))];
                const joinedSchemaString = selectedSchemas.join('&');
                const availableTablesResponse = await getAvailableTablesAndViews(json.database.databaseName, joinedSchemaString);
                const availableTables = await availableTablesResponse.json();

                // Get available columns of all tables:  parent table and join tables.
                const selectedTables = [json.table];
                json.joins.forEach(join => {
                    selectedTables.push(join.parentTable);
                    selectedTables.push(join.targetTable)
                });
                const availableColumnsResponse = await getAvailableColumns(selectedTables);
                const availableColumns = await availableColumnsResponse.json();

                // Update redux state.
                await this.props.onImportQuery(json, availableSchemas, availableTables, availableColumns);

                this.props.showMessageModal('Imported query successfully', Constants.MessageModalType.INFO)
            }).catch(error => {
                console.log(error)
                this.props.showMessageModal(error.message, Constants.MessageModalType.ERROR)
            })
    }

    onGenerateSqlHandler = () => {
        // Check if query is valid first.
        const currentMenuBarState = store.getState().menuBar;
        for (let section in currentMenuBarState) {
            if (! currentMenuBarState[section].isValid) {
                this.props.showMessageModal(
                    'Resolve all messages before running the query.', 
                    Constants.MessageModalType.INFO
                );
                return;
            }
        }

        generateSql()
            .then(async response => {
                if (response.ok) {
                    const sql = await response.text();
                    return sql;
                }

                // If the response is not 200.
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.indexOf("application/json") !== -1) {
                    const data = await response.json();
                    if (data.hasOwnProperty('message')) {
                        throw Error(data.message);
                    }
                }

                throw Error('An error occurred while generating SQL.  Please contact your administrator for more information.');
            }).then(async sql => {
                console.log(sql);
                this.props.showMessageModal(sql, Constants.MessageModalType.INFO);
            }).catch(error => {
                console.log(error);
                this.props.showMessageModal(error.message, Constants.MessageModalType.ERROR);
            })
    };

    onRunQueryHandler = () => {
        // Check if query is valid first.
        const currentMenuBarState = store.getState().menuBar;
        for (let section in currentMenuBarState) {
            if (! currentMenuBarState[section].isValid) {
                this.props.showMessageModal(
                    'Resolve all messages before running the query.', 
                    Constants.MessageModalType.INFO
                );
                return;
            }
        }

        this.props.showQueryProgressModal();
    };

    getSelectedApplication = () => {
        let menuBarState = store.getState().menuBar;
        if (! menuBarState.rawSql.isHidden) {
            return Constants.RAW_SQL;
        }
        else if (! menuBarState.dataCatalog.isHidden) {
            return Constants.DATA_CATALOG
        }
        
        return Constants.QUERY_BUILDER;
    }

    render() {
        // Create database NavDropdown.Item JSX.
        let availableDatabases = [];
        store.getState().query.availableDatabases.forEach(database => {
            availableDatabases.push(
                <NavDropdown.Item key={database.databaseName}
                                  onClick={() => this.props.onChangeSelectedDatabase(database)}
                >
                    {database.databaseName} ({database.databaseType})
                </NavDropdown.Item>
            );
        });

        // Determine whether to create query builder menu items.
        const hideQueryBuilderMenuItems = this.getSelectedApplication() !== Constants.QUERY_BUILDER;
        let queryBuilderNavLinks = [];
        if (! hideQueryBuilderMenuItems) {
            queryBuilderNavLinks.push(
                <Nav.Link className={this.props.menuBar.schemasAndTables.isVisible ? "nav-item active" : "nav-item"}
                          onClick={this.props.toggleSubQueriesVisibility}
                >
                    Sub Queries
                </Nav.Link>
            );

            queryBuilderNavLinks.push(
                <Nav.Link className={this.props.menuBar.schemasAndTables.isVisible ? "nav-item active" : "nav-item"}
                          onClick={this.props.toggleSchemasAndTablesVisibility}
                >
                    Schemas & Tables
                </Nav.Link>
            );

            queryBuilderNavLinks.push(
                <Nav.Link className={this.props.menuBar.joins.isVisible ? "nav-item active" : "nav-item"}
                          onClick={this.props.toggleJoinsVisibility}
                >
                    Joins
                </Nav.Link>
            );

            queryBuilderNavLinks.push(
                <Nav.Link className={this.props.menuBar.columns.isVisible ? "nav-item active" : "nav-item"}
                          onClick={this.props.toggleColumnsVisibility}
                >
                    Columns
                </Nav.Link>
            );

            queryBuilderNavLinks.push(
                <Nav.Link className={this.props.menuBar.criteria.isVisible ? "nav-item active" : "nav-item"}
                          onClick={this.props.toggleCriteriaVisibility}
                >
                    Criteria
                </Nav.Link>
            );

            queryBuilderNavLinks.push(
                <Nav.Link className={this.props.menuBar.otherOptions.isVisible ? "nav-item active" : "nav-item"}
                          onClick={this.props.toggleOtherOptionsVisibility}
                >
                    Other Options
                </Nav.Link>
            );
        }

        return (
            <Navbar bg="light" expand="lg">
                <NavDropdown title={this.getSelectedApplication()} id="basic-nav-dropdown">
                    <NavDropdown.Item key="queryBuilder"
                                      onClick={() => this.props.onSelectApplication(Constants.QUERY_BUILDER)}
                    >
                        Query Builder
                    </NavDropdown.Item>
                    <NavDropdown.Item key="rawSql"
                                      onClick={() => this.props.onSelectApplication(Constants.RAW_SQL)}
                    >
                        Raw SQL
                    </NavDropdown.Item>
                    <NavDropdown.Item key="dataCatalog"
                                      onClick={() => this.props.onSelectApplication(Constants.DATA_CATALOG)}
                    >
                        Data Catalog
                    </NavDropdown.Item>
                </NavDropdown>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <NavDropdown title="Databases" id="basic-nav-dropdown">
                            {availableDatabases}
                        </NavDropdown>
                        {queryBuilderNavLinks}
                    </Nav>

                    {
                        <Dropdown>
                            <Dropdown.Toggle variant="outline-primary" className="mr-1">
                                Run Query
                            </Dropdown.Toggle>

                            <Dropdown.Menu>
                                {store.getState().menuBar.rawSql.isHidden && <Dropdown.Item onClick={this.onGenerateSqlHandler}>Generate SQL</Dropdown.Item>}
                                <Dropdown.Item onClick={this.onRunQueryHandler}>Execute Query</Dropdown.Item>
                            </Dropdown.Menu>
                        </Dropdown>
                    }

                    {
                        store.getState().menuBar.dataCatalog.isHidden && store.getState().menuBar.rawSql.isHidden &&
                        <Button variant="outline-secondary"
                                onClick={this.props.onSaveQueryHandler}
                        >
                            Save Query
                        </Button>
                    }

                    {
                        ! store.getState().menuBar.dataCatalog.isHidden &&
                        <Button variant="outline-secondary"
                                onClick={this.onImportQueryHandler}
                        >
                            Import Query
                        </Button>
                    }
                </Navbar.Collapse>
            </Navbar>
        );
    }
}

const mapReduxStateToProps = (reduxState) => {
    return reduxState
};

const mapDispatchToProps = (dispatch) => {
    return {
        onSaveQueryHandler: () => {
            dispatch({
                type: 'SHOW_SAVE_QUERY_MODAL',
                payload: {
                    hide: false
                }
            });
        },
        toggleSubQueriesVisibility: () => dispatch({ type: Constants.SUB_QUERIES }),
        toggleJoinsVisibility: () => dispatch({ type: Constants.JOINS }),
        toggleSchemasAndTablesVisibility: () => dispatch({ type: Constants.SCHEMAS_AND_TABLES }),
        toggleQueryTemplatesVisibility: () => dispatch({ type: Constants.QUERY_TEMPLATES }),
        toggleColumnsVisibility: () => dispatch({ type: Constants.COLUMNS }),
        toggleCriteriaVisibility: () => dispatch({ type: Constants.CRITERIA }),
        toggleOtherOptionsVisibility: () => dispatch({ type: Constants.OTHER_OPTIONS }),
        updateAvailableDatabases: (availableDatabases) => {
            dispatch({
                type: 'UPDATE_AVAILABLE_DATABASES',
                payload: {
                    availableDatabases: availableDatabases
                }
            })
        },
        onChangeSelectedDatabase: async (selectedDatabase) => {
            // Update selected database.
            dispatch({
                type: 'CHANGE_SELECTED_DATABASE',
                payload: {
                    selectedDatabase: selectedDatabase
                }
            });

            // Get available schemas for the database - even if the Query Builder is hidden because if the user enables the query builder then the 
            // schemas will be ready to choose from.
            getAvailableSchemas(selectedDatabase.databaseName)
                .then(response => response.json())
                .then(schemas => {
                    console.log(schemas);

                    // Update the schemas in the redux state.
                    dispatch({
                        type: 'UPDATE_AVAILABLE_SCHEMAS',
                        payload: {
                            availableSchemas: schemas
                        }
                    })
                });

            await getAvailableSubQueries();

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            })
        },
        showQueryProgressModal: () => {
            dispatch({
                type: Constants.SHOW_QUERY_PROGRESS_MODAL
            });
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
        },
        onSelectApplication: (applicationToShow=Constants.QUERY_BUILDER) => {
            let eventType = Constants.SHOW_QUERY_BUILDER_APPLICATION;

            switch (applicationToShow) {
                case Constants.QUERY_BUILDER:
                    eventType = Constants.SHOW_QUERY_BUILDER_APPLICATION
                    break;
                case Constants.RAW_SQL:
                    eventType = Constants.SHOW_RAW_SQL_APPLICATION;
                    break;
                case Constants.DATA_CATALOG:
                    eventType = Constants.SHOW_DATA_CATALOG_APPLICATION;
                    break;
                default:
                    throw Error(`applicationToShow of ${applicationToShow} is not recognized`);
            }

            dispatch({
                type: eventType
            });
            

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            })
        },
        onImportQuery: async (queryTemplate, availableSchemas, availableTables, availableColumns) => {
            queryTemplate.selectedDatabase = queryTemplate.database;

            queryTemplate.availableSchemas = availableSchemas;
            queryTemplate.availableTables = availableTables;
            queryTemplate.availableColumns = availableColumns;

            // Removing duplicate schema names by using a Set and then making it an array again.
            queryTemplate.selectedSchemas = [...new Set(queryTemplate.columns.map(column => column.schemaName))];
            delete queryTemplate.schemas;

            queryTemplate.selectedColumns = queryTemplate.columns;
            delete queryTemplate.columns;

            queryTemplate.selectedTables = [queryTemplate.table];
            queryTemplate.joins.forEach(join => {
                // Only add the parent table if it is not the same as the selected table to avoid duplicates.
                if (join.parentTable.fullyQualifiedName !== queryTemplate.table.fullyQualifiedName) {
                    queryTemplate.selectedTables.push(join.parentTable);
                }

                // Only add the target table if it is not the same as the selected table to avoid duplicates.
                if (join.targetTable.fullyQualifiedName !== queryTemplate.table.fullyQualifiedName) {
                    queryTemplate.selectedTables.push(join.targetTable);
                }
            });

            // Nest/unflatten criteria and set metadata.
            queryTemplate.criteria = unflattenCriteria(queryTemplate.criteria, []);
            setCriterionMetadata(queryTemplate.criteria);

            // Map common table expressions to subQueries.
            queryTemplate.subQueries = queryTemplate.commonTableExpressions.map(cte => {
                return {
                    subQueryName: cte.name,
                    queryTemplateName: cte.queryName,
                    parametersAndArguments: cte.parametersAndArguments,
                    version: cte.version,
                    overrides: cte.overrides
                }
            });

            // Create join metadata.
            queryTemplate.joins.forEach((join, index) => {
                let objs = Constants.JOIN_IMAGES;
                let joinImageUrl = Constants.JOIN_IMAGES.find(joinNameAndImage => joinNameAndImage.name === join.joinType).image;
                join.metadata = {
                   id: index,
                   joinImageUrl: joinImageUrl
               }
            });

            dispatch({
                type: Constants.IMPORT_QUERY_TEMPLATE,
                payload: {
                    queryTemplate: queryTemplate
                }
            })

            // Get available sub queries for the new selected database.
            await getAvailableSubQueries();
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(MenuBar);
