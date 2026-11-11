package r01f.services.core.persistence;

import java.time.Instant;

import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.model.persistence.CRUDOnMultipleResult;
import r01f.model.persistence.CRUDResult;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.OIDForVersionableModelObject;
import r01f.oids.PersistableObjectOID;
import r01f.oids.VersionIndependentOID;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.callback.spec.COREServiceMethodCallbackSpec;
import r01f.services.core.delegates.persistence.CRUDServicesForVersionableModelObjectDelegateBase;
import r01f.services.interfaces.CRUDServicesForVersionableModelObject;


/**
 * Implements the {@link HasVersionableFacet}-related services which in turn are
 * delegated to {@link CRUDServicesForVersionableModelObjectDelegateBase}
 */
@Accessors(prefix="_")
public abstract class CoreCRUDServicesForVersionableModelObjectBase<O extends OIDForVersionableModelObject & PersistableObjectOID,M extends PersistableModelObject<O> & HasVersionableFacet>
			  extends CoreCRUDServicesForModelObjectBase<O,M>
		   implements CRUDServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param coreCfg
	 * @param modelObjectsMarshaller annotated with @ModelObjectsMarshaller
	 * @param eventBus
	 * @param entityManagerProvider
	 */
	public CoreCRUDServicesForVersionableModelObjectBase(final ServicesCoreModuleConfig coreCfg,
														 final Marshaller modelObjectsMarshaller,
									   					 final EventBus eventBus,
									   					 final Provider<EntityManager> entityManagerProvider) {
		super(coreCfg,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public CRUDResult<M> loadActiveVersionAt(final SecurityContext securityContext,
									   		 final VersionIndependentOID oid,final Instant date) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CRUDServicesForVersionableModelObject.class)
							.loadActiveVersionAt(securityContext,
									 	   		 oid,date);
	}
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public CRUDResult<M> loadWorkVersion(final SecurityContext securityContext,
									   	 final VersionIndependentOID oid) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CRUDServicesForVersionableModelObject.class)
							.loadWorkVersion(securityContext,
									   		 oid);
	}
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public CRUDResult<M> activate(final SecurityContext securityContext,
								  final M entityToBeActivated) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CRUDServicesForVersionableModelObject.class)
							.activate(securityContext,
									  entityToBeActivated);
	}
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public CRUDResult<M> activate(final SecurityContext securityContext,
								  final M entityToBeActivated,
								  final COREServiceMethodCallbackSpec callbackSpec) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CRUDServicesForVersionableModelObject.class)
							.activate(securityContext,
									  entityToBeActivated,
									  callbackSpec);
	}
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public CRUDOnMultipleResult<M> deleteAllVersions(final SecurityContext securityContext,
													 final VersionIndependentOID oid) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CRUDServicesForVersionableModelObject.class)
							.deleteAllVersions(securityContext,
										 	   oid);
	}
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public CRUDOnMultipleResult<M> deleteAllVersions(final SecurityContext securityContext,
													 final VersionIndependentOID oid,
													 final COREServiceMethodCallbackSpec callbackSpec) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(CRUDServicesForVersionableModelObject.class)
							.deleteAllVersions(securityContext,
										 	   oid,
										 	   callbackSpec);
	}
}
