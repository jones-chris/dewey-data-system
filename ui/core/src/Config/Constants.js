import leftJoinExcluding from "../Images/left_join_excluding.png";
import leftJoin from "../Images/left_join.png";
import innerJoin from "../Images/inner_join.png";
import rightJoin from "../Images/right_join.png";
import rightJoinExcluding from "../Images/right_join_excluding.png";
import fullOuterJoin from "../Images/full_outer_join.png";
import fullOuterJoinExcluding from "../Images/full_outer_join_excluding.png";


// HTML section names.
export const SUB_QUERIES = 'Sub Queries';
export const JOINS = 'Joins';
export const SCHEMAS_AND_TABLES = 'Schemas & Tables';
export const COLUMNS = 'Columns';
export const CRITERIA = 'Criteria';
export const OTHER_OPTIONS = 'Other Options';
export const QUERY_TEMPLATES = 'Query Templates';
export const QUERY_BUILDER = 'Query Builder';
export const RAW_SQL = 'Raw SQL';
export const DATA_CATALOG = 'Data Catalog';

// Join images.
export const JOIN_IMAGES = [
    {'name': 'LEFT_EXCLUDING',         'image': leftJoinExcluding},
    {'name': 'LEFT',                   'image': leftJoin},
    {'name': 'INNER',                  'image': innerJoin},
    {'name': 'RIGHT',                  'image': rightJoin},
    {'name': 'RIGHT_EXCLUDING',        'image': rightJoinExcluding},
    {'name': 'FULL_OUTER',             'image': fullOuterJoin},
    {'name': 'FULL_OUTER_EXCLUDING',   'image': fullOuterJoinExcluding}
];

// Criterion model object attributes that can be updated.
export const CONJUNCTION = 'conjunction';
export const COLUMN = 'column';
export const OPERATOR = 'operator';
export const FILTER = 'filter';

// Modal constants.
export const MessageModalType = {
    INFO: 'info',
    ERROR: 'error'
}

// Redux Reducer Constants:
export const SHOW_MESSAGE_MODAL = 'SHOW_MESSAGE_MODAL';
export const HIDE_MESSAGE_MODAL = 'HIDE_MESSAGE_MODAL';
export const SHOW_QUERY_BUILDER_APPLICATION = 'SHOW_QUERY_BUILDER_APPLICATION';
export const SHOW_RAW_SQL_APPLICATION = 'SHOW_RAW_SQL_APPLICATION';
export const SHOW_DATA_CATALOG_APPLICATION = 'SHOW_DATA_CATALOG_APPLICATION';
export const UPDATE_RAW_SQL = 'UPDATE_RAW_SQL';
export const IMPORT_QUERY_TEMPLATE = 'IMPORT_QUERY_TEMPLATE';
export const SHOW_QUERY_PROGRESS_MODAL = 'SHOW_QUERY_PROGRESS_MODAL';
export const UPDATE_QUERY_ID = 'UPDATE_QUERY_ID';
export const HIDE_QUERY_PROGRESS_MODAL = 'HIDE_QUERY_PROGRESS_MODAL';
export const UPDATE_QUERY_STATUS = 'UPDATE_QUERY_STATUS';
