package r01f.services.core.delegates;

 
import com.google.common.reflect.TypeToken;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.generics.TypeRef;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.events.HasCOREEventBus;
import r01f.services.interfaces.ServiceInterface;

@Accessors(prefix="_")
public abstract class ServicesDelegateBase
           implements HasCOREEventBus {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Core config
	 */
	@Getter protected final ServicesCoreModuleConfig _coreConfig;
	/**
	 * The service implementation
	 */
	@Getter protected final ServiceInterface _serviceImpl;
	/**
	 * An event bus to dispatch background jobs
	 */
	@Getter protected final EventBus _eventBus;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesDelegateBase(final ServicesCoreModuleConfig coreCfg,
								final ServiceInterface serviceImpl,
								final EventBus eventBus) {
		_serviceImpl = serviceImpl;
		_coreConfig = coreCfg;
		_eventBus = eventBus;
	}
	public ServicesDelegateBase(final ServicesCoreModuleConfig coreCfg,
								final ServiceInterface serviceImpl) {
		this(coreCfg,
			 serviceImpl,
			 null);			// no event bus
	}
	public ServicesDelegateBase(final ServicesCoreModuleConfig coreCfg,
								final EventBus eventBus) {
		this(coreCfg,
			 null,			// no service impl
			 eventBus);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked" })
	protected <S extends ServiceInterface> S getServiceImplAs(final Class<S> serviceInterfaceType) {
		return (S)_serviceImpl;
	}
	@SuppressWarnings({ "unchecked" })
	protected <S extends ServiceInterface> S getServiceImplAs(final TypeToken<S> serviceInterfaceType) {
		return (S)_serviceImpl;
	}
	@SuppressWarnings({ "unchecked" })
	protected <S extends ServiceInterface> S getServiceImplAs(final TypeRef<S> serviceInterfaceType) {
		return (S)_serviceImpl;
	}
}
