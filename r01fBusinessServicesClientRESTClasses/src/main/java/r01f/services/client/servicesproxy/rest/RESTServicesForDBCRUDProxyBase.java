package r01f.services.client.servicesproxy.rest;

import java.io.IOException;
import java.time.Instant;

import com.google.common.reflect.TypeToken;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.httpclient.HttpResponseResult;
import r01f.io.util.StringPersistenceUtils;
import r01f.mime.MimeType;
import r01f.mime.MimeTypes;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.services.api.COREServiceProxyException;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilderForModelObjectPersistence;
import r01f.services.core.callback.spec.COREServiceMethodCallbackSpec;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.datetime.DateTimeMarshaller;

@Accessors(prefix="_")
@Slf4j
public abstract class RESTServicesForDBCRUDProxyBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
              extends RESTServicesForModelObjectProxyBase<O,M>
           implements CRUDServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps a REST response to a CRUDResult
	 */
	protected final RESTResponseToCRUDResultMapperForModelObject<O,M> _responseToCRUDResultMapper;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public <P extends RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O>>
		   RESTServicesForDBCRUDProxyBase(final Marshaller marshaller,
									 	  final Class<M> modelObjectType,
									 	  final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_responseToCRUDResultMapper = new RESTResponseToCRUDResultMapperForModelObject<O,M>(marshaller,modelObjectType);
	}
	protected <P extends RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O>>
			  RESTServicesForDBCRUDProxyBase(final Marshaller marshaller,
									    	 final Class<M> modelObjectType,
									    	 final P servicesRESTResourceUrlPathBuilder,
									    	 final RESTResponseToCRUDResultMapperForModelObject<O,M> responseToCrudResultMapper) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_responseToCRUDResultMapper = responseToCrudResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected RESTResponseToCRUDResultMapperForModelObject<O,M> getResponseToCRUDResultMapperForModelObject() {
		return _responseToCRUDResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXISTS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings({ "unchecked","serial" })
	public PersistenceOperationResult<Boolean> exists(final SecurityContext securityContext,
													  final O oid) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntity(oid)
													 .joinedWith("lastUpdateDate"));
		String ctxXml = _marshaller.forWriting().toXml(securityContext);
		HttpResponseResult httpResponse = DelegateForRawREST.HEAD(restResourceUrl,
										 				    ctxXml);
		// [0] - Load the response
		String responseStr = null;
		try {
			responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
		} catch ( final IOException e) {
			e.printStackTrace();
		}
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															  	   									   restResourceUrl));

		MimeType mimeType = this.getResponseToCRUDResultMapperForModelObject()
								.getMimeType();
		TypeToken<PersistenceOperationResult<Boolean>> typeToken = new TypeToken<PersistenceOperationResult<Boolean>>() { /* nothing */ };

		// [1] - Map the response
		PersistenceOperationResult<Boolean> outResult = null;
		if (mimeType.is(MimeTypes.APPLICATION_XML)) {
			outResult = _marshaller.forReading()
								   .fromXml(responseStr,typeToken);
		} else if (mimeType.is(MimeTypes.APPLICATION_JSON)) {
			outResult = _marshaller.forReading()
								   .fromJson(responseStr,typeToken);
		} else {
			throw new IllegalArgumentException(Strings.customized("{} mimeType not suported",mimeType)) ;
		}
		// return
		return outResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	LAST UPDATE & TOUCH
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings({ "unchecked","serial" })
	public PersistenceOperationResult<Instant> getLastUpdateDate(final SecurityContext securityContext,
															  	 final O oid) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
															.pathOfEntity(oid)
															.joinedWith("lastUpdateDate"));
		String ctxXml = _marshaller.forWriting().toXml(securityContext);
		HttpResponseResult httpResponse = DelegateForRawREST.GET(restResourceUrl,
										 				   ctxXml);
		// [0] - Load the response
		String responseStr = null;
		try {
			responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
		} catch ( final IOException e) {
			e.printStackTrace();
		}
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															  	   									   restResourceUrl));

		MimeType mimeType = this.getResponseToCRUDResultMapperForModelObject()
								.getMimeType();
		TypeToken<PersistenceOperationResult<Instant>> typeToken = new TypeToken<PersistenceOperationResult<Instant>>() { /* nothing */ };

		// [1] - Map the response
		PersistenceOperationResult<Instant> outResult = null;
		if (mimeType.is(MimeTypes.APPLICATION_XML)) {
			outResult = _marshaller.forReading()
								   .fromXml(responseStr,typeToken);
		} else if (mimeType.is(MimeTypes.APPLICATION_JSON)) {
			outResult = _marshaller.forReading()
								   .fromJson(responseStr,typeToken);
		} else {
			throw new IllegalArgumentException(Strings.customized("{} mimeType not suported",mimeType)) ;
		}
		// return
		return outResult;
	}
	@Override @SuppressWarnings({ "unchecked","serial" })
	public PersistenceOperationResult<Boolean> touch(final SecurityContext securityContext,
													 final O oid,final Instant date) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
															.pathOfEntity(oid)
															.joinedWith("lastUpdateDate"));
		String ctxXml = _marshaller.forWriting().toXml(securityContext);
		HttpResponseResult httpResponse = DelegateForRawREST.POST(restResourceUrl,
										 				    	  ctxXml,
										 				    	  DateTimeMarshaller.forMarshalling(date)
										 				    				  		.asEPOCHMillisString());
		// [0] - Load the response
		String responseStr = null;
		try {
			responseStr = StringPersistenceUtils.load(httpResponse.loadAsStream());
		} catch ( final IOException e) {
			e.printStackTrace();
		}
		if (Strings.isNullOrEmpty(responseStr)) throw new COREServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															  	   									   restResourceUrl));

		MimeType mimeType = this.getResponseToCRUDResultMapperForModelObject()
								.getMimeType();
		TypeToken<PersistenceOperationResult<Boolean>> typeToken = new TypeToken<PersistenceOperationResult<Boolean>>() { /* nothing */ };

		// [1] - Map the response
		PersistenceOperationResult<Boolean> outResult = null;
		if (mimeType.is(MimeTypes.APPLICATION_XML)) {
			outResult = _marshaller.forReading()
								   .fromXml(responseStr,typeToken);
		} else if (mimeType.is(MimeTypes.APPLICATION_JSON)) {
			outResult = _marshaller.forReading()
								   .fromJson(responseStr,typeToken);
		} else {
			throw new IllegalArgumentException(Strings.customized("{} mimeType not suported",mimeType)) ;
		}
		// return
		return outResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> load(final SecurityContext securityContext,
			   	  			  final O oid) {
		// do the http call
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
															.pathOfEntity(oid));
		String ctxXml = _marshaller.forWriting()
								   .toXml(securityContext);
		HttpResponseResult httpResponse = DelegateForRawREST.GET(restResourceUrl,
										 				   ctxXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(securityContext,
															 			  PersistenceRequestedOperation.LOAD,
															  			  restResourceUrl,httpResponse)
												.identifiedOnErrorBy(oid);
		// check that the received entity is the expected one
		if (outResponse.hasSucceeded()) _checkReceivedEntity(oid,outResponse.getOrThrow());

		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CREATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> create(final SecurityContext securityContext,
								final M entity) {
		return this.create(securityContext,
						   entity,
						   null);	// no async callback
	}
	@Override
	public CRUDResult<M> create(final SecurityContext securityContext,
								final M entity,
								final COREServiceMethodCallbackSpec callbackSpec) {
		// do the http call
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
															   			  .pathOfAllEntities());	//   .pathOfEntity(entity.getOid())); 	// _resourcePathForRecord(record,PersistenceRequestedOperation.CREATE);
		String ctxXml = _marshaller.forWriting().toXml(securityContext);
		String entityXml = _marshaller.forWriting().toXml(entity);
		HttpResponseResult httpResponse = DelegateForRawREST.POST(restResourceUrl,
										  					ctxXml,
										  					entityXml);
		// map the response

		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(securityContext,
															              PersistenceRequestedOperation.CREATE,
															  			  entity,
															  			  restResourceUrl,httpResponse);
		// check that the received entity is the expected one just if requested entity oid is not null .
		// It could be possible on creating a new entity not to send an entity oid
		if (outResponse.hasSucceeded() && entity.getOid() != null) _checkReceivedEntity(entity.getOid(),outResponse.getOrThrow());

		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	UPDATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> update(final SecurityContext securityContext,
								final M entity) {
		return this.update(securityContext,
						   entity,
						   null);		// no async callback
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> update(final SecurityContext securityContext,
								final M entity,
								final COREServiceMethodCallbackSpec callbackSpec) {
		// do the http call
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 		   			  .pathOfEntity(entity.getOid())); 	// _resourcePathForRecord(record,PersistenceRequestedOperation.UPDATE);
		String ctxXml = _marshaller.forWriting().toXml(securityContext);
		String entityXml = _marshaller.forWriting().toXml(entity);
		HttpResponseResult httpResponse = DelegateForRawREST.PUT(restResourceUrl,
										 				   ctxXml,
										 				   entityXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(securityContext,
																 		  PersistenceRequestedOperation.UPDATE,
															  			  entity,
															  			  restResourceUrl,httpResponse);
		// check that the received entity is the expected one
		if (outResponse.hasSucceeded() ) _checkReceivedEntity(entity.getOid(),outResponse.getOrThrow());

		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> delete(final SecurityContext securityContext,
							    final O oid) {
		return this.delete(securityContext,
						   oid,
						   null);	// no async callback
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> delete(final SecurityContext securityContext,
							    final O oid,
							    final COREServiceMethodCallbackSpec callbackSpec) {
		// do the http call
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
															   			  .pathOfEntity(oid));
		String ctxXml = _marshaller.forWriting().toXml(securityContext);
		HttpResponseResult httpResponse = DelegateForRawREST.DELETE(restResourceUrl,
															  ctxXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(securityContext,
											  				 			  PersistenceRequestedOperation.DELETE,
											  				 			  restResourceUrl,httpResponse)
															  .identifiedOnErrorBy(oid);
		// check that the received entity is the expected one
		if (outResponse.hasSucceeded()) _checkReceivedEntity(oid,outResponse.getOrThrow());

		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _checkReceivedEntity(final O requestedOid,final M receivedEntity) {
		// check that the received type is the expected one
//		if (receivedEntity.getClass() != _modelObjectType) throw new IllegalStateException(Throwables.message("The client REST proxy received type ({}) is NO the expected one {}",
//																							  	 			  receivedEntity.getClass(),_modelObjectType));
		// Check that it's about the same entity by comparing the received entity oid with the expected one
//		if (!receivedEntity.getOid().equals(requestedOid)) throw new IllegalStateException(Throwables.message("The client REST proxy received entity has NOT the same oid as the expected one (recived={}, expected={})",
//																									 	 	  receivedEntity.getOid(),requestedOid));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LOG
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _logResponse(final Url restResourceUrl,
						   	    final CRUDResult<M> opResult) {
		if (opResult.hasSucceeded()) {
			log.info("Successful REST {} operation at resource path={} on entity with oid={}",opResult.getRequestedOperation(),restResourceUrl,opResult.getOrThrow().getOid());
		}
		else if (opResult.asCRUDError().wasBecauseClientCouldNotConnectToServer()) {			// as(CRUDError.class)
			log.error("Client cannot connect to REST endpoint {}",restResourceUrl);
		}
		else if (!opResult.asCRUDError().wasBecauseAClientError()) {							// as(CRUDError.class)
			log.error("REST: On requesting the {} operation, the REST resource with path={} returned a persistence error code={}",
					  opResult.getRequestedOperation(),restResourceUrl,opResult.asCRUDError().getErrorType().getCode());
			log.debug("[ERROR]: {}",opResult.getDetailedMessage());
		}
		else {
			log.debug("Client error on requesting the {} operation, the REST resource with path={} returned: {}",
					  opResult.getRequestedOperation(),restResourceUrl,opResult.getDetailedMessage());
		}
	}
}
