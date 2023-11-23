import React, { Component } from 'react';
import './Joins.css'
import {connect} from "react-redux";
import {
    addJoin,
    deleteJoin,
    changeJoinType,
    changeTable,
    addJoinColumn,
    changeColumn,
    deleteJoinColumn
} from "../actions/JoinActions";
import {assertAllValidations} from "../Validators/Validators";
import {store} from "../index";


class Joins extends Component {

    constructor(props) {
        super(props);
    }

    getAvailableColumns = (table, dataIndex) => {
        let availableColumns = store.getState().query.availableColumns;
        return availableColumns.filter(availableColumn => availableColumn.databaseName === table.databaseName && availableColumn.schemaName === table.schemaName && availableColumn.tableName === table.tableName);
    };

    render() {
        // Create joins JSX.
        const joinsJsx = this.props.joins.map(join => {
            // Create available tables JSX for parent table select element.
            const availableTablesParentTableOptions = this.props.availableTables.map(table => {
                return <option key={table.fullyQualifiedName}
                               value={table.fullyQualifiedName}
                               selected={join.parentTable.fullyQualifiedName === table.fullyQualifiedName}
                >
                    {table.tableName}
                </option>
            });

            // Create available tables JSX for target table select element.
            const availableTablesTargetTableOptions = this.props.availableTables.map(table => {
                return <option key={table.fullyQualifiedName}
                               value={table.fullyQualifiedName}
                               selected={join.targetTable.fullyQualifiedName === table.fullyQualifiedName}
                >
                    {table.tableName}
                </option>
            });

            // Create a join column div that includes a list of the parent join columns and target join columns.
            let joinColumns = [];

            let index = Math.random().toString().slice(-4);
            let availableParentColumns = this.getAvailableColumns(join.parentTable, index);
            let availableTargetColumns = this.getAvailableColumns(join.targetTable, index);

            join.parentJoinColumns.forEach((parentJoinColumn, index) => {
                let parentJoinColumnsId = `joins${join.metadata.id}.parentJoinColumns${index}`;
                let targetJoinColumnsId = `joins${join.metadata.id}.targetJoinColumns${index}`;

                // Convert each available column into JSX and mark whether it's selected.
                let availableParentColumnsJsx = availableParentColumns.map(availableParentColumn =>
                    <option key={availableParentColumn.fullyQualifiedName}
                            data-index={index}
                            value={availableParentColumn.fullyQualifiedName}
                            selected={parentJoinColumn.fullyQualifiedName === availableParentColumn.fullyQualifiedName}
                    >
                        {availableParentColumn.columnName}
                    </option>
                );

                let availableTargetColumnsJsx = availableTargetColumns.map(availableTargetColumn =>
                    <option key={availableTargetColumn.fullyQualifiedName}
                            data-index={index}
                            value={availableTargetColumn.fullyQualifiedName}
                            selected={join.targetJoinColumns[index].fullyQualifiedName === availableTargetColumn.fullyQualifiedName}
                    >
                        {availableTargetColumn.columnName}
                    </option>
                );

                joinColumns.push(
                    <div key={index} data-index={index}>
                        <select id={parentJoinColumnsId}
                                data-index={index}
                                onChange={() => this.props.changeColumn(join.metadata.id, parentJoinColumnsId, targetJoinColumnsId)}
                        >
                            {availableParentColumnsJsx}
                        </select>

                        <b> = </b>

                        <select id={targetJoinColumnsId}
                                data-index={index}
                                onChange={() => this.props.changeColumn(join.metadata.id, parentJoinColumnsId, targetJoinColumnsId)}
                        >
                            {availableTargetColumnsJsx}
                        </select>

                        <span>
                            {/*Add parent and target column (the ON clause of the JOIN clause)*/}
                            <button id={`joins${join.metadata.id}.addParentAndTargetColumn`} type="button"
                                    onClick={() => this.props.addJoinColumn(join.metadata.id)}
                            >
                                +
                            </button>

                            {/*Delete parent and target column (the ON clause of the JOIN clause*/}
                            <button id={`joins${join.metadata.id}.addParentAndTargetColumn`} type="button"
                                    onClick={() => this.props.deleteJoinColumn(join.metadata.id, index)}
                            >
                                X
                            </button>
                        </span>

                    </div>
                )
            });

            return <div key={join.metadata.id} id={`join-row${join.metadata.id}`} className="join-row">
                {/*Delete join*/}
                <button id={`joins-deleteButton-${join.metadata.id}`} className="delete-join-button" type="button"
                        onClick={() => this.props.deleteJoin(join.metadata.id)}>
                    X
                </button>

                <input id={`joins${join.metadata.id}.joinType`} hidden defaultValue={join.joinType}/>

                <select id={`joins${join.metadata.id}.parentTable`}
                        onChange={(event) => this.props.changeTable(join.metadata.id, `joins${join.metadata.id}.parentTable`, `joins${join.metadata.id}.targetTable`, 'PARENT')}
                >
                    {availableTablesParentTableOptions}
                </select>

                <img id={`joins-image-${join.metadata.id}`}
                     className="join-image"
                     src={join.metadata.joinImageUrl}
                     onClick={() => this.props.changeJoinType(join.metadata.id)}/>

                <select id={`joins${join.metadata.id}.targetTable`}
                        onChange={(event) => this.props.changeTable(join.metadata.id, `joins${join.metadata.id}.parentTable`, `joins${join.metadata.id}.targetTable`, 'TARGET')}
                >
                    {availableTablesTargetTableOptions}
                </select>

                {/*Join columns*/}
                {joinColumns}
            </div>
        });

        return (
            <div id="joinsDiv" className="joins-div" hidden={this.props.hidden === 'true'}>
                <button id="addJoin" name="addJoin" type="button"
                        onClick={this.props.addJoin}
                >
                    Add Join
                </button>

                <div>
                    {joinsJsx}
                </div>
            </div>
        );
    }
}

const mapReduxStateToProps = (reduxState) => {
    return reduxState.query;
};

const mapDispatchToProps = (dispatch) => {
    return {
        addJoin: () => {
            dispatch(addJoin());

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        deleteJoin: (joinId) => {
            dispatch(deleteJoin(joinId));

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        changeJoinType: (joinId) => dispatch(changeJoinType(joinId)),
        changeTable: (joinId, parentTableElementName, targetTableElementName, parentOrTargetChange) => {
            dispatch(
                changeTable(joinId, parentTableElementName, targetTableElementName, parentOrTargetChange)
            );

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        changeColumn: (joinId, parentJoinColumnsElementId, targetJoinColumnsElementId) => {
            dispatch(
                changeColumn(joinId, parentJoinColumnsElementId, targetJoinColumnsElementId)
            );
        },
        addJoinColumn: (joinId) => {
            dispatch(addJoinColumn(joinId));

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });            
        },
        deleteJoinColumn: (joinId, joinColumnIndex) => {
            dispatch(
                deleteJoinColumn(joinId, joinColumnIndex)
            );

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(Joins);
