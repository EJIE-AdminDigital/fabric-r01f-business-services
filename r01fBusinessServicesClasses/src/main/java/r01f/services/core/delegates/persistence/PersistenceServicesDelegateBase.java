package r01f.services.core.delegates.persistence;

 

import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.delegates.ServicesDelegateBase;
import r01f.services.interfaces.ServiceInterface;

@Accessors(prefix="_")
public abstract class PersistenceServicesDelegateBase
			  extends ServicesDelegateBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceServicesDelegateBase(final ServicesCoreModuleConfig coreCfg,
										   final ServiceInterface serviceImpl,
										   final EventBus eventBus) {
		super(coreCfg,
			  serviceImpl,
			  eventBus);
	}
	public PersistenceServicesDelegateBase(final ServicesCoreModuleConfig coreCfg,
										   final ServiceInterface serviceImpl) {
		this(coreCfg,
			 serviceImpl,
			 null);			// no event bus
	}
	public PersistenceServicesDelegateBase(final ServicesCoreModuleConfig coreCfg,
										   final EventBus eventBus) {
		this(coreCfg,
			 null,			// no service impl
			 eventBus);		
	}
}
