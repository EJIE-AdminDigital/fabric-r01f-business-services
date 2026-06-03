package r01f.services.api;

import r01f.model.HasAPI;

/**
 * Interface for model object extensions that contains an API 
 */
public interface HasClientAPI
		 extends HasAPI {
	/**
	 * @return the api instance
	 */
	public ClientAPI getApi();
}
