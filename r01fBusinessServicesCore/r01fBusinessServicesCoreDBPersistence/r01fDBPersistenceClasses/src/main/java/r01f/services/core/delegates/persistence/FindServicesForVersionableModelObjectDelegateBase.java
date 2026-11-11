package r01f.services.core.delegates.persistence;


 

import r01f.eventbus.EventBus;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.model.persistence.FindResult;
import r01f.model.security.context.SecurityContext;
import r01f.oids.OIDForVersionableModelObject;
import r01f.oids.PersistableObjectOID;
import r01f.oids.VersionIndependentOID;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.persistence.db.DBFindForModelObject;
import r01f.services.interfaces.FindServicesForVersionableModelObject;

/**
 * Service layer delegated type for CRUD find operations
 */
public abstract class FindServicesForVersionableModelObjectDelegateBase<O extends OIDForVersionableModelObject & PersistableObjectOID,M extends PersistableModelObject<O> & HasVersionableFacet>
		      extends FindServicesForModelObjectDelegateBase<O,M> 
		   implements FindServicesForVersionableModelObject<O,M> {


/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR  
/////////////////////////////////////////////////////////////////////////////////////////
	public FindServicesForVersionableModelObjectDelegateBase(final ServicesCoreModuleConfig coreCfg,
															 final Class<M> modelObjectType,
												  		 	 final DBFindForModelObject<O,M> findServices) {
		this(coreCfg,
			 modelObjectType,
			 findServices,
			 null);
	}
	public FindServicesForVersionableModelObjectDelegateBase(final ServicesCoreModuleConfig coreCfg,
															 final Class<M> modelObjectType,
												  		 	 final DBFindForModelObject<O,M> findServices,
												  		 	 final EventBus eventBus) {
		super(coreCfg,
			  modelObjectType,
			  findServices,
			  eventBus);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public FindResult<M> findAllVersionsOf(final SecurityContext securityContext,
										   final VersionIndependentOID versionIndependentOid) {
		return this.getServiceImplAs(FindServicesForVersionableModelObject.class)
				   .findAllVersionsOf(securityContext,
						   			  versionIndependentOid);
	}
	
}
