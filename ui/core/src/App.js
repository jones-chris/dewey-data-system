import React from 'react';
import './App.css';
import MenuBar from "./MenuBar/MenuBar";
import Joins from "./Joins/Joins";
import { connect } from 'react-redux'
import SchemasAndTables from "./SchemasAndTables/SchemasAndTables";
import Columns from "./Columns/Columns";
import OtherOptions from "./OtherOptions/OtherOptions";
import Criteria from "./Criteria/Criteria";
import ColumnValues from "./Modals/ColumnValues/ColumnValues";
import Warnings from "./Warnings/Warnings";
import {assertAllValidations} from "./Validators/Validators";
import SaveQuery from "./Modals/SaveQuery/SaveQuery";
import SubQueries from "./SubQueries/SubQueries";
import { store } from "./index";
import Message from "./Modals/Message/Message";
import QueryProgressBar from "./Modals/QueryProgressBar/QueryProgressBar";
import RawSql from './RawSql/RawSql';
import DataCatalog from './DataCatalog/DataCatalog';


class App extends React.Component {

    constructor(props) {
        super(props);

        // Get config state.
        this.props.onLoadIFrame();
    }

    render() {
        return (
            <div className="App">
                <MenuBar/>

                <Warnings/>

                {
                    ! this.props.menuBar.rawSql.isHidden &&
                    <RawSql/>
                }

                {
                    ! this.props.menuBar.dataCatalog.isHidden &&
                    <DataCatalog/>
                }

                {
                    ! this.props.menuBar.joins.isHidden &&
                    <Joins/>
                }

                {
                    ! this.props.menuBar.schemasAndTables.isHidden &&
                    <SchemasAndTables/>
                }

                {
                    ! this.props.menuBar.columns.isHidden &&
                    <Columns/>
                }

                {
                    ! this.props.menuBar.otherOptions.isHidden &&
                    <OtherOptions/>
                }

                {
                    ! this.props.menuBar.criteria.isHidden &&
                    <Criteria/>
                }

                {
                    ! this.props.menuBar.subQueries.isHidden &&
                    <SubQueries/>
                }

                {/*Modals*/}
                {
                    ! this.props.modal.hideColumnMembersModal &&
                    <ColumnValues
                        modalState={this.props.modal.columnValueModal}
                    />
                }

                {
                    ! this.props.modal.hideSaveQueryModal &&
                    <SaveQuery/>
                }

                {
                    ! this.props.modal.messageModal.isHidden &&
                    <Message/>
                }

                {
                    ! this.props.modal.queryProgressModal.isHidden &&
                    <QueryProgressBar/>
                }

            </div>
        );
    }
}

const mapReduxStateToProps = (reduxState) => {
    return reduxState;
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLoadIFrame: () => {
            let baseApiUrl = window.location.origin;
            
            // If in development environment, then use http://localhost:8080 for API calls.
            console.log(process.env.NODE_ENV);
            if (process.env.NODE_ENV === 'development') {
                baseApiUrl = 'http://localhost:8080'
            }

            baseApiUrl = baseApiUrl + '/api/v1'

            dispatch({
                type: 'ADD_BASE_API_URL',
                payload: {
                    parentWindow: window.parent,
                    parentWindowUrl: document.location.ancestorOrigins[0],
                    baseApiUrl: baseApiUrl
                }
            });

            fetch(`${store.getState().config.baseApiUrl}/rules`)
                .then(response => {
                    if (! response.ok) {
                        throw Error('Could not retrieve rules')
                    }

                    return response.json()
                }).then(rules => {
                    console.log(rules);
                    
                    dispatch({
                        type: 'UPDATE_RULES',
                        payload: {
                            rules: rules
                        }
                    });

                    dispatch({
                        type: 'UPDATE_UI_MESSAGES',
                        payload: {
                            uiMessages: assertAllValidations()
                        }
                    })
                }).catch(reason => {
                    console.log(reason);
                });
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(App);
