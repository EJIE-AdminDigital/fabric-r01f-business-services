package r01f.services.core.rest.resources.delegates.spring;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.experimental.Accessors;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceException;

import r01f.securitycontext.SecurityContext;
import r01f.services.core.rest.spring.RESTResponsePayload;
import r01f.services.interfaces.CRUDServicesForModelObject;

/**
 * Base type for REST services that encapsulates the common CRUD ops>
 */
@Accessors(prefix="_")
public abstract class RESTCRUDDelegateBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	          extends RESTDelegateForModelObjectBase<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final CRUDServicesForModelObject<O,M> _persistenceServices;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <C extends CRUDServicesForModelObject<O,M>> C getCRUDServicesAs(@SuppressWarnings("unused") final Class<C> type) {
		return (C)_persistenceServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTCRUDDelegateBase(final Class<M> modelObjectType,
							    final CRUDServicesForModelObject<O,M> persistenceServices) {
		super(modelObjectType);
		_persistenceServices = persistenceServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PERSISTENCE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param oid
	 * @return the model object M
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload> load(final SecurityContext securityContext,
			                                        final String resourcePath, final MediaType mediaType,
								                    final O oid) throws PersistenceException {

		CRUDResult<M> loadResult = _persistenceServices.load(securityContext,
									  					     oid);
		ResponseEntity<RESTResponsePayload> outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																					.at(URI.create(resourcePath))
																					.withContentType(mediaType)
																				 .build(loadResult);
		return outResponse;
	}
	
	
	/**
	 * Loads a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param oid
	 * @return the CRUDResult on model object M
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload> loadAsCRUDResult(final SecurityContext securityContext,
			                                              final String resourcePath, final MediaType mediaType,
								                          final O oid) throws PersistenceException {

		CRUDResult<M> loadResult = _persistenceServices.load(securityContext,
									  					     oid);
		ResponseEntity<RESTResponsePayload> outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																								.at(URI.create(resourcePath))
																								.withContentType(mediaType)
																							  .buildAsCRUDResult(loadResult);
		return outResponse;
	}
	
	
	/**
	 * Creates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload> create(final SecurityContext securityContext,
									final String resourcePath,final MediaType mediaType,
						            final M modelObject) throws PersistenceException {

		CRUDResult<M> createResult = _persistenceServices.create(securityContext,
										   	   					 modelObject);
		ResponseEntity<RESTResponsePayload> outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																				    .at(URI.create(resourcePath))
																				    .withContentType(mediaType)
																				  .build(createResult);
		return outResponse;
	}	
	
	/**
	 * Creates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload> createAsCRUDResult(final SecurityContext securityContext,
															      final String resourcePath,final MediaType mediaType,
									                              final M modelObject) throws PersistenceException {

		CRUDResult<M> createResult = _persistenceServices.create(securityContext,
										   	   					 modelObject);
		ResponseEntity<RESTResponsePayload> outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																				    .at(URI.create(resourcePath))
																				    .withContentType(mediaType)
																				  .buildAsCRUDResult(createResult);
		return outResponse;
	}
	
	/**
	 * Updates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload> update(final SecurityContext securityContext,
									                  final String resourcePath,final MediaType mediaType,
						   			                  final M modelObject) throws PersistenceException {
		CRUDResult<M> updateResult = _persistenceServices.update(securityContext,
										   	      				 modelObject);
		ResponseEntity<RESTResponsePayload> outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																		 .at(URI.create(resourcePath))
																	     .withContentType(mediaType)
																	    .build(updateResult);
		return outResponse;
	}
	
	
	/**
	 * Updates a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload>  updateAsCRUDResult(final SecurityContext securityContext,
															 final String resourcePath,final MediaType mediaType,
						   			                         final M modelObject) throws PersistenceException {
		CRUDResult<M> updateResult = _persistenceServices.update(securityContext,
										   	      				 modelObject);
		ResponseEntity<RESTResponsePayload>outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																							 .at(URI.create(resourcePath))
																						     .withContentType(mediaType)
																						    .buildAsCRUDResult(updateResult);
		return outResponse;
	}	
	
	
	/**
	 * Removes a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload> delete(final SecurityContext securityContext,
													  final String resourcePath,final MediaType mediaType,
													  final O oid) throws PersistenceException {
		CRUDResult<M> deleteResult = _persistenceServices.delete(securityContext,
																 oid);
		ResponseEntity<RESTResponsePayload> outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
														    							.at(URI.create(resourcePath))
														    							.withContentType(mediaType)
														    						.build(deleteResult);
		return outResponse;
	}
	
	
	
	/**
	 * Removes a db entity
	 * @param securityContext
	 * @param resourcePath
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public ResponseEntity<RESTResponsePayload>  deleteAsCRUDResult(final SecurityContext securityContext,final String resourcePath,final MediaType mediaType,
									            final O oid) throws PersistenceException {
		CRUDResult<M> deleteResult = _persistenceServices.delete(securityContext,
																 oid);
		ResponseEntity<RESTResponsePayload>  outResponse = RESTOperationsSpringResponseEntityBuilder.crudOn(_modelObjectType)
																    							.at(URI.create(resourcePath))
																    							.withContentType(mediaType)
														    								  .buildAsCRUDResult(deleteResult);
		return outResponse;
	}
}
