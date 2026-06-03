package r01f.services.bootstrap.api.guice;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.services.bootstrap.ServiceInterfacesMatchings;
import r01f.services.bootstrap.config.ServicesBootstrapConfig;

@Slf4j
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ServicesBootstrapClientGuiceModulesFactory {
/////////////////////////////////////////////////////////////////////////////////////////
//	FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates the guice modules needed to bootstrap the client
	 * The required client bindings are: 
	 * 		- the [service interface] to [core impl] or [proxy] MapBinder (something like Map<ServiceInterface,coreImpl|coreProxy>)
	 *		  (these are the binding that the client will use: if both the core impl are available, the core impl will be used
	 *		  ... if just the proxy impl is available, obviously, the proxy will be used)
	 * 		- the [core proxies] as @Singleton (it should not be necessary if every [core proxy] type were annotated with @Singleton)
	 * 		- the [core proxies] exposition properties, ie, if the [core impl] is exposed as a [REST service] the connection properties (host, port, urlpath, etc)
	 * 		  are needed
	 * 		- if there is more than one [core proxies] exposition of the same type (ie REST or Servlet), the implementation class MUST be annotated with a [Named]
	 * 		  annotation and the injection MUST be done with that annotation too (to avoid double binding error)
	 *		- client api as singleton (it should not be necessary if the API type was annotated with @Singleton)
	 *		- any other additional client guice modules
	 * @param bootstrapCfg
	 * @return
	 */
	public static Collection<Module> createClientBootstrapGuiceModulesFor(final ServicesBootstrapConfig bootstrapCfg,
																     	  final ServiceInterfacesMatchings serviceInterfacesMatchings) {
		// contains all the guice modules to be bootstraped: client & core
		List<Module> outClientBootstrapGuiceModules = Lists.newArrayList();
		
		// a) guice's MapBinder that indexes each [service impl] or [proxy] by the [service interface]
		//	  (a map like Map<ServiceInterface,coreImpl|coreProxy> that in guice "terminology is a MapBinder<Class,ServiceInterface> -the key & value are reversed-)
		//	  This map is injected at the [client api] annotated by the [client api appcode]:
		//			@Singleton
		//			public class MyApi
		//				 extends BaseApi {
		//
		//				@Inject
		//				public MyApi(@Named(apiAppCode) Map<Class,ServiceInterface> ifaceToImplOrProxy) {
		//					...
		//				}
		//			}
		//	  This map is used at the CLIENT API when "going to" the CORE (either directly or indirectly through a proxy)
		//	 		MyServiceInterface iface = ifaceToImplOrProxy.get(MyServiceInterface.class);	<-- this will return the [core impl] or [proxy]
		//	 																							implementing the [service interface] MyServiceInterface
		//	 																							using the Map<Class,ServiceInterface>
		outClientBootstrapGuiceModules.add(new ServiceInterfaceToCoreImplOrProxyClientGuiceModule(serviceInterfacesMatchings));
		
		// b) a guice module that binds every [proxy] type as a @Singleton
		//	  (this should NOT be necessary if the [core proxy] types were annotated with @Singleton
		outClientBootstrapGuiceModules.add(new ServiceInterfacesToCoreImplsOrProxiesClientGuiceModule(serviceInterfacesMatchings));

		// [99] - return
		return outClientBootstrapGuiceModules;
	}
}
