import { store } from "../index";

export async function getAvailableSubQueries() {
	// A database must be selected first before getting available sub queries/query templates.
	if (store.getState().query.selectedDatabase === null) {
		return;
	}

	let availableSubQueryNames = Object.keys(store.getState().query.availableSubQueries);
	let databaseName = store.getState().query.selectedDatabase.databaseName;
	if (availableSubQueryNames.length === 0) {
		let apiUrl = `${store.getState().config.baseApiUrl}/query-template?databaseName=${databaseName}`;
        await fetch(apiUrl)
        	.then(response => {
        		if (! response.ok) {
                    throw Error('Could not retrieve databases')
                }

                return response.json()
        	}).then(queryTemplateNames => {
                console.log(queryTemplateNames);

                let subQueriesObject = {}
                queryTemplateNames.forEach(queryTemplateName => {
                	subQueriesObject[queryTemplateName] = {
                		versions: {} // Metadata should be the value of each version.
                	};
                })


                store.dispatch({
	    			type: 'UPDATE_AVAIALABLE_SUBQUERIES',
	    			payload: {
	    				availableSubQueries: subQueriesObject
	    			}
	    		});
            }).catch(reason => {
                console.log(reason);
            });
	}
}

export async function updateSubQuery(subQueryId, attributeName, value) {
	let newSubQueries = [...store.getState().query.subQueries];

	newSubQueries.forEach(subQuery => {
		if (subQuery.id === subQueryId) {
			subQuery[attributeName] = value;
		}
	});

	return {
		type: 'UPDATE_SUBQUERIES',
		payload: {
			subQueries: newSubQueries
		}
	};
};

export async function updateVersions(subQueryId) {
	let newSubQueries = [...store.getState().query.subQueries];

	newSubQueries.forEach(subQuery => {
		// If the sub query has no versions, then get them from the API.  This is a blocking operation.
		if (subQuery.versions.length === 0) {
			let apiUrl = `${store.getState().config.baseApiUrl}/query-template/${subQuery.queryTemplateName}/versions`;
	        
	        fetch(apiUrl)
	            .then(response => response.json())
	            .then(versions => {
	                console.log(versions);
	                subQuery.versions = versions;
	            });
		}
	});

	return {
		type: 'UPDATE_SUBQUERIES',
		payload: {
			subQueries: newSubQueries
		}
	}

}