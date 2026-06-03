package r01f.services.client.servicesproxy.rest;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpHeader;
import r01f.httpclient.HttpResponseResult;
import r01f.mime.MimeType;
import r01f.mime.MimeTypes;
import r01f.objectstreamer.Marshaller;
import r01f.services.api.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

@Slf4j
public abstract class DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Objects to xml/json marshaller
	 */
	protected final Marshaller _marshaller;
	/**
	 * Maps from the REST Response to the returned object
	 */
	protected final RESTResponseToResultMapper _responseToResultMapper;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DelegateForRawREST(final Marshaller marshaller) {
		_marshaller = marshaller;
		_responseToResultMapper = new RESTResponseToResultMapper(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected RESTResponseToResultMapper getResponseToResultMapper() {
		return _responseToResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	HEAD
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponseResult HEAD(final Url restResourceUrl,
										  final String securityContextXml,
										  final HttpHeader... otherHeaders) {
		return HEAD(restResourceUrl, new HttpHeader("securityContext",securityContextXml),Lists.newArrayList(otherHeaders));
	}
	public static HttpResponseResult HEAD(final Url restResourceUrl,
								          final HttpHeader securityHeader,
								          final Collection<HttpHeader> otherHeaders) {
		log.warn("\t\tHEAD resource: {}",restResourceUrl);
		HttpResponseResult outHttpResponse = null;
		try {

			HttpRequest request = null;
			if ( CollectionUtils.hasData(otherHeaders)) {
				request =  HttpRequest.newBuilder()
											.uri(restResourceUrl.asUri())
											.header(securityHeader.getId().asString(),
													securityHeader.getValue())
											.method("head", BodyPublishers.noBody())
									  .build();
			} else {
				request =  HttpRequest.newBuilder()
											.uri(restResourceUrl.asUri())
											.header(securityHeader.getId().asString(),
													securityHeader.getValue())
											// .headers("X-Our-Header-1", "value1".... "X-Our-Header-N", "valueN")
											.headers(otherHeaders
													    .stream()
													    .<String>flatMap(model -> Stream.of(model.getId().asString(),model.getValue()))
													    .collect(Collectors.toList()).toArray(String[]::new))
											.method("head", BodyPublishers.noBody())
									.build();
			}
			HttpResponse<InputStream> response = HttpClient.newBuilder()
														   .build()
														   .send(request,
					                                             HttpResponse.BodyHandlers.ofInputStream());

			outHttpResponse = new HttpResponseResult(response);
		} catch (final Throwable ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while HEADing {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GET
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponseResult GET(final Url restResourceUrl,
				                         final String securityContextXml) {
			return GET(restResourceUrl, new HttpHeader("securityContext",securityContextXml),Lists.newArrayList());
	}
	public static HttpResponseResult GET(final Url restResourceUrl,
										 final String securityContextXml, final HttpHeader...headers) {
		return GET(restResourceUrl, new HttpHeader("securityContext",securityContextXml),Lists.newArrayList(headers));
	}
	public static HttpResponseResult GET(final Url restResourceUrl,
			                             final HttpHeader securityHeader, final HttpHeader...headers) {
		return GET(restResourceUrl, securityHeader,Lists.newArrayList(headers));
	}
	public static HttpResponseResult GET(final Url restResourceUrl,
								         final HttpHeader securityHeader,
								         final Collection<HttpHeader> otherHeaders) {
		log.warn("\t\tGET resource: {}",restResourceUrl);
		HttpResponseResult outHttpResponse = null;
		try {
			HttpRequest request = null;
			if ( CollectionUtils.hasData(otherHeaders)) {
				request =  HttpRequest.newBuilder()
											.uri(restResourceUrl.asUri())
											.header(securityHeader.getId().asString(),
													securityHeader.getValue())
											.GET()
									  .build();
			} else {
				request =  HttpRequest.newBuilder()
										.uri(restResourceUrl.asUri())
										.header(securityHeader.getId().asString(),
												securityHeader.getValue())
										// .headers("X-Our-Header-1", "value1".... "X-Our-Header-N", "valueN")
										.headers(otherHeaders.stream()
														     .<String>flatMap(model -> Stream.of(model.getId().asString(),model.getValue()))
														     .collect(Collectors.toList()).toArray(String[]::new))
										.GET()
									.build();
			}
			HttpResponse<InputStream> response = HttpClient.newBuilder()
														   .build()
														   .send(request,
					                                             HttpResponse.BodyHandlers.ofInputStream());

			outHttpResponse = new HttpResponseResult(response);

		} catch (final Throwable ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while GETing {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	POST
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponseResult POST(final Url restResourceUrl,
									      final String securityContextXml,
							     	      final String entityXml,
							     	      final HttpHeader... headers) {
		return POST(restResourceUrl,
				    MimeTypes.APPLICATION_XML,
				    new HttpHeader("securityContext",securityContextXml),
				    entityXml,
				    Lists.newArrayList(headers));
	}
	public static HttpResponseResult POST(final Url restResourceUrl,
            							  final MimeType mimeType,
            							  final HttpHeader securityHeader,
            							  final String entityAsString,
            							  final HttpHeader... otherHeaders) {
		return POST(restResourceUrl,
				    MimeTypes.APPLICATION_XML,
				    securityHeader,
				    entityAsString,
				    Lists.newArrayList(otherHeaders));
	}
	public static HttpResponseResult POST(final Url restResourceUrl,
					                      final MimeType mimeType,
										  final HttpHeader securityHeader,
									      final String entityAsString,
									      final Collection<HttpHeader> otherHeaders) {
		log.warn("\t\tPOST resource: {}",restResourceUrl);
		HttpResponseResult outHttpResponse = null;
		try {
			HttpRequest request = null;
			if ( CollectionUtils.hasData(otherHeaders)) {
				 request = HttpRequest.newBuilder()
										  .uri(restResourceUrl.asUri())
										  .header("Content-Type", mimeType.getName())
										  .header(securityHeader.getId().asString(),
												  securityHeader.getValue())
										  .headers(otherHeaders //headers("X-Our-Header-1", "value1".... "X-Our-Header-N", "valueN")
													    .stream()
													    .<String>flatMap(model -> Stream.of(model.getId().asString(),model.getValue()))
													    .collect(Collectors.toList()).toArray(String[]::new))
										  .POST(Strings.isNOTNullOrEmpty(entityAsString)? HttpRequest.BodyPublishers.ofString(entityAsString)
												                                        : HttpRequest.BodyPublishers.noBody())
									  .build();

			} else {
				 request = HttpRequest.newBuilder()
										  .uri(restResourceUrl.asUri())
										  .header("Content-Type", mimeType.getName())
										  .header(securityHeader.getId().asString(),
												  securityHeader.getValue())
										  .POST(Strings.isNOTNullOrEmpty(entityAsString)? HttpRequest.BodyPublishers.ofString(entityAsString)
												                                        : HttpRequest.BodyPublishers.noBody())
										 .build();
			}
			HttpResponse<InputStream> response = HttpClient.newBuilder()
														   .build()
														   .send(request,
									                             HttpResponse.BodyHandlers.ofInputStream());
			outHttpResponse = new HttpResponseResult(response);

		} catch (final Throwable ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while POSTing to {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PUT
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponseResult PUT(final Url restResourceUrl,
										 final String securityContextXml,
					 			  	   	 final String entityXml,
					 			  	   	 final HttpHeader... headers) {
		return PUT(restResourceUrl,
				    MimeTypes.APPLICATION_XML,
				    new HttpHeader("securityContext",securityContextXml),
				    entityXml,
				    Lists.newArrayList(headers));

	}

	public static HttpResponseResult PUT(final Url restResourceUrl,
										 final MimeType mimeType,
										 final HttpHeader securityHeader,
										 final String entityAsString,
										 final HttpHeader... otherHeaders) {
		return PUT(restResourceUrl,
				   MimeTypes.APPLICATION_XML,
				   securityHeader,
				   entityAsString,
				   Lists.newArrayList(otherHeaders));
	}
	public static HttpResponseResult PUT(final Url restResourceUrl,
										 final MimeType mimeType,
										 final HttpHeader securityHeader,
						 			  	 final String entityAsString,
						 			  	 final Collection<HttpHeader> otherHeaders) {
		log.trace("\t\tPUT resource: {}",restResourceUrl);
		HttpResponseResult outHttpResponse = null;
		try {
			HttpRequest request = null;
			if ( CollectionUtils.hasData(otherHeaders)) {
				 request = HttpRequest.newBuilder()
										  .uri(restResourceUrl.asUri())
										  .header("Content-Type", mimeType.getName())
										  .header(securityHeader.getId().asString(),
												  securityHeader.getValue())
										  .headers(otherHeaders //headers("X-Our-Header-1", "value1".... "X-Our-Header-N", "valueN")
													    .stream()
													    .<String>flatMap(model -> Stream.of(model.getId().asString(),model.getValue()))
													    .collect(Collectors.toList()).toArray(String[]::new))
										  .PUT(Strings.isNOTNullOrEmpty(entityAsString)? HttpRequest.BodyPublishers.ofString(entityAsString)
												                                       : HttpRequest.BodyPublishers.noBody())
									  .build();

			} else {
				 request = HttpRequest.newBuilder()
										  .uri(restResourceUrl.asUri())
										  .header("Content-Type", mimeType.getName())
										  .header(securityHeader.getId().asString(),
												  securityHeader.getValue())
										  .PUT(Strings.isNOTNullOrEmpty(entityAsString)? HttpRequest.BodyPublishers.ofString(entityAsString)
												                                        : HttpRequest.BodyPublishers.noBody())
									  .build();
			}
			HttpResponse<InputStream> response = HttpClient.newBuilder()
														   .build()
														   .send(request,
									                             HttpResponse.BodyHandlers.ofInputStream());

			outHttpResponse = new HttpResponseResult(response);


		} catch (final Throwable ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while POSTing to {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;

	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpResponseResult DELETE(final Url restResourceUrl,
									        final String securityContextXml,
									        final HttpHeader... otherHeaders) {
		return DELETE(restResourceUrl,
				      new HttpHeader("securityContext",securityContextXml),
				      Lists.newArrayList(otherHeaders));
	}

	public static HttpResponseResult DELETE(final Url restResourceUrl,
	        								final HttpHeader securityHeader,
	        								final HttpHeader... otherHeaders) {
		return DELETE(restResourceUrl,
				      securityHeader,
			          Lists.newArrayList(otherHeaders));
	}

	public static HttpResponseResult DELETE(final Url restResourceUrl,
									        final HttpHeader securityHeader,
									        final Collection<HttpHeader> otherHeaders) {
		HttpResponseResult outHttpResponse = null;
		try {

			HttpRequest request = null;
			if ( CollectionUtils.hasData(otherHeaders)) {
				request =  HttpRequest.newBuilder()
											.uri(restResourceUrl.asUri())
											.header(securityHeader.getId().asString(), securityHeader.getValue())
											.DELETE()
									  .build();
			} else {
				request =  HttpRequest.newBuilder()
										.uri(restResourceUrl.asUri())
										.header(securityHeader.getId().asString(),
												securityHeader.getValue())
										// .headers("X-Our-Header-1", "value1".... "X-Our-Header-N", "valueN")
										.headers(otherHeaders
												    .stream()
												    .<String>flatMap(model -> Stream.of(model.getId().asString(),
												    		                            model.getValue()))
												    .collect(Collectors.toList()).toArray(String[]::new))
										.DELETE()
									.build();
			}
			HttpResponse<InputStream> response = HttpClient.newBuilder()
														   .build()
														   .send(request,
					                                             HttpResponse.BodyHandlers.ofInputStream());

			outHttpResponse = new HttpResponseResult(response);
		} catch (final Throwable ioEx) {
			ioEx.getLocalizedMessage();
			log.error("Error while HEADing {}: {}",restResourceUrl,ioEx.getMessage());
			throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}

	public static HttpResponseResult DELETE(final Url restResourceUrl,
										    final MimeType mimeType,
											final HttpHeader securityHeader,
										  	final String entityAsString,
										    final Collection<HttpHeader> otherHeaders) {
		log.trace("\t\tDELETE With Payload resource: {}",restResourceUrl);
		HttpResponseResult outHttpResponse = null;
		try {
		HttpRequest request = null;
		if ( CollectionUtils.hasData(otherHeaders)) {
		request = HttpRequest.newBuilder()
					  .uri(restResourceUrl.asUri())
					  .header("Content-Type", mimeType.getName())
					  .header(securityHeader.getId().asString(),
							  securityHeader.getValue())
					  .headers(otherHeaders //headers("X-Our-Header-1", "value1".... "X-Our-Header-N", "valueN")
								    .stream()
								    .<String>flatMap(model -> Stream.of(model.getId().asString(),
								    		                            model.getValue()))
								    .collect(Collectors.toList()).toArray(String[]::new))
					  .method("delete",Strings.isNOTNullOrEmpty(entityAsString)? HttpRequest.BodyPublishers.ofString(entityAsString)
                                                                              : HttpRequest.BodyPublishers.noBody())
				  .build();

		} else {
			request = HttpRequest.newBuilder()
								  .uri(restResourceUrl.asUri())
								  .header("Content-Type", mimeType.getName())
								  .header(securityHeader.getId().asString(),
										  securityHeader.getValue())
								  .method("delete",Strings.isNOTNullOrEmpty(entityAsString)? HttpRequest.BodyPublishers.ofString(entityAsString)
										                                        : HttpRequest.BodyPublishers.noBody())
								 .build();
			}
			HttpResponse<InputStream> response = HttpClient.newBuilder()
										   .build()
										   .send(request,
					                             HttpResponse.BodyHandlers.ofInputStream());

			outHttpResponse = new HttpResponseResult(response);


		} catch (final Throwable ioEx) {
			ioEx.getLocalizedMessage();
				log.error("Error while POSTing to {}: {}",restResourceUrl,ioEx.getMessage());
				throw new COREServiceProxyException(ioEx);
		}
		return outHttpResponse;
	}


}
