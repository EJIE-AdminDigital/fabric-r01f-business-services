package r01f.services.bootstrap.core.guice;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.services.bootstrap.ServiceInterfaceMatch;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.bootstrap.config.ServicesCoreModuleBootstrapConfig;
import r01f.util.types.collections.CollectionUtils;

/**
 * When more than a single coreAppCode / module is found in the classpath there's a big chance for a collision of 
 * binded resources like JPA's EntityManager that MUST be binded at guice's {@link PrivateModule}s 
 * (see guice multiple persist modules at https://github.com/google/guice/wiki/GuicePersistMultiModules)
 * 
 * The solution is isolate core bindings for every coreAppCode / module at a separate private module and expose only 
 * the public service interface implementations.
 */
@Slf4j
@RequiredArgsConstructor
public class ServicesBootstrapCorePrivateGuiceModule 
     extends PrivateModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Core config
	 */
	private final ServicesCoreModuleBootstrapConfig _coreModuleCfg;
	/**
	 * match for each service interfaces to a bean impl or a proxy
	 */
	private final Collection<ServiceInterfaceMatch> _coreImplMatchings;
	/**
	 * Core module
	 */
	private final ServicesBootstrapCoreGuiceModuleBase _coreBootstrapModule;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configure() {
		Binder privateBinder = this.binder();
		
		Stopwatch sw = Stopwatch.createStarted();
		
		// [1] - Create the core bootstrap guice module and install it as PRIVATE module
		// 		 BEWARE!!!	do NOT install the REST core buide modules (they're binded at ServicesMainGuiceBootstrap, otherwise they're not visible
		// 					to the outside world and so the Guice Servlet filter cannot see REST resources)
		log.warn("\n\n" +
				 "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%",
				 _coreModuleCfg.getCoreAppCode(),_coreModuleCfg.getCoreModule());
		log.warn("[START]-Binding PRIVATE CORE guice module for {}/{} using {}",
				 _coreModuleCfg.getCoreAppCode(),_coreModuleCfg.getCoreModule(),
				 _coreBootstrapModule.getClass().getName());
		privateBinder.install(_coreBootstrapModule);
			
		log.warn("  [END] ({} millis)-Binding PRIVATE CORE guice module for {}/{}: {}",
				 sw.elapsed(TimeUnit.MILLISECONDS),
				 _coreModuleCfg.getCoreAppCode(),_coreModuleCfg.getCoreModule(),
				 _coreBootstrapModule.getClass().getName());
		log.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		
		// [2] - Bind [core impls] as @Singleton and expose them to the outer modules 
		//		 (the [core impl] type should be binded to the [service interface] when matching [service interfaces] to [core impl] or [core proxy])
		if (CollectionUtils.hasData(_coreImplMatchings)) {
			for (ServiceInterfaceMatch serviceInterfaceCoreImplMatch : _coreImplMatchings) {
				// core impl
				Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> coreImplType = serviceInterfaceCoreImplMatch.getCoreImplOrProxyType();	// sure it's a core imp
				
				// BIND the service impl as singleton
				privateBinder.bind(coreImplType)		
					  		 .in(Singleton.class);
				
				// EXPOSE the [core impl] in order to be binded to the [service interface] type at ServiceBootstap 
				this.expose(coreImplType);
			}
		}
		
		// [3] - EXPOSE bindings outside the private module if the core bootstrap guice module exposes any of it's bindings
		//		 BEWARE!!  What's being exposed to the outside is the [core Impl], NOT the [service interface] type, 
		//				   The [service interface] type is binded to the [core impl] at ServiceInterfacesToCoreImplsOrProxiesClientGuiceModule
		//				   ... so the [core impl] MUST be visible to the outside world
		if (_coreBootstrapModule instanceof ServicesBootstrapCoreGuiceModuleExposesBindings exposes) {
			exposes.exposeBindings(this.binder());
		}
	}
}
