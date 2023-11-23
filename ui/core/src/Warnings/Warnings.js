import React, { Component } from 'react';
import {connect} from "react-redux";
import Alert from 'react-bootstrap/Alert';
import './Warnings.css';


export const Warnings = (props) => {

    let uiMessages = [];
    for (let property in props) {
        if (props[property].hasOwnProperty('uiMessages')) {
            props[property].uiMessages.forEach(uiMessage => {
                uiMessages.push(
                    <Alert variant="danger">{uiMessage}</Alert>
                )
            });
        }
    }

    return (
        <div className="warnings">
            {uiMessages}
        </div>
    );

}

const mapReduxStateToProps = (reduxState) => {
    return reduxState.menuBar
};

export default connect(mapReduxStateToProps, null)(Warnings);
