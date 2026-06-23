package r01f.services.bootstrap.api.guice;

import com.google.inject.Binder;
import com.google.inject.Module;

import jakarta.inject.Singleton;
import r01f.services.bootstrap.ServiceInterfaceMatch;
import r01f.services.bootstrap.ServiceInterfacesMatchings;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.interfaces.ServiceInterface;

/**
 * This module binds all the [service interface] PROXIES as singletons
 * (it should NOT be necessary if every [proxy] was annotated with @Singleton)
 */
public class ServiceInterfacesToCoreImplsOrProxiesClientGuiceModule 
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final ServiceInterfacesMatchings _serviceInterfacesToCoreImplOrProxyMatchings;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServiceInterfacesToCoreImplsOrProxiesClientGuiceModule(final ServiceInterfacesMatchings serviceInterfacesToCoreImplOrProxyMatchings) {
		_serviceInterfacesToCoreImplOrProxyMatchings = serviceInterfacesToCoreImplOrProxyMatchings;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		if (!_serviceInterfacesToCoreImplOrProxyMatchings.hasData()) return;

		// Bind client proxies as singletons
		// BEWARE CORE impl was binded as singletons at the private module (see ServicesCoreBootstrapPrivateGuiceModule)
		for (ServiceInterfaceMatch ifaceMatch : _serviceInterfacesToCoreImplOrProxyMatchings.getAllServiceInterfaceToCoreImplOrProxyMatchings()) {
			Class<? extends ServiceInterface> iface = ifaceMatch.getServiceInterfaceType();
			Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> coreImplOrProxyType = ifaceMatch.getCoreImplOrProxyType();

			if (ifaceMatch.isProxy()) {
				// a proxy must NOT be binded if there's another matching for the core bean impl
				binder.bind(_captureSubType(coreImplOrProxyType))
					  .in(Singleton.class);
			} else if (ifaceMatch.isCoreImpl()) {
				// BEWARE! - the [core impl]s usually are binded as singletons in the private module (see ServicesCoreBootstrapPrivateGuiceModule) and exposed to the outer world
			}
			
			// b) bind the service interface to the proxy or impl
			binder.bind(_captureType(iface))
				  .to(_captureSubType(coreImplOrProxyType));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	private static <S extends ServiceInterface> Class<S> _captureType(final Class<? extends ServiceInterface> type) {
		return (Class<S>)type;
	}
	@SuppressWarnings("unchecked")
	private static <S extends IsCoreServiceImplOrProxyToCoreServiceImpl> Class<? extends S> _captureSubType(final Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> type) {
		return (Class<? extends S>)type;
	}
}
