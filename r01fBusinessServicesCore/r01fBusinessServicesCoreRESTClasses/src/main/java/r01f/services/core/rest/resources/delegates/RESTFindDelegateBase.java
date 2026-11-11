package r01f.services.core.rest.resources.delegates;

import java.net.URI;
import java.time.Instant;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.experimental.Accessors;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectOID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.services.core.rest.RESTOperationsResponseBuilder;
import r01f.services.core.rest.RESTOperationsResponseBuilder.PersistenceOperationOnObjectResulCollectionToReponseEntity;
import r01f.services.core.rest.RESTOperationsResponseBuilder.PersistenceOperationOnObjectResulToReponseEntity;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;

/**
 * Base type for REST services that encapsulates the common Find OPs.
 */
@Accessors(prefix="_")
public abstract class RESTFindDelegateBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	          extends RESTDelegateForModelObjectBase<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final FindServicesForModelObject<O,M> _findServices;
	protected final MediaType _mediaType;
	protected final PersistenceOperationOnObjectResulCollectionToReponseEntity<M> _transformer;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <F extends FindServicesForModelObject<O,M>> F getFindServicesAs(final Class<F> type) {
		return (F)_findServices;
	}
	@SuppressWarnings("unchecked")
	public <R extends RESTFindDelegateBase<O,M>> R getRESTFindDelegateAs(final Class<R> type) {
		return (R)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTFindDelegateBase(final Class<M> modelObjectType,
							    final FindServicesForModelObject<O,M> persistenceServices) {
		this(modelObjectType, persistenceServices, null, null);
	}
	public RESTFindDelegateBase(final Class<M> modelObjectType,
							    final FindServicesForModelObject<O,M> persistenceServices,
							    final MediaType mediaType ) {
		this(modelObjectType,persistenceServices,mediaType, null );
	}
	public RESTFindDelegateBase(final Class<M> modelObjectType,
							    final FindServicesForModelObject<O,M> findServices,
							    final MediaType mediaType,
							    final PersistenceOperationOnObjectResulCollectionToReponseEntity<M> transformer) {
		super(modelObjectType);
		_findServices = findServices;
		_mediaType =    ( mediaType   != null )?  mediaType :
			                                      MediaType.APPLICATION_XML_TYPE;
		_transformer =  ( transformer != null )?  transformer :        //Default transformer received via Constructor or created in-situ with a default behaviour (returning CRUDResult)
			                                      c -> {return c ; };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findAll(final SecurityContext securityContext,final String resourcePath) {
		return findAll(securityContext,resourcePath,_transformer);
	}
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext
	 * @param PersistenceOperationOnObjectResulToReponseEntity<?>  transformer.
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findAll(final SecurityContext securityContext,final String resourcePath, final PersistenceOperationOnObjectResulToReponseEntity<?> transformer) {
		FindOIDsResult<O> findResult = _findServices.findAll(securityContext);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															    .at(URI.create(resourcePath))
															    .mediaType(_mediaType)
															    .withPersistenceOperationTransformer((PersistenceOperationOnObjectResulCollectionToReponseEntity<?>) transformer)
															.build(findResult);
		return outResponse;
	}

	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param PersistenceOperationOnObjectResulToReponseEntity<?>  transformer.
	 * @param createDate
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByCreateDate(final SecurityContext securityContext,final String resourcePath,
									 final Range<Instant> createDate) {
		return findByCreateDate(securityContext,resourcePath,_transformer,createDate);
	}

	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param PersistenceOperationOnObjectResulToReponseEntity<?>  transformer.
	 * @param createDate
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByCreateDate(final SecurityContext securityContext,final String resourcePath,final PersistenceOperationOnObjectResulToReponseEntity<?> transformer,
									 final Range<Instant> createDate) {
		FindOIDsResult<O> findResult = _findServices.findByCreateDate(securityContext,
																	  createDate);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
															    .withPersistenceOperationTransformer((PersistenceOperationOnObjectResulCollectionToReponseEntity<?>) transformer)
															.build(findResult);
		return outResponse;
	}

	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext
	 * @param lastUpdateDate
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */

	public Response findByLastUpdateDate(final SecurityContext securityContext,final String resourcePath,
										 final Range<Instant> lastUpdateDate) {
		return findByLastUpdateDate(securityContext,resourcePath,_transformer,lastUpdateDate);
	}

	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param PersistenceOperationOnObjectResulToReponseEntity<?>  transformer.
	 * @param lastUpdateDate
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByLastUpdateDate(final SecurityContext securityContext,final String resourcePath,final PersistenceOperationOnObjectResulToReponseEntity<?> transformer,
										 final Range<Instant> lastUpdateDate) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdateDate(securityContext,
																		 lastUpdateDate);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
																.withPersistenceOperationTransformer((PersistenceOperationOnObjectResulCollectionToReponseEntity<?>) transformer)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids created by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param creatorPrincipalId
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByCreator(final SecurityContext securityContext,final String resourcePath,
								  final SecurityPrincipalID creatorPrincipalId) {
		return findByCreator(securityContext,resourcePath,_transformer, creatorPrincipalId);
	}
	/**
	 * Finds all persisted model object oids created by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param PersistenceOperationOnObjectResulToReponseEntity<?>  transformer.
	 * @param creatorPrincipalId
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByCreator(final SecurityContext securityContext,final String resourcePath,final PersistenceOperationOnObjectResulToReponseEntity<?> transformer,
								  final SecurityPrincipalID creatorPrincipalId) {
		FindOIDsResult<O> findResult = _findServices.findByCreator(securityContext,
																   creatorPrincipalId);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
																.withPersistenceOperationTransformer((PersistenceOperationOnObjectResulCollectionToReponseEntity<?>) transformer)
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids last updated by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param updaterPrincipalId
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByLastUpdater(final SecurityContext securityContext,final String resourcePath,
									  final SecurityPrincipalID updaterPrincipalId) {

		return this.findByLastUpdater(securityContext,resourcePath,_transformer,
									  updaterPrincipalId);
	}
	/**
	 * Finds all persisted model object oids last updated by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param PersistenceOperationOnObjectResulToReponseEntity<?>  transformer.
	 * @param updaterPrincipalId
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public Response findByLastUpdater(final SecurityContext securityContext,final String resourcePath,final PersistenceOperationOnObjectResulToReponseEntity<?> transformer,
									  final SecurityPrincipalID updaterPrincipalId) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdater(securityContext,
																	   updaterPrincipalId);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
																.at(URI.create(resourcePath))
																.mediaType(_mediaType)
																.withPersistenceOperationTransformer((PersistenceOperationOnObjectResulCollectionToReponseEntity<?>) transformer)
															.build(findResult);
		return outResponse;
	}
}
