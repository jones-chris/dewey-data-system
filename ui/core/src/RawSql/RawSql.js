import React from 'react';
import { connect } from 'react-redux';
import { store } from "../index";
import Form from 'react-bootstrap/Form';
import { UPDATE_RAW_SQL } from '../Config/Constants';
import Container from 'react-bootstrap/Container';
import './RawSql.css';
import { assertAllValidations } from "../Validators/Validators";

export const RawSql = (props) => {
	return (
		<div hidden={props.hidden === 'true'} className="raw-sql">
			<Form>
	            <Form.Group controlId="rawSql">
	            	<Form.Control 
	            		as="textarea" 
	            		rows={10}
	            		placeholder="Type your SQL statement..."
	            		onChange={(event) => props.onRawSqlChange(event.target.value)}
	        		/>    
	            </Form.Group>
	        </Form>
        </div>
	);
};

const mapReduxStateToProps = (reduxState) => {
    return reduxState
};

const mapDispatchToProps = (dispatch) => {
    return {
        onRawSqlChange: (rawSql) => {
            dispatch({
                type: UPDATE_RAW_SQL,
                payload: {
                    rawSql: rawSql
                }
            });

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            })
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(RawSql);