package r01f.services.client.servicesproxy.rest;

import lombok.experimental.Accessors;
import r01f.guids.PersistableObjectOID;
import r01f.httpclient.HttpResponseResult;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.FindResult;
import r01f.securitycontext.SecurityContext;
import r01f.types.url.Url;


@Accessors(prefix="_")
public interface RESTResponseToFindResultMapper<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
// METHODS TO IMPLEMENT
/////////////////////////////////////////////////////////////////////////////////////////

	public FindResult<M> mapHttpResponseForEntities(final SecurityContext securityContext,
											        final Url restResourceUrl,final HttpResponseResult httpResponse) ;

}
