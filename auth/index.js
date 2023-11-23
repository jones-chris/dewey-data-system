const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express()
const PORT = 8080

const assertExists = (variableName, variableValue) => {
    if (! variableValue) {
        throw Error(`${variableName} should exist, but is ${variableValue}`)
    }

    if (variableValue.toLowerCase() === 'false') {
        return false;
    } else if (variableName.toLowerCase() === 'true') {
        return true;
    }

    return variableValue;
}

const UI_DOMAIN = assertExists('UI_DOMAIN', process.env.UI_DOMAIN);
const API_DOMAIN = assertExists('API_DOMAIN', process.env.API_DOMAIN);
const ENABLE_AUTH = assertExists('ENABLE_AUTH', process.env.ENABLE_AUTH);

console.log(`UI_DOMAIN: ${UI_DOMAIN}`);
console.log(`API_DOMAIN: ${API_DOMAIN}`)
console.log(`ENABLE_AUTH: ${ENABLE_AUTH}`)

// Authorization interceptor.
app.use('', (req, res, next) => {
    if (! ENABLE_AUTH) {
        next();
    } else {
        if (req.headers.authorization) {
            next();
        } else {
            res.sendStatus(403);
        }
    }
 });

 // Proxy endpoints.
app.use(
    '/api/*',
    createProxyMiddleware({
        target: API_DOMAIN,
        changeOrigin: true,

    })
);
app.use(
    '/connectors/*',
    createProxyMiddleware({
        target: UI_DOMAIN,
        changeOrigin: true,

    })
);
app.use(
    '/',
    createProxyMiddleware({
        target: UI_DOMAIN,
        changeOrigin: true,
    })
);

 // Start express.
 app.listen(PORT, () => {
    console.log(`Starting Proxy on port ${PORT}`);
 });