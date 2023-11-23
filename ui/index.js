const express = require('express')
const fs = require('fs');
const app = express()
const port = 8081

const JUPYTER_NOTEBOOK = 'jupyter-notebook';
const EXCEL_MANIFEST = 'excel-manifest';
const TABLEAU_WDC = 'tableau-wdc';
const SANDBOX = 'sandbox';
const EXCEL_ADD_IN = 'excel-add-in';

const SERVING_DOMAIN = process.env.SERVING_DOMAIN;

const cacheConnectorFile = (connectorType, connectorFilePath) => {
  fs.readFile(
    connectorFilePath,
    'utf-8',
    (err, data) => {
        if (err) {
          console.error(err.message);
        }
        
        let replacedData = data.replace(/\${servingDomain}/g, SERVING_DOMAIN);
        CONNECTOR_CACHE[connectorType] = replacedData;
    }
  )
}

const CONNECTOR_CACHE = {}
cacheConnectorFile(JUPYTER_NOTEBOOK, '/usr/app/connectors/jupyter-notebook.js'),  // todo:  parameterize the path.
cacheConnectorFile(EXCEL_MANIFEST, '/usr/app/connectors/manifest.xml'),
cacheConnectorFile(TABLEAU_WDC, '/usr/app/connectors/wdc.html'),
cacheConnectorFile(SANDBOX, '/usr/app/connectors/sandbox.html'),
cacheConnectorFile(EXCEL_ADD_IN, '/usr/app/connectors/excel-taskpane.html')

const sendConnectorResponse = (response, connectorType) => {
    let connectorFile = CONNECTOR_CACHE[connectorType]
    if (! connectorFile) {
        console.error(`Could not find ${connectorType} connector file`);
        response.sendStatus(500);
    }

    response.setHeader('Content-Type', 'text/html');
    response.status = 200;
    response.send(connectorFile);
}

app.get('/connectors/jupyter-notebook', (req, res) => {
    sendConnectorResponse(res, JUPYTER_NOTEBOOK);
})

app.get('/connectors/office-365/excel/manifest.xml', (req, res) => {
    sendConnectorResponse(res, EXCEL_MANIFEST);
})

app.get('/connectors/wdc', (req, res) => {
    sendConnectorResponse(res, TABLEAU_WDC);
})

app.get('/connectors/sandbox', (req, res) => {
    sendConnectorResponse(res, SANDBOX);
})

app.get('/connectors/office-365/excel', (req, res) => {
    sendConnectorResponse(res, EXCEL_ADD_IN);
})

// Serve static core UI at `/`.
app.use('/', express.static('/usr/app/core'));

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})
