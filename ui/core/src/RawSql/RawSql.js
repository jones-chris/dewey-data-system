import React from 'react';
import { connect } from 'react-redux';
import Form from 'react-bootstrap/Form';
import { UPDATE_RAW_SQL } from '../Config/Constants';
import './RawSql.css';
import { assertAllValidations } from "../Validators/Validators";
import Table from 'react-bootstrap/Table';
import { extractParameters } from '../actions/QueryActions';
import { QueryParameter } from '../Models/QueryParameter';


export const RawSql = (props) => {
    const parameterTableRows = props.query.rawSqlParameters.map(parameter => {
        return (
            <tr>
                <td>{parameter.name}</td>
                <td>
                    <select>
                        <option value="oneString" selected={parameter.dataType === 'oneString'}>One String</option>
                        <option value="manyStrings" selected={parameter.dataType === 'manyStrings'}>Many Strings</option>
                        <option value="oneNumber" selected={parameter.dataType === 'oneNumber'}>One Number</option>
                        <option value="manyNumbers" selected={parameter.dataType === 'manyNumbers'}>Many Numbers</option>
                        <option value="boolean" selected={parameter.dataType === 'boolean'}>True/False</option>
                    </select>
                </td>
                <td>
                    <select>
                        <option selected={parameter.constraintType === 'noConstraint'}>No Contraint</option>
                        <option selected={parameter.constraintType === 'columnConstraint'}>Column Constraint</option>
                        <option selected={parameter.constraintType === 'numberRangeConstraint'}>Number Range Constraint</option>
                        <option selected={parameter.constraintType === 'enumConstraint'}>Enum Constraint</option>
                    </select>
                </td>
                <td>{buildConstraintJsx(parameter)}</td>
                <td>{parameter.value}</td>
            </tr>
        );
            
    });

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
                        <th>Data Type</th>
                        <th>Constraint Type</th>
                        <th>Constraint</th>
                        <th>Values</th>
                    </tr>
                </thead>
                <tbody>
                    {parameterTableRows}
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
            // console.log(`rawSql is: ${rawSql}`)
            const parameters = extractParameters(rawSql);
            // console.log(`parameters is: ${parameters}`);
            const queryParameters = parameters.map(parameter => {
                let queryParameter = new QueryParameter();
                queryParameter.name = parameter;

                return queryParameter;
            });

            dispatch({
                type: UPDATE_RAW_SQL,
                payload: {
                    rawSql: rawSql,
                    parameters: queryParameters
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

const buildConstraintJsx = (parameter) => {
    if (parameter.constraintType === 'noConstraint') {
        return null;
    }
    else if (parameter.constraintType === 'columnConstraint') {

    }
    else if (parameter.constraintType === 'numberRangeConstraint') {
        const minInputElementName = `min_${parameter.name}`;
        const maxInputElementName = `max_${parameter.name}`;

        return (
            <form>
                <ol>
                    <li>
                        <label for={minInputElementName}>Minimum</label>
                        <input type="number" name={minInputElementName}></input>
                    </li>
                    <li>
                        <label for={maxInputElementName}>Maximum</label>
                        <input type="number" name={maxInputElementName}></input>
                    </li>
                </ol>
            </form>
        )
    }
    else if (parameter.constraintType === 'enumConstraint') {

    }
    else {
        console.error(`Did not recognize parameter ${parameter.name}'s constraint type ${parameter.constraintType}`);
    }
};

const buildValuesJsx = (parameter) => {
    if (parameter.constraintType === 'noConstraint') {
        return (<textarea></textarea>);
    }
    else if (parameter.constraintType === 'columnConstraint') {
        
    }
    else if (parameter.constraintType === 'numberRangeConstraint') {
        return (<input type="number" min={parameter.constraint.minimum} max={parameter.constraint.maximum}></input>)
    }
    else if (parameter.constraintType === 'enumConstraint') {
        
    }
    else {
        console.error(`Did not recognize parameter ${parameter.name}'s constraint type ${parameter.constraintType}`);
    }
}

export default connect(mapReduxStateToProps, mapDispatchToProps)(RawSql);