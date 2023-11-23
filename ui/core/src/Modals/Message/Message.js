import React from "react";
import { connect } from 'react-redux';
import * as Constants from '../../Config/Constants';
import Modal from "react-bootstrap/Modal";
import './Message.css';

const messageTypeToModalPropertiesMapping = {
	info: {
		title: <h3 className="info-text">&#9432;</h3>
	},
	error: {
		title: <h3 className="error-text">&#9888;</h3>
	}
};

export const Message = (props) => {
	let modalTitleElement = messageTypeToModalPropertiesMapping[props.type].title;

	return (
		<Modal 
			show={true}
			backdrop='static' 
			size="lg" 
			onHide={props.onCloseHandler}>
		        
		        <Modal.Header closeButton>
		        	<Modal.Title>
		        		{modalTitleElement}
		        	</Modal.Title>
		        </Modal.Header>
		        
		        <Modal.Body bsPrefix="message-modal-body">
		        	{props.messageText}
		        	<img alt=""
		        		className="message-img" 
		        		src={props.imageUrl}>
		        	</img>
		        </Modal.Body>

	      </Modal>
	);
}

const mapReduxStateToProps = (reduxState) => {
    return {
        ...reduxState.modal.messageModal
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
    	onCloseHandler: () => {
    		dispatch({ type: Constants.HIDE_MESSAGE_MODAL });
    	}
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(Message);
