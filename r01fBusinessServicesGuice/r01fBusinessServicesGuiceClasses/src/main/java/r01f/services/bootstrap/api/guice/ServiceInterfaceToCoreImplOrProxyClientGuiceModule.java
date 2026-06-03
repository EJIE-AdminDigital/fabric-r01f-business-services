package r01f.services.bootstrap.api.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import r01f.services.bootstrap.ServiceInterfaceMatch;
import r01f.services.bootstrap.ServiceInterfacesMatchings;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.interfaces.ServiceInterface;

/**
 * [client api] uses a guice's {@link MapBinder} that indexes each [service impl] or [proxy] by the [service interface]
 * (a map like Map<Class,ServiceInterface>)
 *
 * This map is injected at the [client api] annotated by the [client api appcode]:
 *	    @Singleton
 *	    public class MyApi
 *	    	 extends BaseApi {
 *      
 *	    	@Inject
 *	    	public MyApi(@Named(apiAppCode) Map<Class,ServiceInterface> ifaceToImplOrProxy) {
 *	    		...
 *	    	}
 *	    }
 * This map is used at the CLIENT API when "going to" the CORE (either directly or indirectly through a proxy)
 * 		MyServiceInterface iface = ifaceToImplOrProxy.get(MyServiceInterface.class);	<-- this will return the [core impl] or [proxy]
 * 																							implementing the [service interface] MyServiceInterface
 * 																							using the Map<Class,ServiceInterface>
 *
 * Usually the bootstrap is done with a SINGLE ServicesBootstrapConfig for a given [client api appCode], 
 * ... BUT it's possible to bootstrap with MULTIPLE ServicesBootstrapConfig (either for the SAME [client api appCode] or for DIFFERENT [client api appCode])
 * For example:
 *		- single api with it's [bootstrap config]
 *		- multiple apis with DIFFERENT [client api appCode] and DIFFERENT [bootstrap configs] for each API (each api has different COREs or PROXIES)
 *		- multiple apis with the SAME [client api appCode] and DIFFERENT [bootstrap configs] for each API (each api has different COREs or PROXIES)ç
 *		  (this last case is tricky since the [service interface] to [core impl] or [proxy] in injected in a Map annotated with [client api appCode])
 */
public class ServiceInterfaceToCoreImplOrProxyClientGuiceModule 
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final ServiceInterfacesMatchings _serviceInterfacesToCoreImplOrProxyMatchings;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServiceInterfaceToCoreImplOrProxyClientGuiceModule(final ServiceInterfacesMatchings serviceInterfacesToCoreImplOrProxyMatchings) {
		_serviceInterfacesToCoreImplOrProxyMatchings = serviceInterfacesToCoreImplOrProxyMatchings;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	GUICE MODULE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {
		// [1] - create the map binder annotated with @Named(clientApiAppCode)
		// a) the @Named(clientApiAppCode) annotation
		ClientApiAppCode theClientApiAppCode = _serviceInterfacesToCoreImplOrProxyMatchings.getClientApiAppCode();
		Named mapBinderNamed = Names.named(theClientApiAppCode.asString());
		
		// b) the MapBinder<Class,ServiceInterface>
		@SuppressWarnings("rawtypes")
		MapBinder<Class,ServiceInterface> srvcIfaceTypeToCoreImplOrProxyBinder = MapBinder.newMapBinder(binder,
																				 				 		Class.class,ServiceInterface.class,
																				 				 		mapBinderNamed);			
		// [2] - add the [service interface] to [core impl] or [proxy] matchings
		if (_serviceInterfacesToCoreImplOrProxyMatchings.hasData()) {
			for (ServiceInterfaceMatch ifaceMatch : _serviceInterfacesToCoreImplOrProxyMatchings.getAllServiceInterfaceToCoreImplOrProxyMatchings()) {
				Class<? extends ServiceInterface> iface = ifaceMatch.getServiceInterfaceType();
				Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> coreImplOrProxy = ifaceMatch.getCoreImplOrProxyType();

				// a an interface to impl / proxy binding to the Map used at ServicesClientProxyLazyLoaderGuiceMethodInterceptor
				srvcIfaceTypeToCoreImplOrProxyBinder.addBinding(_captureType(iface))
								 			 	    .to(_captureSubType(coreImplOrProxy));
			}
		}
	}
	@SuppressWarnings("unchecked")
	private static <S extends ServiceInterface> Class<S> _captureType(final Class<? extends ServiceInterface> type) {
		return (Class<S>)type;
	}
	@SuppressWarnings("unchecked")
	private static <S extends IsCoreServiceImplOrProxyToCoreServiceImpl> Class<? extends S> _captureSubType(final Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> type) {
		return (Class<? extends S>)type;
	}

}
