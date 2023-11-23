import React, { Component } from "react";
import { connect } from 'react-redux';
import * as Constants from '../../Config/Constants';
import Modal from "react-bootstrap/Modal";
import ProgressBar from 'react-bootstrap/ProgressBar';
import Button from 'react-bootstrap/Button';
import { runQuery } from "../../actions/QueryActions";
import {store} from "../../index";
import { sleep } from '../../Utils/Utils';
import './QueryProgressBar.css';


class QueryProgressBar extends React.Component {

    state = {
        queryId: null,
        isCheckingStatus: false,
        queryResult: {}
    }

    constructor(props) {
        super(props);
    }

    getProgressBar(isComplete, label) {
        return isComplete ?
            <ProgressBar now={100} variant="success" label={label.charAt(0).toUpperCase() + label.slice(1)} /> :
            <ProgressBar animated now={100} label={'Not ' + label.charAt(0).toUpperCase() + label.slice(1)} />
    }

    getBreakElement() {
        return <br></br>;
    }

    buildProgressBars() {
        let bars = [];

        if (this.props.status.toLowerCase() === 'not built') {
            bars.push(this.getProgressBar(false, 'built'));
//            bars.push(<br></br>);
            bars.push(this.getBreakElement());
        }
        else if (this.props.status.toLowerCase() === 'built') {
            bars.push(this.getProgressBar(true, 'built'));
            bars.push(this.getBreakElement());
            bars.push(this.getProgressBar(false, 'queued'));
            bars.push(this.getBreakElement());
        }
        else if (this.props.status.toLowerCase() === 'queued') {
            bars.push(this.getProgressBar(true, 'built'));
            bars.push(this.getBreakElement());
            bars.push(this.getProgressBar(true, 'queued'));
            bars.push(this.getBreakElement());
        }
        else if (this.props.status.toLowerCase() === 'running') {
            bars.push(this.getProgressBar(true, 'built'));
            bars.push(this.getBreakElement());
            bars.push(this.getProgressBar(true, 'queued'));
            bars.push(this.getBreakElement());
            bars.push(this.getProgressBar(false, 'running'));
            bars.push(this.getBreakElement());
        }
        else if (this.props.status.toLowerCase() === 'complete' || this.props.status.toLowerCase() === 'failed') {
            bars.push(this.getProgressBar(true, 'built'));
            bars.push(this.getBreakElement());
            bars.push(this.getProgressBar(true, 'queued'));
            bars.push(this.getBreakElement());
            bars.push(this.getProgressBar(true, 'running'));
            bars.push(this.getBreakElement());
        }

        return bars;
    }

    checkQueryStatus = async () => {
        this.setState({ isCheckingStatus: true })

        await sleep(3);

        let queryId = store.getState().modal.queryProgressModal.queryId;
        let apiUrl = `${store.getState().config.baseApiUrl}/data/query/${queryId}/result`;
        fetch(apiUrl)
            .then(response => response.json())
            .then(json => {
                console.log(json);

                const currentStatus = Object.keys(json.statusStartTimes).slice(-1)[0];
                if (currentStatus.toLowerCase() === 'complete' || currentStatus.toLowerCase() === 'failed') {
                    this.setState({
                        isCheckingStatus: false,
                        queryResult: json
                    });
                }
                else {
                    this.setState({ isCheckingStatus: false })
                }

                const startTimestamp = json.statusStartTimes[currentStatus];
                this.props.updateCheckStatusHandler(currentStatus, startTimestamp);
            })
    }

    getQueryResults = () => {
        const queryStatus = store.getState().modal.queryProgressModal.status.toLowerCase();
        if (queryStatus === 'complete') {
            // Send json to window's parent so the parent can choose what to do with the data.
            let parentWindow = store.getState().config.parentWindow;
            let parentWindowUrl = store.getState().config.parentWindowUrl;
            parentWindow.postMessage(this.state.queryResult, parentWindowUrl);

            this.setState({ checkQueryStatus: false });

            this.props.onCloseHandler();
        }
        else if (queryStatus === 'failed')  {
            const queryResultState = this.state.queryResult;
            const message = queryResultState.message;
            this.props.showMessageModal(message, Constants.MessageModalType.ERROR);
        }
    }

    async componentDidMount() {
        // POST query
        let response = await runQuery();
        if (response.ok) {
            let queryId = await response.json();
            console.log(`Query Id: ${queryId}`);

            this.props.onUpdateQueryIdHandler(queryId);
        }
        else {
            // If the response is not 200.
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const data = await response.json();
                if (data.hasOwnProperty('message')) {
                    this.props.showMessageModal(data.message, Constants.MessageModalType.ERROR);
                }
            }

            this.props.showMessageModal(
                'An error occurred when running the query.  Please contact your administrator for more information.',
                Constants.MessageModalType.ERROR
            );
        }
    }

    render() {
        const queryStatus = this.props.status.toLowerCase();

        let buttonText;
        if (this.state.isCheckingStatus) {
            buttonText = 'Loading...'
        }
        else {
            buttonText = (queryStatus === 'complete' || queryStatus === 'failed') ? 'Get Results' : 'Check Status';
        }

        let completionMessage;
        if (queryStatus === 'complete') {
            completionMessage = <div className="query-success">Success ✅</div>
        }
        else if (queryStatus === 'failed') {
            completionMessage = <div className="query-failed">Failed ⌧</div>
        }

        const buttonClickHandlerFunction = (queryStatus === 'complete' || queryStatus === 'failed') ? this.getQueryResults : this.checkQueryStatus;
        return (
            <Modal
                show={true}
                backdrop='static'
                size="lg"
                onHide={this.props.onCloseHandler}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            Query Id: {this.props.queryId}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body bsPrefix="message-modal-body">
                        {this.buildProgressBars()}
                        {completionMessage}
                    </Modal.Body>
                    <Modal.Footer>
                        <div>
                            <Button
                                variant="primary"
                                disabled={this.state.isCheckingStatus}
                                onClick={buttonClickHandlerFunction}
                            >
                                {buttonText}
                            </Button>
                        </div>
                    </Modal.Footer>
            </Modal>
        );
    }

}

const mapReduxStateToProps = (reduxState) => {
    return {
        ...reduxState.modal.queryProgressModal
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
        onUpdateQueryIdHandler: (queryId) => {
            dispatch({
                type: Constants.UPDATE_QUERY_ID,
                payload: {
                    queryId: queryId
                }
            })
        },
    	onCloseHandler: () => {
    		dispatch({ type: Constants.HIDE_QUERY_PROGRESS_MODAL });
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
        updateCheckStatusHandler: (status, startTimestamp) => {
            dispatch({
                type: Constants.UPDATE_QUERY_STATUS,
                payload: {
                    status: status,
                    timestamp: startTimestamp
                }
            })
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(QueryProgressBar);
