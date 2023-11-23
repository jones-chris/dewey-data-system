window.onmessage = (event) => {
  console.log('In the event handler!');
  if (event.origin === '${servingDomain}') {
      console.log('event.origin matches serving domain');
      console.log(event);

      delete event.data.sql; // Delete SQL property because escaping causes error when deserializing the JSON in Python.
      delete event.data.selectStatement // Delete the selectStatement property because quoted filter values are not escaped which causes an error when deserializing the JSON in Python.

      // This object handles logging the output of the IPython.notebook.kernel.execute commands.
      const callbacks = {
              iopub : {
                   output : (data) => {
                       if (data.msg_type === 'error') {
                           console.error(data);
                       }
                       else {
                           console.log(data);
                       }
                   },
          }
      }

      // Load the event's data (the query result) into a Python string.
      let dataJsonString = JSON.stringify(event.data);
      let command = "qb_results=" + "'" + dataJsonString + "'";
      IPython.notebook.kernel.execute(command, callbacks);

      // Import necessary Python libraries in case they are not imported already.
      IPython.notebook.kernel.execute("qb_debugger='Starting imports'", callbacks)
      IPython.notebook.kernel.execute("import sys", callbacks)
      IPython.notebook.kernel.execute("import json", callbacks)
      IPython.notebook.kernel.execute("import pandas as p", callbacks)
      IPython.notebook.kernel.execute("qb_debugger='Finished imports successfully'", callbacks)

      // Convert the event's data into a dict.  It was previously loaded into Python as a string variable.  We are overwriting the string variable to be a dict.
      IPython.notebook.kernel.execute("qb_debugger='Starting to load qb_results into JSON'", callbacks)
      IPython.notebook.kernel.execute("qb_results = json.loads(qb_results)", callbacks)
      IPython.notebook.kernel.execute("qb_debugger='Successfully loaded qb_results into JSON'", callbacks)

      // Create a pandas DataFrame from the dict variable's `data` and `columns` values.
      IPython.notebook.kernel.execute("qb_debugger='Starting to load qb_results into pandas data frame'", callbacks)
      IPython.notebook.kernel.execute("df = p.DataFrame(data=qb_results['data'], columns=qb_results['columns'])", callbacks)
      IPython.notebook.kernel.execute("qb_debugger='Finished loading qb_results into pandas data frame'", callbacks)
  }
  else {
      console.error(event.origin + ' does not match ' + '${servingDomain}')
  }
};

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for(let i = 0; i <ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
}


function buildIFrameHtml() {
    let parentDiv = document.createElement('div');
    parentDiv.style = 'height: 550px';
    parentDiv.innerHTML = `
        <iframe src="${servingDomain}"
              style="position:absolute; top:0; left:0; width:100%; height:100%; border:0"
              allowtransparency="true" frameborder="0" scrolling="yes"
        >
        </iframe>
    `;

    return parentDiv;
}

// https://github.com/jcb91/notebook/blob/3251cb90fae07a32b54afa90946a082e306def6c/notebook/static/base/js/dialog.js
// https://www.stefaanlippens.net/jupyter-notebook-dialog.html
require(
  ["base/js/dialog"],
  function(dialog) {
      dialog.modal({
          body: buildIFrameHtml(),
          backdrop: false // This allows the modal to be draggable and the user to interact with the Jupyter Notebook behind the modal.
      });
  }
);