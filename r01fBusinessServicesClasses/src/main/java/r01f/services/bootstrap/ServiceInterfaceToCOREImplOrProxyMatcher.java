package r01f.services.bootstrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;
import r01f.reflection.ReflectionUtils;
import r01f.reflection.scanner.ClassgraphScanHandler;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.bootstrap.config.ServicesBootstrapConfig;
import r01f.services.bootstrap.config.ServicesBootstrapConfigUtil;
import r01f.services.bootstrap.config.ServicesCoreImplOrProxyToCoreModuleConfig;
import r01f.services.bootstrap.config.ServicesCoreModuleBootstrapConfig;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.interfaces.ExposedServiceInterface;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * This utility type performs three operations
 * 		1.- Finds all {@link ServiceInterface}-extending interfaces annotated with @ServiceInterface
 * 		2.- Finds all types implementing COREService or COREProxy
 * 		3.- Tries to match @ServiceInterface-annotated types with a COREService impl or a COREProxyImpl
 */
@Slf4j
public class ServiceInterfaceToCOREImplOrProxyMatcher {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final ServicesClassPathScanner _classPathScanner;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServiceInterfaceToCOREImplOrProxyMatcher(final ClassgraphScanHandler classPathScanner) {
		_classPathScanner = new ServicesClassPathScanner(classPathScanner);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SERVICE INTERFACE TO PROXY MATCH
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Usually the bootstrap is done with a SINGLE ServicesBootstrapConfig for a given client api appCode, 
	 * ... BUT it's possible to bootstrap with MULTIPLE ServicesBootstrapConfig (either for the SAME [client api appCode] or for DIFFERENT [client api appCode])
	 * This method returns a collection of ServiceInterfacesMatchings objects by [client api appCode]
	 * (ie a Collection<ServiceInterfaceMatch> by [client api appCode])
	 * @param servicesBootstrapCfgs
	 * @return
	 */
	public Collection<ServiceInterfacesMatchings> serviceInterfacesToCoreImplOrProxyMatchings(final Collection<ServicesBootstrapConfig> servicesBootstrapCfgs) {
		// Usually the bootstrap is done with a SINGLE ServicesBootstrapConfig for a given client api appCode, 
		// ... BUT it's possible to bootstrap with MULTIPLE ServicesBootstrapConfig (either for the SAME [client api appCode] or for DIFFERENT [client api appCode])
		// ... the first step is GROUP the ServicesBootstrapConfig by [client api appCode]
		Map<ClientApiAppCode,
			Collection<ServicesBootstrapConfig>> bootstrapCfgByClientApiAppCode = ServicesBootstrapConfigUtil.of(servicesBootstrapCfgs)
																											  .getBootstrapConfigsGroupedByClientApiAppCode();
		
		return this.serviceInterfacesToCoreImplOrProxyMatchings(bootstrapCfgByClientApiAppCode);
	}
	/**
	 * Usually the bootstrap is done with a SINGLE ServicesBootstrapConfig for a given client api appCode, 
	 * ... BUT it's possible to bootstrap with MULTIPLE ServicesBootstrapConfig (either for the SAME [client api appCode] or for DIFFERENT [client api appCode])
	 * This method returns a collection of ServiceInterfacesMatchings objects by [client api appCode]
	 * (ie a Collection<ServiceInterfaceMatch> by [client api appCode])
	 * @param servicesBootstrapCfgs
	 * @return
	 */
	public Collection<ServiceInterfacesMatchings> serviceInterfacesToCoreImplOrProxyMatchings(final Map<ClientApiAppCode,Collection<ServicesBootstrapConfig>> bootstrapCfgByClientApiAppCode) {
		return CollectionUtils.hasData(bootstrapCfgByClientApiAppCode) 
				 	? bootstrapCfgByClientApiAppCode.entrySet()
											 		.stream()
											 		.flatMap(me -> me.getValue().stream())
											 		.map(servicesBootCfg -> this.serviceInterfacesToCoreImplOrProxyMatchings(servicesBootCfg))
											 		.collect(Collectors.toList())
					: Collections.emptyList();	// no matchings!!
	}
	/**
	 * Returns a {@link ServiceInterfacesMatchings} object that encapsulates a collection of {@link ServiceInterfaceMatch} objects
	 * that in-turn models a {@link ServiceInterface} matching with a [core impl] or a [proxy] to a [core impl]
	 * @param servicesBootstrapCfg
	 * @return
	 */
	public ServiceInterfacesMatchings serviceInterfacesToCoreImplOrProxyMatchings(final ServicesBootstrapConfig servicesBootstrapCfg) {
		log.warn("########## [client API appCode]={} api type={}> [start] finding [service interface] to [core impl] or [core proxy] matchings",
				 servicesBootstrapCfg.getClientApiAppCode(),
				 servicesBootstrapCfg.getClientApiType().getSimpleName());
		return CollectionUtils.hasData(servicesBootstrapCfg.getCoreModulesBootstrapConfig()) 
					? new ServiceInterfacesMatchings(servicesBootstrapCfg.getClientApiAppCode(),
													 servicesBootstrapCfg.getCoreModulesBootstrapConfig()
																		 .stream()
																		 .map(moduleCfg -> this.serviceInterfaceToCoreImplOrProxyMatchingsFor(moduleCfg))
																		 .collect(Collectors.toList()))
					: new ServiceInterfacesMatchings(servicesBootstrapCfg.getClientApiAppCode(),
													 Collections.emptyList());	// no matchings!!
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public ServiceInterfacesMatchingsForCoreModule serviceInterfaceToCoreImplOrProxyMatchingsFor(final ServicesCoreModuleBootstrapConfig coreModuleCfg) {
		if (CollectionUtils.isNullOrEmpty(coreModuleCfg.getCoreImplOrProxyToCoreModuleConfigs())) {
			log.warn("NO [CORE module] config provided: DO NOT try to match [service interfaces] to [core impls] or [proxies]");
			return new ServiceInterfacesMatchingsForCoreModule(coreModuleCfg.getCoreAppCode(),coreModuleCfg.getCoreModule());	// no matchings!!
		}
		log.warn("[CORE Module: {}.{}] Service interface matchings......",
				 coreModuleCfg.getCoreAppCode(),coreModuleCfg.getCoreModule());
		
		// [1] - Find [service interfaces]: scan classpath looking for interfaces extending ServiceInterface annotated with @ExposedServiceInterface
		log.warn("a) find [service interface] types extending {}",
				 coreModuleCfg.getServiceInterfacesBaseTypes());
		Set<Class<? extends ServiceInterface>> serviceInterfaceTypes = _classPathScanner.findServiceInterfaceTypes(coreModuleCfg.getServiceInterfacesBaseTypes());
		
		// debug
		if (log.isDebugEnabled()) {
			log.debug("... found {} [service interface] extending {}\n" + 
					  "{}",
					  serviceInterfaceTypes.size(),serviceInterfaceTypes,
					  CollectionUtils.hasData(serviceInterfaceTypes)
					  		? serviceInterfaceTypes.stream()
					  							   .map(serviceIface -> "\t-" + serviceIface)
					  							   .collect(Collectors.joining("\n"))
					  		: "\tNO [service interface] found!!");
		}

		// [2] - The CORE can be reached in multiple ways: 
		//			- direct access (IN-PROCESS) to the core service implementation      > at the classpath there MUST exist a [cores service] type implementing the [service interface] 
		//			- proxy access by a REST service to the core service implementation  > at the classpath there MUST exist a [proxy] type implementing the [service interface]
		//		 If the direct access (IN-PROCESS) is available it's the preferred option
		log.warn("b) for every defined [core impl] or [proxy] config try to match [service interfaces] to [core impl] or [proxy]");
		
		ServiceInterfacesMatchingsForCoreModule outMatchingsForModule = new ServiceInterfacesMatchingsForCoreModule(coreModuleCfg.getCoreAppCode(),coreModuleCfg.getCoreModule());
		for (ServicesCoreImplOrProxyToCoreModuleConfig coreImplOrProxyCfg : coreModuleCfg.getCoreImplOrProxyToCoreModuleConfigs()) {
			log.warn("\t-{}",coreImplOrProxyCfg.debugInfo());			
			if (CollectionUtils.isNullOrEmpty(coreImplOrProxyCfg.getCoreServiceOrProxyToCoreServiceBaseTypes())) continue;		// skip 
			
			// find the matchings for the [serviceInterfaces] discovered at [1]
			Collection<ServiceInterfaceMatch> matchings = _findServiceInterfaceToCoreImplOrProxyMatchings(serviceInterfaceTypes,
																										  coreImplOrProxyCfg);
			// find the BEST matching
			if (CollectionUtils.hasData(matchings)) matchings.forEach(match -> outMatchingsForModule.addMatching(match));
		}
		return outMatchingsForModule;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private Collection<ServiceInterfaceMatch> _findServiceInterfaceToCoreImplOrProxyMatchings(final Set<Class<? extends ServiceInterface>> serviceInterfaceTypes,
																			 			      final ServicesCoreImplOrProxyToCoreModuleConfig coreImplOrProxyCfg) {
		Map<Class<? extends ServiceInterface>,
			Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> matchings = _findServiceInterfaceToCoreImplOrProxyMatchings(serviceInterfaceTypes,
																												   					coreImplOrProxyCfg.getCoreServiceOrProxyToCoreServiceBaseTypes(),
																												   					true);	// strict
		Collection<ServiceInterfaceMatch> outMatchings = matchings.entrySet()
																  .stream()
																  .map(me -> new ServiceInterfaceMatch(me.getKey(),me.getValue()))
																  .collect(Collectors.toList());
		return outMatchings;														  
	}
	private Map<Class<? extends ServiceInterface>,
				Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> _findServiceInterfaceToCoreImplOrProxyMatchings(final Set<Class<? extends ServiceInterface>> serviceInterfaceTypes,
																										   					final Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> coreImplOrProxyTypes,
																										   					final boolean strict) {		
		// [1] - do find type implementing types
		if (log.isDebugEnabled()) {
			log.debug("- find matchings for [service interfaces] {} extending {}",
					  serviceInterfaceTypes,
					  coreImplOrProxyTypes);	
		}
		// [2] - Find all types implementing the [core impl] or [core proxy] base type
		Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> coreOrProxyTypes = coreImplOrProxyTypes.stream() 
																											  .flatMap(coreImplOrProxyType -> _classPathScanner.findServiceInterfaceProxyOrCoreImplTypes(coreImplOrProxyType)
																													  										   .stream())
																											  .collect(Collectors.toSet());

		// [3] - Match each type to it's corresponding ServiceInterface type
    	Map<Class<? extends ServiceInterface>,
    		Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> outServiceInterfaceMatchings = Maps.newHashMapWithExpectedSize(serviceInterfaceTypes.size());
		for (Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> coreOrProxyType : coreOrProxyTypes) {
			boolean matched = false;
			for (Class<? extends ServiceInterface> serviceInterfaceType : serviceInterfaceTypes) {
				if (ReflectionUtils.isImplementing(coreOrProxyType,
												   serviceInterfaceType)) {
					// if already matched > error
					if (outServiceInterfaceMatchings.containsKey(serviceInterfaceType))	throw new IllegalStateException(String.format("The [service interface] %s is ALREADY implemented by %s and cannot be implemented by {}, " +
												   																					  "this is usually the case for BASE service interfaces; " +
												   																					  "annotate EXPOSED service interfaces with @{} and DO NOT annotate the BASE service interface",
																	  																  serviceInterfaceType,outServiceInterfaceMatchings.get(serviceInterfaceType),coreOrProxyType,
																	  																  ExposedServiceInterface.class.getSimpleName()));
					outServiceInterfaceMatchings.put(serviceInterfaceType,
											 		 coreOrProxyType);
					matched = true;
				}
			}
			if (strict && !matched) throw new IllegalStateException(String.format("[core impl] or [proxy] type %s does NOT match any [service interface]ª",
																				  coreOrProxyType));
		}
		
		if (log.isDebugEnabled()) {
			log.debug("... found {} matchings for [core impl] or [proxy] extending {}\n" + 
					  "{}",
					  outServiceInterfaceMatchings != null ? outServiceInterfaceMatchings.size() : 0,
					  coreImplOrProxyTypes,
					  outServiceInterfaceMatchings != null  
							  ? outServiceInterfaceMatchings.entrySet()
									  						.stream()
									  						.map(me -> Strings.customized("\t{} > {}",me.getKey(),me.getValue()))
									  						.collect(Collectors.joining("\n"))
							  : "\tNO matchings found!");
		}
		
		// [3] - Return
		return outServiceInterfaceMatchings;
	}
}
