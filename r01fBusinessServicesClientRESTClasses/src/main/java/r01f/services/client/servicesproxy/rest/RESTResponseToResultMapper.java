package r01f.services.client.servicesproxy.rest;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.httpclient.HttpResponseResult;
import r01f.io.util.StringPersistenceUtils;
import r01f.model.persistence.PersistenceException;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.services.api.COREServiceProxyException;
import r01f.types.url.Url;
import r01f.util.types.Strings;

@Slf4j
public class RESTResponseToResultMapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Marshaller _marshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToResultMapper(final Marshaller marshaller) {
		_marshaller = marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps the entity contained in the {@link HttpResponseResult}
	 * @param securityContext
	 * @param restResourceUrl
	 * @param httpResponse
	 * @param Class<T> expectedType
	 * @return
	 * @throws PersistenceException
	 */
	public <T> T mapHttpResponse(final SecurityContext securityContext,
								 final Url restResourceUrl,
								 final HttpResponseResult httpResponse,
								 final Class<T> expectedType) {
		T outObj = null;
		// [SUCCESS] ---
		if (httpResponse.isSuccess()) {
			String responseStr = null;
			try {
				responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
			} catch ( final IOException e) {
				e.printStackTrace();
			}
			log.trace("\t\tREST Response body: {}",responseStr);

			if (Strings.isNOTNullOrEmpty(responseStr)) {
				// Check the received type if both a header and an expected type are provided
				String receivedModelObjTypeStr = httpResponse.getSingleValuedHeaderAsString("x-r01-modelObjType");
				if (Strings.isNOTNullOrEmpty(receivedModelObjTypeStr) && expectedType != null) {
					Class<?> receivedType = ReflectionUtils.typeFromClassName(receivedModelObjTypeStr);
					if (!receivedType.equals(expectedType)) throw new ClassCastException(Throwables.message("The client REST proxy received type ({}) is NOT the expected one {}",
																										 	receivedType,expectedType));
				}

				// De-serialize
				outObj = _marshaller.forReading().fromXml(responseStr,
														  expectedType);
			} else {
				throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake!",
																   restResourceUrl));
			}
		}
		// [ERROR] ---
		else {
			String errorMsg = null;
			try {
				errorMsg = StringPersistenceUtils.load(httpResponse.loadAsStream());
			} catch ( final IOException e) {
				e.printStackTrace();
			}
			int errorCode = httpResponse.getCodeNumber();

			log.error("The REST resource {} returned an error code {} with message {}",
					  restResourceUrl,errorCode,errorMsg);
			throw new COREServiceProxyException(errorMsg);
		}
		return outObj;
	}
}
