import React from 'react';
import ReactDOM from 'react-dom';
import {combineReducers, createStore} from "redux";
import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import menuBarReducer from "./Store/MenuBarReducer";
import queryReducer from "./Store/QueryReducer";
import { Provider } from 'react-redux'
import configReducer from "./Store/ConfigReducer";
import modalReducer from './Store/ModalReducer';
import dataCatalogReducer from './Store/DataCatalogReducer';

export const store = createStore(combineReducers(
    {
        config: configReducer,
        menuBar: menuBarReducer,
        query: queryReducer,
        modal: modalReducer,
        dataCatalog: dataCatalogReducer
    }
));

ReactDOM.render(
    <Provider store={store}>
        <App />
    </Provider>
    , document.getElementById('root'));
registerServiceWorker();
