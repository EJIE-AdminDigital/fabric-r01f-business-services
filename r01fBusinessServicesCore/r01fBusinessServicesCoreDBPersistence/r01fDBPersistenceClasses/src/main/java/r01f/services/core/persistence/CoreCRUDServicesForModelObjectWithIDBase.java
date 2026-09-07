package r01f.services.core.persistence;

import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import r01f.eventbus.EventBus;
import r01f.guids.PersistableObjectID;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObjectWithID;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.interfaces.CRUDServicesForModelObjectWithID;

public abstract class CoreCRUDServicesForModelObjectWithIDBase<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>>
			  extends CoreCRUDServicesForModelObjectBase<O,M>
		   implements CRUDServicesForModelObjectWithID<O,I,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param cfg core config
	 * @param modelObjectsMarshaller annotated with @ModelObjectsMarshaller
	 * @param eventBus
	 * @param entityManagerProvider
	 */
	public CoreCRUDServicesForModelObjectWithIDBase(final ServicesCoreModuleConfig cfg,
									   		  		final Marshaller modelObjectsMarshaller,
									   		  		final EventBus eventBus,
									   		  		final Provider<EntityManager> entityManagerProvider) {
		super(cfg,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SERVICES EXTENSION
// 	IMPORTANT!!! Do NOT put any logic in these methods ONLY DELEGATE!!!
/////////////////////////////////////////////////////////////////////////////////////////
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadById(final SecurityContext securityContext,
								  final I id) {
		return this.forSecurityContext(securityContext)
				   .createDelegateAs(CRUDServicesForModelObjectWithID.class)
				   .loadById(securityContext,
							 id);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> deleteById(final SecurityContext securityContext,
								    final I id) {
		return this.forSecurityContext(securityContext)
 				   .createDelegateAs(CRUDServicesForModelObjectWithID.class)
				   .deleteById(securityContext,
						   	   id);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<Boolean> exists(final SecurityContext securityContext,
													  final I id) {
		return this.forSecurityContext(securityContext)
 				   .createDelegateAs(CRUDServicesForModelObjectWithID.class)
				   .exists(securityContext,
						   id);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<I> getIdOf(final SecurityContext securityContext,
										  	     final O oid) {
		return this.forSecurityContext(securityContext)
				   .createDelegateAs(CRUDServicesForModelObjectWithID.class)
				   .getIdOf(securityContext,
							oid);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<O> getOidOf(final SecurityContext securityContext,
												  final I id) {
		return this.forSecurityContext(securityContext)
				   .createDelegateAs(CRUDServicesForModelObjectWithID.class)
				   .getOidOf(securityContext,
							 id);
	}
}
