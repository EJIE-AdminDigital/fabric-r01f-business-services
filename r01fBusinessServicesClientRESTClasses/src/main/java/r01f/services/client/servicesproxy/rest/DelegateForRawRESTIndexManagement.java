package r01f.services.client.servicesproxy.rest;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.httpclient.HttpHeader;
import r01f.httpclient.HttpResponseResult;
import r01f.mime.MimeTypes;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.services.api.COREServiceProxyException;
import r01f.services.core.persistence.index.IndexManagementCommand;
import r01f.types.jobs.EnqueuedJob;
import r01f.types.url.Url;
import r01f.util.types.Strings;

@Slf4j
public class DelegateForRawRESTIndexManagement
     extends DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DelegateForRawRESTIndexManagement(final Marshaller marshaller) {
		super(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob doIndexManagementCommand(final Url restResourceUrl,
							   	  				final SecurityContext securityContext,
							   	  				final IndexManagementCommand indexCmd) {
		log.warn("\t\tINDEX resource: {}",restResourceUrl);

		// [1] - Serialize params
		String securityContextXml = securityContext != null ? _marshaller.forWriting().toXml(securityContext) : null;
		String dataXml = _marshaller.forWriting().toXml(indexCmd);

		// [2] - Do http request
		HttpResponseResult httpResponse = null;
		try {
			// index some records
			if (indexCmd != null) {
				if (Strings.isNOTNullOrEmpty(securityContextXml)) {
					httpResponse = PUT(restResourceUrl,
					                   MimeTypes.APPLICATION_XML,
					                   new HttpHeader("securityContext",securityContextXml),
					                   dataXml);
				} else {
					httpResponse = PUT(restResourceUrl,
					                   MimeTypes.APPLICATION_XML,
					                   new HttpHeader("securityContext",null),
					                   dataXml);
				}
			}
			else {
				throw new IllegalArgumentException(Throwables.message("The index resource {} is NOT valid",restResourceUrl));
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
