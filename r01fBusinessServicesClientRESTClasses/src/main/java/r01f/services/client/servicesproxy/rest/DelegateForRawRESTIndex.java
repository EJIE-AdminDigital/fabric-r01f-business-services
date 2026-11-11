package r01f.services.client.servicesproxy.rest;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpHeader;
import r01f.httpclient.HttpResponseResult;
import r01f.mime.MimeTypes;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.services.api.COREServiceProxyException;
import r01f.types.jobs.EnqueuedJob;
import r01f.types.url.Url;

@Slf4j
public class DelegateForRawRESTIndex
     extends DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DelegateForRawRESTIndex(final Marshaller marshaller) {
		super(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob index(final Url restResourceUrl,
						     final SecurityContext securityContext,
 							 final Object data) {
		log.trace("\t\tINDEX resource: {}",restResourceUrl);

		// [1] - Serialize params
		String securityContextXml = _marshaller.forWriting().toXml(securityContext);
		String dataXml = _marshaller.forWriting().toXml(data);

		// [2] - Do http request
		HttpResponseResult httpResponse = null;
		try {
			// index some records
			if (data != null) {
				httpResponse =  POST(restResourceUrl,
		                             MimeTypes.APPLICATION_XML,
		                             new HttpHeader("securityContext",securityContextXml),
		                             dataXml);
			}
			// index all records
			else {
				httpResponse =  POST(restResourceUrl,
                                     MimeTypes.APPLICATION_XML,
                                     new HttpHeader("securityContext",securityContextXml),
                                     null);
			}
		} catch (final Throwable ioEx) {
			throw new COREServiceProxyException(ioEx);
		}

		// [3] - De-serialize response
		EnqueuedJob outJob = this.mapHttpResponseForEnqueuedJob(securityContext,
																restResourceUrl,
																httpResponse);
		return outJob;
	}
	public EnqueuedJob updateIndex(final Url restResourceUrl,
							   	   final SecurityContext securityContext,
							   	   final Object data) {
		log.trace("\t\tINDEX resource: {}",restResourceUrl);

		// [1] - Serialize params
		String securityContextXml = _marshaller.forWriting().toXml(securityContext);
		String dataXml = _marshaller.forWriting().toXml(data);

		// [2] - Do http request
		HttpResponseResult httpResponse = null;
		try {
			// index some records
			if (data != null) {
				httpResponse =  PUT(restResourceUrl,
									 MimeTypes.APPLICATION_XML,
                                     new HttpHeader("securityContext",securityContextXml),
                                     dataXml);
			}
			// index all records
			else {
				httpResponse =  PUT(restResourceUrl,
						            MimeTypes.APPLICATION_XML,
                                    new HttpHeader("securityContext",securityContextXml),
                                    null);
			}
		} catch (final Throwable ioEx) {
			throw new COREServiceProxyException(ioEx);
		}

		// [3] - De-serialize response
		EnqueuedJob outJob = this.mapHttpResponseForEnqueuedJob(securityContext,
																restResourceUrl,
																httpResponse);
		return outJob;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UNINDEX
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob removeFromIndex(final Url restResourceUrl,
							   		   final SecurityContext securityContext,
							   		   final Object data) {
		log.trace("\t\tUN INDEX resource: {}",restResourceUrl);

		// [1] - Serialize params
		String securityContextXml = _marshaller.forWriting().toXml(securityContext);
		String dataXml = data != null ? _marshaller.forWriting().toXml(data)
									  : null;

		// [2] - Do http request
		HttpResponseResult httpResponse = null;
		try {
			// index some records
			if (data != null) {
				httpResponse =  DELETE(restResourceUrl,
						               MimeTypes.APPLICATION_XML,
                                       new HttpHeader("securityContext",securityContextXml),
                                       dataXml, null);
			}
			// index all records
			else {
				httpResponse =  DELETE(restResourceUrl,
			                           MimeTypes.APPLICATION_XML,
                                       new HttpHeader("securityContext",securityContextXml),null,null);
			}
		} catch (final Throwable ioEx) {
			throw new COREServiceProxyException(ioEx);
		}

		// [3] - De-serialize response
		EnqueuedJob outJob = this.mapHttpResponseForEnqueuedJob(securityContext,
																restResourceUrl,
																httpResponse);
		return outJob;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob mapHttpResponseForEnqueuedJob(final SecurityContext securityContext,
													 final Url restResourceUrl,
													 final HttpResponseResult httpResponse) {
		EnqueuedJob outJob = this.getResponseToResultMapper()
										.mapHttpResponse(securityContext,
												  		 restResourceUrl,
												  		 httpResponse,
												  		 EnqueuedJob.class);
		return outJob;
	}
}
