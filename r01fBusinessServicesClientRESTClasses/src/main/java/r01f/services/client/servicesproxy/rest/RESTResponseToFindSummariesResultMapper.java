package r01f.services.client.servicesproxy.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.common.reflect.TypeToken;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.generics.ParameterizedTypeImpl;
import r01f.httpclient.HttpResponseResult;
import r01f.io.util.StringPersistenceUtils;
import r01f.mime.MimeType;
import r01f.mime.MimeTypes;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.model.persistence.FindSummariesError;
import r01f.model.persistence.FindSummariesOK;
import r01f.model.persistence.FindSummariesResult;
import r01f.model.persistence.FindSummariesResultBuilder;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.services.api.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Strings;
@Slf4j
public class RESTResponseToFindSummariesResultMapper<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Marshaller _marshaller;
	private final Class<M> _modelObjectType;
	@Getter protected final MimeType _mimeType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToFindSummariesResultMapper(final Marshaller marshaller,
										  		   final Class<M> modelObjectType) {
		this(marshaller,modelObjectType,MimeTypes.APPLICATION_XML);
	}

	public RESTResponseToFindSummariesResultMapper(final Marshaller marshaller,
												   final Class<M> modelObjectType,
												   final MimeType mimeType) {
		_marshaller = marshaller;
		_modelObjectType = modelObjectType;
		_mimeType = mimeType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public FindSummariesResult<M> mapHttpResponseForSummaries(final SecurityContext securityContext,
													 		  final Url restResourceUrl,final HttpResponseResult httpResponse) {
		FindSummariesResult<M> outOperationResult = null;
		if (httpResponse.isSuccess()) {
			outOperationResult = _mapHttpResponseForSuccessFindingSummaries(securityContext,
																	   	   restResourceUrl,httpResponse);
		} else {
			outOperationResult = _mapHttpResponseForErrorFindigSummaries(securityContext,
														  				restResourceUrl,httpResponse);
		}
		return outOperationResult;
	}
	@SuppressWarnings({ "rawtypes","unchecked" })
	protected FindSummariesOK<M> _mapHttpResponseForSuccessFindingSummaries(final SecurityContext securityContext,
												   	   			   			final Url restResourceUrl,final HttpResponseResult httpResponse) {
		FindSummariesOK<M> outOperationResult = null;

		// [0] - Load the response
		String responseStr = null;
		try {
			responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
		} catch ( final IOException e) {
			e.printStackTrace();
		}
		log.warn("..\n Received response from remote service !!! :, {}", responseStr);
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));

		// [1] - Determine concrete class to marshall
		/*	 Marshaller tries to marshall "M" just a PersistableObject Interface and NOT as the concrete class... so crashes...
		   outOperationResult = _marshaller.forReading().fromJson(responseStr,
													  new TypeToken<FindSummariesOK<M>>() { /* nothing */
		/*	... temporary use this dirty trick to get concrete class to marhaller , so the CRUD will JUST for modelTypes
		 *  		 (TypeToken<CRUDOK<M>>) TypeToken.of( new ParameterizedTypeImpl(CRUDOK.class, modelTypes, null));
		 */
		Type[] modelTypes = { _modelObjectType };
		TypeToken<FindSummariesOK<M>> typeToken =  (TypeToken<FindSummariesOK<M>>)TypeToken.of(new ParameterizedTypeImpl(FindSummariesOK.class,
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

		if (((FindSummariesResult)outOperationResult).getOrThrow() == null) {
			outOperationResult.setMethodExecResult(new ArrayList<SummarizedModelObject<M>>());	// ensure an empty array list for no results
		}

		// [3] - Return
		return outOperationResult;
	}
	protected FindSummariesError<M> _mapHttpResponseForErrorFindigSummaries(final SecurityContext securityContext,
												    			   			final Url restResourceUrl,final HttpResponseResult httpResponse) {
		FindSummariesError<M> outOpError = null;

		// [0] - Load the http response text
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
			outOpError = FindSummariesResultBuilder.using(securityContext)
												   .on(_modelObjectType)
												   .errorFindingSummaries()
												  	  		.causedBy(responseStr);
		}
		// [2] - Error while request processing: the PersistenceCRUDError comes INSIDE the response
		else {
			outOpError = FindSummariesResultBuilder.using(securityContext)
												   .on(_modelObjectType)
												   .errorFindingSummaries()
												  	  		.causedBy(responseStr);
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
}
