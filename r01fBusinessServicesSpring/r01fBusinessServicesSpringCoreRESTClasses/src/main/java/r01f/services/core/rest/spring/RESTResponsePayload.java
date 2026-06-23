package r01f.services.core.rest.spring;

import r01f.model.ModelObject;

/**
 * 	A facet to wrap the response payload either as a modelo object or as crud-result
 * [Warning! ]  This object will not be marshalled, insteaf of this, the object to marshall wil be the modelo object or the crud resul
 */
public interface RESTResponsePayload 
	extends ModelObject {
/////////////////////////////////////////////////////////////////////////////////////////
// METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public default boolean forModelObject() {
		return this instanceof RESTResponseOnModelObject;
	}
	
	public default boolean forCRUDResult() {
		return this instanceof RESTResponseOnCRUDResult;
	}
}
