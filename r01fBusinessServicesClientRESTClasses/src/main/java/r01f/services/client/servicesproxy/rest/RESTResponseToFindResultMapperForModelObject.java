package r01f.services.client.servicesproxy.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.common.reflect.TypeToken;

import r01f.exceptions.Throwables;
import r01f.generics.ParameterizedTypeImpl;
import r01f.httpclient.HttpResponseResult;
import r01f.io.util.StringPersistenceUtils;
import r01f.mime.MimeType;
import r01f.mime.MimeTypes;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.FindError;
import r01f.model.persistence.FindOK;
import r01f.model.persistence.FindResult;
import r01f.model.persistence.FindResultBuilder;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.services.api.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Strings;

public class RESTResponseToFindResultMapperForModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	implements RESTResponseToFindResultMapper<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Marshaller _marshaller;
	private final Class<M> _modelObjectType;
    protected final MimeType _mimeType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToFindResultMapperForModelObject(final Marshaller marshaller,
										                final Class<M> modelObjectType) {
		this(marshaller,modelObjectType,MimeTypes.APPLICATION_XML);
	}
	public RESTResponseToFindResultMapperForModelObject(final Marshaller marshaller,
			                                            final Class<M> modelObjectType,
			                                            final MimeType mimeType) {
		_marshaller = marshaller;
		_modelObjectType = modelObjectType;
		_mimeType = mimeType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindResult<M> mapHttpResponseForEntities(final SecurityContext securityContext,
													final Url restResourceUrl,final HttpResponseResult httpResponse) {
		FindResult<M> outOperationResult = null;
		if (httpResponse.isSuccess()) {
			outOperationResult = _mapHttpResponseForSuccessFindingEntities(securityContext,
																	   	   restResourceUrl,httpResponse);
		} else {
			outOperationResult = _mapHttpResponseForErrorFindigEntities(securityContext,
														  				restResourceUrl,httpResponse);
		}
		return outOperationResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// PROTECTED METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unused","unchecked" })
	protected FindOK<M> _mapHttpResponseForSuccessFindingEntities(final SecurityContext securityContext,
												   	   			  final Url restResourceUrl,final HttpResponseResult httpResponse) {
		FindOK<M> outOperationResult = null;

		// [0] - Load the response
		String responseStr = null;
		try {
			responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
		} catch ( final IOException e) {
			e.printStackTrace();
		}
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));

		// [1] --[ Map the response]
		/*	 Marshaller tries to marshall "M" just a PersistableObject Interface and NOT as the concrete class... so crashes...
			   outOperationResult = _marshaller.forReading().fromJson(responseStr,
														  new TypeToken<FindOK<M>>() { /* nothing */
		/*	... Use this dirty trick to get concrete class to marshall , so the CRUD will JUST for modelTypes
		 *  		 (TypeToken<FindOK<M>>) TypeToken.of( new ParameterizedTypeImpl(FindOK.class, modelTypes, null));
		 */
		Type[] modelTypes = { _modelObjectType };
		TypeToken<FindOK<M>> typeToken =  (TypeToken<FindOK<M>>)TypeToken.of(new ParameterizedTypeImpl(FindOK.class,
																									   modelTypes,
																									   null));			// owner type

		// [2] - Map the response
		if (_mimeType.is(MimeTypes.APPLICATION_XML)) {
			outOperationResult = _marshaller.forReading().fromXml(responseStr,typeToken);


		} else if (_mimeType.is(MimeTypes.APPLICATION_JSON)) {
			outOperationResult = _marshaller.forReading().fromJson(responseStr,typeToken);

		} else {
			throw new IllegalArgumentException(Strings.customized("{} mimeType not suported",
																  _mimeType)) ;
		}
		if (outOperationResult == null) {
			throw new COREServiceProxyException(Throwables.message("The REST service {}"
																	+ " worked BUT RESPONSE:  {}"
																	+ "  ....CANNOT BE PARSED to a  FindResult object for {}" ,
																			restResourceUrl, responseStr , _modelObjectType));
		}

		if (outOperationResult.getOrThrow() == null) outOperationResult.setMethodExecResult(new ArrayList<M>());	// ensure an empty array list for no results




		// [2] - Return
		return outOperationResult;
	}
	protected FindError<M> _mapHttpResponseForErrorFindigEntities(final SecurityContext securityContext,
												    			  final Url restResourceUrl,final HttpResponseResult httpResponse) {
		FindError<M> outOpError = null;

		// [0] - Load the http response text
		// [0] - Load the response
		String responseStr = null;
		try {
			responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
		} catch ( final IOException e) {
			e.printStackTrace();
		}
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));

		// [1] - Server error (the request could NOT be processed)
		if (httpResponse.isServerError()) {
			outOpError = FindResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .errorFindingEntities()
										  	  		.causedBy(responseStr);
		}
		// [2] - Error while request processing: the FindError comes INSIDE the response
		else {
			outOpError = FindResultBuilder.using(securityContext)
										  .on(_modelObjectType)
										  .errorFindingEntities()
										  	  		.causedBy(responseStr);
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}

}
