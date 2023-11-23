%%javascript

let qbScriptElement = document.getElementById('qbScript');
if (qbScriptElement !== null) {
    qbScriptElement.remove();
}
var script = document.createElement('script');
script.id = 'qbScript'
script.src = 'http://localhost:8080/connectors/jupyter-notebook?cacheBuster=' + new Date(); // todo: Update this domain.
script.async = false;
document.head.appendChild(script);