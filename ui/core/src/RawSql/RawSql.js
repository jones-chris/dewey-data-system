import React from 'react';
import { connect } from 'react-redux';
import { store } from "../index";
import Form from 'react-bootstrap/Form';
import { UPDATE_RAW_SQL } from '../Config/Constants';
import './RawSql.css';
import { assertAllValidations } from "../Validators/Validators";
import Table from 'react-bootstrap/Table';
import { extractParameters } from '../actions/QueryActions';


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
            <Table striped bordered hover>
                <thead>
                    <tr>
                    <th>Parameter Name</th>
                    <th>Values</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>1</td>
                        <td>@mdo</td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td>@fat</td>
                    </tr>
                    <tr>
                        <td>3</td>
                        <td>@twitter</td>
                    </tr>
                </tbody>
            </Table>
        </div>
	);
};

const mapReduxStateToProps = (reduxState) => {
    return reduxState
};

const mapDispatchToProps = (dispatch) => {
    return {
        onRawSqlChange: (rawSql) => {
            console.log(`rawSql is: ${rawSql}`)
            const parameters = extractParameters(rawSql);
            console.log(`parameters is: ${parameters}`)

            dispatch({
                type: UPDATE_RAW_SQL,
                payload: {
                    rawSql: rawSql,
                    parameters: parameters
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