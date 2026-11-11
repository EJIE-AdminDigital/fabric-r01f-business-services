package r01f.services.core.delegates.persistence;


import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.eventbus.EventBus;
import r01f.model.PersistableModelObjectWithID;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.CRUDResultBuilder;
import r01f.model.persistence.PersistenceOperationExecResultBuilder;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.model.security.context.SecurityContext;
import r01f.model.services.COREServiceMethod;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.services.interfaces.CRUDServicesForModelObjectWithID;
import r01f.validation.ObjectValidationResult;
import r01f.validation.ObjectValidationResultBuilder;

/**
 * Service layer delegated type for CRUD (Create/Read/Update/Delete) operations
 */
@Slf4j
@Accessors(prefix="_")
public abstract class CRUDServicesForModelObjectWithIDDelegateBase<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>>
		      extends CRUDServicesForModelObjectDelegateBase<O,M>
		   implements CRUDServicesForModelObjectWithID<O,I,M>,
		   			  ValidatesModelObjectBeforeCreateOrUpdate<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDServicesForModelObjectWithIDDelegateBase(final ServicesCoreModuleConfig coreCfg,
														final Class<M> modelObjectType,
														final CRUDServicesForModelObject<O,M> crud,
														final EventBus eventBus) {
		super(coreCfg,
			  modelObjectType,
			  crud,
			  eventBus);
	}
	public CRUDServicesForModelObjectWithIDDelegateBase(final ServicesCoreModuleConfig coreCfg,
												  		 final Class<M> modelObjectType,
												  		 final CRUDServicesForModelObject<O,M> crud) {
		this(coreCfg,
			 modelObjectType,
		     crud,
		     null);		// no event bus
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadById(final SecurityContext securityContext,
								  final I id) {
		if (id == null) return CRUDResultBuilder.using(securityContext)
												.on(_modelObjectType)
												.notLoaded()
												.becauseClientBadRequest("The id is mandatory!")
												.build();
		return this.getServiceImplAs(CRUDServicesForModelObjectWithID.class)
				   .loadById(securityContext,
						     id);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> deleteById(final SecurityContext securityContext,
								    final I id) {
		if (id == null) return CRUDResultBuilder.using(securityContext)
												.on(_modelObjectType)
												.notLoaded()
												.becauseClientBadRequest("The id is mandatory!")
												.build();
		return this.getServiceImplAs(CRUDServicesForModelObjectWithID.class)
				   .deleteById(securityContext,
						       id);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<Boolean> exists(final SecurityContext securityContext,
												 	  final I id) {
		if (id == null) return PersistenceOperationExecResultBuilder.using(securityContext)
																	.failed(COREServiceMethod.current())
																	.becauseClientBadRequest("The id is mandatory!");
		return this.getServiceImplAs(CRUDServicesForModelObjectWithID.class)
				   .exists(securityContext,
						   id);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<I> getIdOf(final SecurityContext securityContext,
												 final O oid) {
		if (oid == null) return PersistenceOperationExecResultBuilder.using(securityContext)
																	 .failed(COREServiceMethod.current())
																	 .becauseClientBadRequest("The oid is mandatory!");
		return this.getServiceImplAs(CRUDServicesForModelObjectWithID.class)
				   .getIdOf(securityContext,
						    oid);
	}
	@Override @SuppressWarnings("unchecked")
	public PersistenceOperationResult<O> getOidOf(final SecurityContext securityContext,
												  final I id) {
		if (id == null) return PersistenceOperationExecResultBuilder.using(securityContext)
																	 .failed(COREServiceMethod.current())
																	 .becauseClientBadRequest("The id is mandatory!");
		return this.getServiceImplAs(CRUDServicesForModelObjectWithID.class)
				   .getOidOf(securityContext,
						     id);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	VALIDATION
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public ObjectValidationResult<M> validateModelObjBeforeCreateOrUpdate(final SecurityContext securityContext,
																		  final PersistenceRequestedOperation requestedOp,
																		  final M obj) {
		// validate the data
		ObjectValidationResult<M> superValid = super.validateModelObjBeforeCreateOrUpdate(securityContext,
																  						  requestedOp,
																  						  obj);
		if (!superValid.isValid()) return superValid;
		if (requestedOp.isNOT(PersistenceRequestedOperation.CREATE)) return superValid;	// only on creation we check that there's no other entity with the same id
		
		// check that there's no other entity with the same id
		boolean exists = this.exists(securityContext,
									 obj.getId())
							 .getOrDefault(false);
		return exists ? ObjectValidationResultBuilder.on(obj)
													 .isNotValidBecause("There exists another {} with the same id={}",
																		_modelObjectType.getSimpleName(),
																		obj.getId())
					  : ObjectValidationResultBuilder.on(obj)
										    		 .isValid();
	}
}
