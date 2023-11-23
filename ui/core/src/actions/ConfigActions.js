import {store} from "../index";

export const getRules = async () => {
	const apiUrl = `${store.getState().config.baseApiUrl}/rules`;
	let response = await fetch(apiUrl);
	let json = await response.json();

	return json;
};