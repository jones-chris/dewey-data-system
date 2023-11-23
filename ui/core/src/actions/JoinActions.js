import {store} from "../index";
import * as Constants from '../Config/Constants';
import * as Utils from "../Utils/Utils";

export const addJoin = () => {
    let queryState = store.getState().query;

    // Get the default table and columns.
    let defaultTable = (queryState.availableTables.length > 0) ? queryState.availableTables[0] : '';

    // Get all columns for default table.
    let defaultAvailableColumns = (queryState.availableTables.length > 0)
        ? queryState.availableColumns.filter(column => {
            return column.databaseName === defaultTable.databaseName
                && column.schemaName === defaultTable.schemaName
                && column.tableName === defaultTable.tableName;
        })
        : [];

    let defaultSelectedJoinColumns = (defaultAvailableColumns.length > 0) ? [defaultAvailableColumns[0]] : []

    // Create a new join object and add it to the state's `joins` item.
    let newJoin = {
       joinType: Constants.JOIN_IMAGES[0].name,
       parentTable: defaultTable,
       targetTable: defaultTable,
       parentJoinColumns: [...defaultSelectedJoinColumns],
       targetJoinColumns: [...defaultSelectedJoinColumns],
       metadata: {
           id: queryState.joins.length,
           joinImageUrl: Constants.JOIN_IMAGES[0].image
       }
    };

    let newJoins = [...store.getState().query.joins];
    newJoins.push(newJoin);

    return {
        type: 'ADD_JOIN',
        payload: {
            joins: newJoins
        }
    };
};

export const deleteJoin = (joinId) => {
    return {
        type: 'DELETE_JOIN',
        payload: {
            joinId: joinId
        }
    }
};

export const changeJoinType = (joinId) => {
    // Get next join image based on currentJoinType.
    joinId = parseInt(joinId);

    let joinsState = [...store.getState().query.joins];
    let currentJoinType = joinsState.find(join => join.metadata.id === joinId).joinType;

    let currentJoinTypeIndex;
    for (let i=0; i<Constants.JOIN_IMAGES.length; i++) {
        let join = Constants.JOIN_IMAGES[i];
        if (join.name === currentJoinType) {
            currentJoinTypeIndex = i;
            break;
        }
    }

    let nextJoinType;
    let nextJoinImageUrl;
    try {
        nextJoinType = Constants.JOIN_IMAGES[currentJoinTypeIndex + 1].name;
        nextJoinImageUrl = Constants.JOIN_IMAGES[currentJoinTypeIndex + 1].image;
    } catch (e) {
        nextJoinType = Constants.JOIN_IMAGES[0].name; // Default to first item in case of index out of range exception.
        nextJoinImageUrl = Constants.JOIN_IMAGES[0].image; // Default to first item in case of index out of range exception.
    }

    // Copy state
    joinsState.forEach(join => {
        if (join.metadata.id === joinId) {
            join.joinType = nextJoinType;
            join.metadata.joinImageUrl = nextJoinImageUrl;
        }
    });

    return {
        type: 'CHANGE_JOIN_TYPE',
        payload: {
            joins: joinsState
        }
    }
};

export const changeTable = (joinId, parentTableElementName, targetTableElementName, parentOrTargetChange) => {
    let queryState = store.getState().query;

    let parentTableName = Utils.getSelectedOptions(document.getElementById(parentTableElementName))[0];
    let parentTableObject = queryState.availableTables.find(table => { return table.fullyQualifiedName === parentTableName; });

    let targetTableName = Utils.getSelectedOptions(document.getElementById(targetTableElementName))[0];
    let targetTableObject = queryState.availableTables.find(table => { return table.fullyQualifiedName === targetTableName; });

    let newJoins = [...queryState.joins];

    newJoins.forEach(join => {
        if (join.metadata.id === joinId) {

            // Update join's parent and target tables.
            join.parentTable = parentTableObject;
            join.targetTable = targetTableObject;

            if (queryState.availableColumns.length !== 0) {
                // Replace parent join columns if the parent table changed.
                if (parentOrTargetChange === 'PARENT') {
                    let firstParentJoinColumn = queryState.availableColumns.find(column => {  // find() because we just need first item that meets criteria.
                        return column.databaseName === parentTableObject.databaseName && column.schemaName === parentTableObject.schemaName && column.tableName === parentTableObject.tableName;
                    });
                    let numberOfParentJoinColumns = join.parentJoinColumns.length;
                    join.parentJoinColumns = [];
                    for (let i = 0; i < numberOfParentJoinColumns; i++) {
                        join.parentJoinColumns.push(firstParentJoinColumn);
                    }
                }

                // Replace target join columns if the target table changed.
                if (parentOrTargetChange === 'TARGET') {
                   let firstTargetJoinColumn = queryState.availableColumns.find(column => {  // find() because we just need first item that meets criteria.
                        return column.databaseName === targetTableObject.databaseName && column.schemaName === targetTableObject.schemaName && column.tableName === targetTableObject.tableName;
                    });
                    let numberOfTargetJoinColumns = join.targetJoinColumns.length;
                    join.targetJoinColumns = [];
                    for (let i = 0; i < numberOfTargetJoinColumns; i++) {
                        join.targetJoinColumns.push(firstTargetJoinColumn);
                    }
                }
            }
        }
    });

    return {
        type: 'CHANGE_TABLE',
        payload: {
            joins: newJoins
        }
    }
};

export const changeColumn = (joinId, parentJoinColumnsElementId, targetJoinColumnsElementId) => {
    let queryState = store.getState().query;

    let parentColumnEl = document.getElementById(parentJoinColumnsElementId);
    let index = parseInt(parentColumnEl.getAttribute('data-index'));

    let parentColumn = Utils.getSelectedOptions(parentColumnEl)[0];
    let parentColumnObject = queryState.availableColumns.find(column => { return column.fullyQualifiedName === parentColumn; });

    let targetColumn = Utils.getSelectedOptions(document.getElementById(targetJoinColumnsElementId))[0];
    let targetColumnObject = queryState.availableColumns.find(column => { return column.fullyQualifiedName === targetColumn; });

    let newJoins = [...store.getState().query.joins];
    newJoins.forEach(join => {
        if (join.metadata.id === joinId) {
            join.parentJoinColumns.splice(index, 1, parentColumnObject);
            join.targetJoinColumns.splice(index, 1, targetColumnObject);
        }
    });

    return {
        type: 'CHANGE_COLUMN',
        payload: {
            joins: newJoins
        }
    }
};

export const addJoinColumn = (joinId) => {
    let newJoins = [...store.getState().query.joins];

    newJoins.forEach(join => {
        // Get first available parent column and first available target column.
        let firstAvailableParentColumn = {...join.parentJoinColumns[0]};
        let firstAvailableTargetColumn = {...join.targetJoinColumns[0]};

        // Add first available parent column and first available target column as another item at the end of the array.
        if (join.metadata.id === joinId) {
            join.parentJoinColumns.push(firstAvailableParentColumn);
            join.targetJoinColumns.push(firstAvailableTargetColumn);
        }
    });

    return {
        type: 'ADD_JOIN_COLUMN',
        payload: {
            joins: newJoins
        }
    }
};

export const deleteJoinColumn = (joinId, joinColumnIndex) => {
    let newJoins = [...store.getState().query.joins];

    newJoins.forEach(join => {
        if (join.metadata.id === joinId) {
            // Remove the parent join column and target join column at the joinColumnIndex.
            join.parentJoinColumns.splice(joinColumnIndex, 1);
            join.targetJoinColumns.splice(joinColumnIndex, 1)
        }
    });

    return {
        type: 'DELETE_JOIN_COLUMN',
        payload: {
            joins: newJoins
        }
    }
};

/**
 * Returns an array of the joins with each join's metadata removed.
 *
 * @param joins The array of joins to remove the metadata attribute from.
 * @returns An array of joins with the metadata removed.
 */
export const removeJoinMetadata = (joins) => {
    return joins.map(join => {
        join = Object.assign({}, join);
        delete join.metadata;
        return join;
    })
};


