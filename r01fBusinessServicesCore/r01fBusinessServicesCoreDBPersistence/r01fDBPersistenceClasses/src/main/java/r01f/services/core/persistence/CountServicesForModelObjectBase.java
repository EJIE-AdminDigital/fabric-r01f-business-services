package r01f.services.core.persistence;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CountResult;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.interfaces.CountServicesForModelObject;


@Singleton
@Accessors(prefix="_")
public abstract class CountServicesForModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>> 
     		  extends CorePersistenceServicesBase
     	   implements CountServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CountServicesForModelObjectBase(final ServicesCoreModuleConfig coreCfg,
										   final Marshaller modelObjectsMarshaller,
										   final EventBus eventBus,
										   final Provider<EntityManager> entityManagerProvider) {
		super(coreCfg,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////						  	
	
	@Override @SuppressWarnings("unchecked")
	public CountResult<M> countAll(final SecurityContext securityContext) {
		return this.forSecurityContext(securityContext)
				   		.createDelegateAs(CountServicesForModelObject.class)
				   		.countAll(securityContext);
	}
}
