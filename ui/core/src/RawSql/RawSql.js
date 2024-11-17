import React from 'react';
import { connect } from 'react-redux';
import Form from 'react-bootstrap/Form';
import { UPDATE_RAW_SQL, UPDATE_RAW_SQL_PARAMETERS, UPDATE_UI_MESSAGES } from '../Config/Constants';
import { store } from "../index";
import './RawSql.css';
import { assertAllValidations } from "../Validators/Validators";
import Table from 'react-bootstrap/Table';
import { extractParameters } from '../actions/QueryActions';
import { QueryParameter, NumberRangeConstraint, ColumnConstraint, EnumConstraint } from '../Models/QueryParameter';
import { enforceInputElementMinMax } from '../Utils/Utils';


export const RawSql = (props) => {
    const parameterTableRows = props.query.rawSqlParameters.map(parameter => {
        return (
            <tr>
                <td>{parameter.name}</td>
                <td>
                    <select onChange={(event) => props.onParameterDataTypeChange(parameter.name, event.target.value)}>
                        <option value="oneString" selected={parameter.dataType === 'oneString'}>One String</option>
                        <option value="manyStrings" selected={parameter.dataType === 'manyStrings'}>Many Strings</option>
                        <option value="oneNumber" selected={parameter.dataType === 'oneNumber'}>One Number</option>
                        <option value="manyNumbers" selected={parameter.dataType === 'manyNumbers'}>Many Numbers</option>
                        <option value="boolean" selected={parameter.dataType === 'boolean'}>True/False</option>
                    </select>
                </td>
                <td>
                    <select onChange={(event) => props.onParameterConstraintTypeChange(parameter.name, event.target.value)}>
                        <option value="noConstraint" selected={parameter.constraintType === 'noConstraint'}>No Contraint</option>
                        <option value="columnConstraint" selected={parameter.constraintType === 'columnConstraint'}>Column Constraint</option>
                        <option value="numberRangeConstraint" selected={parameter.constraintType === 'numberRangeConstraint'}>Number Range Constraint</option>
                        <option value="enumConstraint" selected={parameter.constraintType === 'enumConstraint'}>Enum Constraint</option>
                    </select>
                </td>
                <td>{buildConstraintJsx(parameter, props)}</td>
                <td>{buildValuesJsx(parameter)}</td>
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
            const parameters = extractParameters(rawSql);

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
                type: UPDATE_UI_MESSAGES,
                payload: {
                    uiMessages: assertAllValidations()
                }
            })
        },
        onParameterDataTypeChange: (parameterName, dataType) => {
            // Get the state's query parameters
            const newQueryParameters = [...store.getState().query.rawSqlParameters];

            // Find the query parameter by name and update the query parameter's data type.
            const parameterIndex = newQueryParameters.findIndex(parameter => parameterName === parameter.name);
            newQueryParameters[parameterIndex].dataType = dataType;

            // Dispatch to reducer.
            dispatch({
                type: UPDATE_RAW_SQL_PARAMETERS,
                payload: {
                    parameters: newQueryParameters
                }
            });

            dispatch({
                type: UPDATE_UI_MESSAGES,
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        onParameterConstraintTypeChange: (parameterName, constraintType) => {
            const newQueryParameters = [...store.getState().query.rawSqlParameters];

            let constraint = null;
            if (constraintType === 'columnConstraint') {
                constraint = new ColumnConstraint();
            }
            else if (constraintType === 'numberRangeConstraint') {
                constraint = new NumberRangeConstraint();
            }
            else if (constraintType === 'enumConstraint') {
                constraint = new EnumConstraint();
            }
            else {
                console.error(`Did not recognize constrain type ${constraintType}`);
            }

            const parameterIndex = newQueryParameters.findIndex(parameter => parameterName === parameter.name);
            newQueryParameters[parameterIndex].constraintType = constraintType;
            newQueryParameters[parameterIndex].constraint = constraint;

            dispatch({
                type: UPDATE_RAW_SQL_PARAMETERS,
                payload: {
                    parameters: newQueryParameters
                }
            });

            dispatch({
                type: UPDATE_UI_MESSAGES,
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        onNumberRangeConstraintMinimumChange: (parameterName, minimum) => {
            const newQueryParameters = [...store.getState().query.rawSqlParameters];
            const parameterIndex = newQueryParameters.findIndex(parameter => parameterName === parameter.name);
            newQueryParameters[parameterIndex].constraint.minimum = parseInt(minimum);

            dispatch({
                type: UPDATE_RAW_SQL_PARAMETERS,
                payload: {
                    parameters: newQueryParameters
                }
            });

            dispatch({
                type: UPDATE_UI_MESSAGES,
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        onNumberRangeConstraintMaximumChange: (parameterName, maximum) => {
            const newQueryParameters = [...store.getState().query.rawSqlParameters];
            const parameterIndex = newQueryParameters.findIndex(parameter => parameterName === parameter.name);
            newQueryParameters[parameterIndex].constraint.maximum = parseInt(maximum);

            dispatch({
                type: UPDATE_RAW_SQL_PARAMETERS,
                payload: {
                    parameters: newQueryParameters
                }
            });

            dispatch({
                type: UPDATE_UI_MESSAGES,
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        }
    }
};

const buildConstraintJsx = (parameter, props) => {
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
                        <input type="number" name={minInputElementName} onChange={(event) => props.onNumberRangeConstraintMinimumChange(parameter.name, event.target.value)} value={parameter.constraint.minimum}></input>
                    </li>
                    <li>
                        <label for={maxInputElementName}>Maximum</label>
                        <input type="number" name={maxInputElementName} onChange={(event) => props.onNumberRangeConstraintMaximumChange(parameter.name, event.target.value)} value={parameter.constraint.maximum}></input>
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
        return (<input type="text"></input>)
    }
    else if (parameter.constraintType === 'numberRangeConstraint') {
        return (<input type="text" onChange={(event) => props.}></input>)
    }
    else if (parameter.constraintType === 'enumConstraint') {
        const optionElements = parameter.constraint.enumValues
            .map(enumValue => {
                <option 
                    value={enumValue}
                    selected={parameter.value === enumValue}
                >
                    enumValue
                </option>
            });

        return (
            <select>
                {optionElements}
            </select>
        );
    }
    else {
        console.error(`Did not recognize parameter ${parameter.name}'s constraint type ${parameter.constraintType}`);
    }
}

export default connect(mapReduxStateToProps, mapDispatchToProps)(RawSql);