package r01f.services.bootstrap.guice;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.inject.Binder;
import com.google.inject.Module;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.eventbus.EventBus;
import r01f.reflection.scanner.ClassgraphScanHandler;
import r01f.services.bootstrap.ServiceInterfaceMatch;
import r01f.services.bootstrap.ServiceInterfaceToCOREImplOrProxyMatcher;
import r01f.services.bootstrap.ServiceInterfacesMatchings;
import r01f.services.bootstrap.ServiceInterfacesMatchingsForCoreModule;
import r01f.services.bootstrap.api.guice.ServicesBootstrapClientAPIGuiceModuleBase;
import r01f.services.bootstrap.api.guice.ServicesBootstrapClientGuiceModulesFactory;
import r01f.services.bootstrap.config.ServicesBootstrapConfig;
import r01f.services.bootstrap.config.ServicesBootstrapConfigUtil;
import r01f.services.bootstrap.config.ServicesCoreEventsHandlingConfig;
import r01f.services.bootstrap.config.ServicesCoreModuleBootstrapConfig;
import r01f.services.bootstrap.core.guice.ServicesBootstrapCoreGuiceModuleBase;
import r01f.services.bootstrap.core.guice.ServicesBootstrapCorePrivateGuiceModule;
import r01f.services.core.persistence.jobs.AsyncEventBusProvider;
import r01f.services.core.persistence.jobs.ExecutorServiceManagerProvider;
import r01f.services.core.persistence.jobs.SyncEventBusProvider;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.types.ExecutionMode;
import r01f.types.JavaPackage;
import r01f.util.types.collections.CollectionUtils;


@Slf4j
public final class ServicesBootstrapGuiceModuleBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Service matcher in charge of finding the [service interface] to [proxy] or [core impl] matchings
	 * (for every [service interface] it tries to find a [core impl] or a [proxy])
	 */
	private final ServiceInterfaceToCOREImplOrProxyMatcher _serviceInterfaceToCoreImplOrProxyMatcher;
	/**
	 * Bootstrap configs
	 */
	private final Collection<ServicesBootstrapConfig> _bootstrapCfgs;
	/**
	 * The [service interface] to [core impl] or [proxy] matchings by [client api app code]
	 */
	private final Collection<ServiceInterfacesMatchings> _serviceInterfacesMatchingsByClientApiAppCode;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////	
	public static ServicesBootstrapGuiceModuleBuilder createFor(final ServicesBootstrapConfig... bootstrapCfgs) {
		return new ServicesBootstrapGuiceModuleBuilder(CollectionUtils.hasData(bootstrapCfgs) ? Arrays.asList(bootstrapCfgs) : Collections.emptyList());
	}
	public static ServicesBootstrapGuiceModuleBuilder createFor(final Collection<ServicesBootstrapConfig> bootstrapCfgs) {
		return new ServicesBootstrapGuiceModuleBuilder(CollectionUtils.hasData(bootstrapCfgs) ? bootstrapCfgs : Collections.emptyList());
	}
	private ServicesBootstrapGuiceModuleBuilder(final Collection<ServicesBootstrapConfig> bootstrapCfgs) {
		_bootstrapCfgs = bootstrapCfgs;
		
		// [service interface] to [proxy] or [core impl] matching are guessed using 
		// classpath scanning; in order to limit the scanning the [java packages] where
		// to look for [service interface] or [proxy] or [core impl] is needed
		Set<JavaPackage> javaPackagesToScan = ServicesBootstrapConfig.getJavaPackagesToScanFor(bootstrapCfgs);
		ClassgraphScanHandler classpathScanner = new ClassgraphScanHandler(javaPackagesToScan,
																		   this.getClass().getClassLoader());
		_serviceInterfaceToCoreImplOrProxyMatcher = new ServiceInterfaceToCOREImplOrProxyMatcher(classpathScanner);	
		
		Stopwatch sw = Stopwatch.createStarted();
		
		// Usually the bootstrap is done with a SINGLE ServicesBootstrapConfig for a given client api appCode, 
		// ... BUT it's possible to bootstrap with MULTIPLE ServicesBootstrapConfig (either for the SAME [client api appCode] or for DIFFERENT [client api appCode])
		// ... the first step is GROUP the ServicesBootstrapConfig by [client api appCode]
		Map<ClientApiAppCode,
			Collection<ServicesBootstrapConfig>> bootstrapCfgByClientApiAppCode = ServicesBootstrapConfigUtil.of(_bootstrapCfgs)
																											 .getBootstrapConfigsGroupedByClientApiAppCode();
		
		log.warn("\n\n\n" +
				 "BOOTSTRAPPING {}\n" +
				 "####################################################################################################",
				 bootstrapCfgByClientApiAppCode.keySet());
		
		// [1] - Find [service interface] to [core impl] or [proxy] matchings 
		// 		 ============================================================
		//  	 The [client api] uses a guice's MapBinder that indexes each [service impl] or [proxy] by the [service interface]
		//		 (a map like Map<ServiceInterface,coreImpl|coreProxy> that in guice "terminology is a MapBinder<Class,ServiceInterface> -the key & value are reversed-)
		//
		//		 This map is injected at the [client api] annotated by the [client api appcode]:
		//			@Singleton
		//			public class MyApi
		//				 extends BaseApi {
		//
		//				@Inject
		//				public MyApi(@Named(apiAppCode) Map<Class,ServiceInterface> ifaceToImplOrProxy) {
		//					...
		//				}
		//			}
		//		This map is used at the CLIENT API when "going to" the CORE (either directly or indirectly through a proxy)
		//	 		MyServiceInterface iface = ifaceToImplOrProxy.get(MyServiceInterface.class);	<-- this will return the [core impl] or [proxy]
		//	 																							implementing the [service interface] MyServiceInterface
		//	 																							using the Map<Class,ServiceInterface>
		//
		// 		Usually the bootstrap is done with a SINGLE ServicesBootstrapConfig for a given [client api appCode], 
		//		... BUT it's possible to bootstrap with MULTIPLE ServicesBootstrapConfig (either for the SAME [client api appCode] or for DIFFERENT [client api appCode])
		//			for example:
		//				- single [client api]
		//				  ... the [client api] is injected with a Map annotated with the [client api appCode] that contains the [service interface] to [core impl] or [proxy] matchings
		//
		//				- multiple DIFFERENT [client apis] (different api appCode)
		//				  ... EACH [client api] is injected with it's OWN Map annotated with it's OWN [client api appCode] so there's NO conflict between [client apis]
		//
		//				- multiple [client apis] with the SAME [client api appCode] 
		//				  ... this last case is tricky since ALL the [client api] is injected with the SAME Map annotated with [client api appCode] 
		//					  that contains [service interface] to [core impl] or [proxy] but for ALL [client apis] and all [core modules]
		//				  ... so ALL [service interface] to [core impl] for those DIFFERENT [core modules] must be MERGED in a SINGLE Map  
		log.warn("[START] - Matching  [service interface] to (1) [core impl] or (2) [core proxy]");
		_serviceInterfacesMatchingsByClientApiAppCode = _serviceInterfaceToCoreImplOrProxyMatcher.serviceInterfacesToCoreImplOrProxyMatchings(bootstrapCfgByClientApiAppCode);
		log.warn("[END] ({} millis) - Matching [service interface] to (1) [core impl] or (2) [core proxy] ({} millis)\n" +
				 "####################################################################################################" +
				 "\n\n\n\n\n",
				 sw.elapsed(TimeUnit.MILLISECONDS));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CLIENT API GUICE MODULE CREATION
/////////////////////////////////////////////////////////////////////////////////////////	
	public <AGM extends ServicesBootstrapClientAPIGuiceModuleBase> Module buildClientApiGuiceModuleFor(final AGM clientApiBootGuiceModule) {
		log.warn("[API GUICE BINGING] - Creating CLIENT guice module for client appCode={} api type={}",
				 clientApiBootGuiceModule.getClientApiAppCode(),clientApiBootGuiceModule.getClientApiType().getSimpleName());
		
		// [1] - get the [client api bootstrap config] for the given [client api app code]
		ServicesBootstrapConfig bootCfg = ServicesBootstrapConfigUtil.of(_bootstrapCfgs)
																	 .getBootstrapConfigFor(clientApiBootGuiceModule.getClientApiAppCode(),
																			 				clientApiBootGuiceModule.getClientApiType());
		// [2] - get the [service interface] to [core impl] or [proxy] matchings
		ServiceInterfacesMatchings srvcIfaceMatchings = _serviceInterfacesMatchingsByClientApiAppCode.stream()
																									 .filter(matchings -> matchings.getClientApiAppCode().is(clientApiBootGuiceModule.getClientApiAppCode()))	// same api appCode
																									 .findFirst().orElseGet(null);
		// [3] - create the client api guice module
		// a) - Create the client api bootstrap guice modules
		Collection<Module> clientApiBootstrapModules = ServicesBootstrapClientGuiceModulesFactory.createClientBootstrapGuiceModulesFor(bootCfg,
																							   										   srvcIfaceMatchings);
		// b) - Merge with the client api bootstrap module (this)
		Module outGuiceModule = new Module() {
										@Override
										public void configure(final Binder binder) {
											// install the client api bootstrap modules
											if (CollectionUtils.hasData(clientApiBootstrapModules)) clientApiBootstrapModules.forEach(mod -> binder.install(mod));
											
											// install the "original" client api bootstrap module 
											binder.install(clientApiBootGuiceModule);
										}
							   };
		// [99] - Return
		return outGuiceModule;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CORE MODULE GUICE MODULE CREATION
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * If the core module is isolated then it MUST be installed as a private module in order to avoid bindings collisions
	 * (ie JPA's guice persist modules MUST reside in separate private guice modules -see https://github.com/google/guice/wiki/GuicePersistMultiModules-)
	 * This method returns the core module as a private module if it's isolated or as it is if it's not isolated
	 * @param coreModuleBootCfg
	 * @param srvcIfaceMatchings
	 * @return
	 */
	public <CGM extends ServicesBootstrapCoreGuiceModuleBase> Module buildCoreGuiceModuleFor(final CGM coreModuleBootGuiceModule) {
		log.warn("[CORE GUICE BINDING] - Creating CORE guice module for core appCode={} module={}",
				 coreModuleBootGuiceModule.getCoreAppCode(),coreModuleBootGuiceModule.getCoreModule());
		
		
		Module outCoreGuiceModule = null;
		if (coreModuleBootGuiceModule.isIsolated()) {
			// [1] - get the [core module bootstrap config] for the given [core app code] and [core module]
			ServicesCoreModuleBootstrapConfig moduleBootCfg = ServicesBootstrapConfigUtil.of(_bootstrapCfgs)
																						 .getCoreModuleBootstrapConfigFor(coreModuleBootGuiceModule.getCoreAppCode(),
																							 					  		  coreModuleBootGuiceModule.getCoreModule());
			// [2] - get the [service interface] to [core impl] or [core proxy] matchings
			//		 for the core module
			ServiceInterfacesMatchingsForCoreModule srvcIfaceMatchings = _serviceInterfacesMatchingsByClientApiAppCode.stream()
																													  .map(matchings -> matchings.getServiceInterfaceMatchingsFor(coreModuleBootGuiceModule.getCoreAppCode(),
																															  													  coreModuleBootGuiceModule.getCoreModule()))
																													  .findFirst()
																													  .orElse(null);
			// if the core module SHOULD reside in it's own private guice module in order to avoid bindings collisions
			// (ie JPA's guice persist modules MUST reside in separate private guice modules -see https://github.com/google/guice/wiki/GuicePersistMultiModules-)
			
			
			// in this case the [core service impls] (layer 1) MUST be exposed to the main injector
			// ... find the service core impls that will be binded as singletons
			Collection<ServiceInterfaceMatch> coreImplMatchings = srvcIfaceMatchings != null 
															   			? srvcIfaceMatchings.getServiceInterfacesMatchings()
																		: null;
			// At the private module:
			//	i - bind every service core impl as a singleton
			//	ii- bootstrap the core
			outCoreGuiceModule = new ServicesBootstrapCorePrivateGuiceModule(moduleBootCfg,
																		  	 coreImplMatchings,				// core impls that will be binded as singletons and exposed
																		  	 coreModuleBootGuiceModule);	// the module to be isolated
		} else {
			// not isolated core module (ie: REST or Servlet modules)
			// they usually DO NOT contain service core impls so there's no service core impl to bind
			// ... just bootstrap the module
			outCoreGuiceModule = coreModuleBootGuiceModule;
		}
		return outCoreGuiceModule;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EVENT BUS 
/////////////////////////////////////////////////////////////////////////////////////////
	public static Module createCoreEventBusGuiceModuleFor(final ServicesCoreEventsHandlingConfig cfg) {
		if (cfg == null) return null;
		return _createCoreEventBusModule(cfg);
	}
	private static Module _createCoreEventBusModule(final ServicesCoreEventsHandlingConfig coreEventsHandlingConfig) {
		return binder -> {
					if (coreEventsHandlingConfig == null) {
						log.warn("NO [EventBus] is configured: if you're using events, review the [core] bootstrapping code (if a common [EventBus] is supposed to be used, remember to configure it!!)");
						return;
					}
					log.warn("EVENT HANDLING: {}",coreEventsHandlingConfig.debugInfo());

					// The EventBus needs an ExecutorService (a thread pool) to manage events in the background
					if (coreEventsHandlingConfig.getExecutionMode() == ExecutionMode.ASYNC) {
						ExecutorServiceManagerProvider execServiceManagerProvider = new ExecutorServiceManagerProvider(coreEventsHandlingConfig.getNumberOfBackgroundThreads());
						binder.bind(ExecutorServiceManager.class)
							  .toProvider(execServiceManagerProvider)
							  .in(Singleton.class);
						// Expose the ServiceHandler to stop the exec manager threads
						String bindingName = "r01.eventBus.backgroundTasksExecService";
						log.warn("Expose the ServiceHandler '{}' to handle the Executor Service Manager ",bindingName);
						// do NO forget!!
						ServicesBootstrapGuiceUtil.bindServiceHandler(binder,
																 	  ExecutorServiceManager.class,bindingName);
						// create the event bus provider
						binder.bind(EventBus.class)
							  .toProvider(AsyncEventBusProvider.class)	// AsyncEventBusProvider needs an ExecutorServiceManager that MUST be binded
							  .in(Singleton.class);
					} else {
						binder.bind(EventBus.class)
								 .toProvider(SyncEventBusProvider.class)
								 .in(Singleton.class);
					}
			  };
	}


}		
