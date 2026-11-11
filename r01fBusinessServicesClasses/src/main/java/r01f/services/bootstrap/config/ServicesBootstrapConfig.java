package r01f.services.bootstrap.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.reflection.outline.TypeOutline;
import r01f.services.ServicesConfigObject;
import r01f.services.api.ClientAPI;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.interfaces.ServiceInterface;
import r01f.types.JavaPackage;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
public class ServicesBootstrapConfig 
  implements ServicesConfigObject {

/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The client API app code
	 */
	@Getter protected final ClientApiAppCode _clientApiAppCode;
	/**
	 * The client API type (ie: the class that aggregates the service interfaces access methods)
	 */
	@Getter protected final Class<? extends ClientAPI> _clientApiType;
	/**
	 * Core modules bootstrap config
	 */
	@Getter protected final Collection<ServicesCoreModuleBootstrapConfig> _coreModulesBootstrapConfig;

/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("Services Bootstrap config for CLIENT appCode={} exposing API {}\n" +
								  "Core modules bootstrap configs:\n" +
								  "------------------------------" + 
								  "{}",
								  _clientApiAppCode,_clientApiType.getSimpleName(),
								  CollectionUtils.hasData(_coreModulesBootstrapConfig) 
								  		? _coreModulesBootstrapConfig.stream()
																	 .map(cfg -> cfg.debugInfo())
										  							 .collect(Collectors.joining("\n"))
										: "NO core module defined!");	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	UTIL
/////////////////////////////////////////////////////////////////////////////////////////
	public static Set<JavaPackage> getJavaPackagesToScanFor(final Collection<ServicesBootstrapConfig> bootstrapCfgs) {
		if (CollectionUtils.isNullOrEmpty(bootstrapCfgs)) return Collections.emptySet();
		return bootstrapCfgs.stream()
							.flatMap(bootstrapCfg -> ServicesBootstrapConfig.getJavaPackagesToScanFor(bootstrapCfg)
																			.stream())
							.collect(Collectors.toUnmodifiableSet());
	}
	public static Set<JavaPackage> getJavaPackagesToScanFor(final ServicesBootstrapConfig bootstrapCfg) {
		if (CollectionUtils.isNullOrEmpty(bootstrapCfg.getCoreModulesBootstrapConfig())) return Collections.emptySet();
		
		return bootstrapCfg.getCoreModulesBootstrapConfig()
						   .stream()
						   .flatMap(modBootCfg -> ServicesBootstrapConfig.getJavaPackagesToScanFor(modBootCfg)
								   										 .stream())
						   .collect(Collectors.toCollection(LinkedHashSet::new));
	}
	public static Set<JavaPackage> getJavaPackagesToScanFor(final ServicesCoreModuleBootstrapConfig modBootCfg) {
		if (modBootCfg == null) return Collections.emptySet();

		Set<JavaPackage> outJavaPackages = new LinkedHashSet<>();

		// [1] - java packages where to look for [service interface]-extending interfaces
		// a) get the [service interfaces] base interface (a super-interface for every [service interface])
		Collection<Class<? extends ServiceInterface>> serviceInterfaceBaseTypes = modBootCfg.getServiceInterfacesBaseTypes();
		
		// b) get the packages where to look for [service interfaces] extending the [base interface]
		// 	  BEWARE!! 	all the [service interfaces] extending the [base interface] MUST be in the same package or a sub-package
		//				as the [base interface]
		if (CollectionUtils.hasData(serviceInterfaceBaseTypes))
			serviceInterfaceBaseTypes.stream()
									 .forEach(srvcIfaceBaseType -> {
										 			Set<JavaPackage> javaPackagesContainingSrvcIface = _packagesWhereToLookForTypesExtending(srvcIfaceBaseType);
										 			outJavaPackages.addAll(javaPackagesContainingSrvcIface);
									 		  });
		
		// [2] - java packages where to look for [core service impl] or [proxy to core service impl] types
		Collection<ServicesCoreImplOrProxyToCoreImplConfig> coreImplOrProxyCfgs = modBootCfg.getCoreImplOrProxyToCoreModuleConfigs();
		
		for (ServicesCoreImplOrProxyToCoreImplConfig coreImplOrProxyCfg : coreImplOrProxyCfgs) {
			// a) get the [core impl] or [proxy] base type (a super-type for every [core impl] or [proxy])
			Collection<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> coreImplOrProxyBaseTypes = coreImplOrProxyCfg.getCoreServiceOrProxyToCoreServiceBaseTypes();
	
			// b) get the packages where to look for [core impls] or [proxies] extending the base [core impl] or [proxy]
			// 	  BEWARE!! 	all the [core impls] or [proxies] extending the base [core impl] or [proxy] MUST be in the SAME package or a sub-package
			//				as the base [core impl] or [proxy]
			if (CollectionUtils.hasData(coreImplOrProxyBaseTypes)) {
				coreImplOrProxyBaseTypes.stream()
									    .forEach(coreImplOrProxyBaseType -> {
													Set<JavaPackage> javaPackagesContainingSrvcIface = _packagesWhereToLookForTypesExtending(coreImplOrProxyBaseType);
													outJavaPackages.addAll(javaPackagesContainingSrvcIface);
											   });
			}
		}
		// [99] - return
		return outJavaPackages;
		
	}
	static Set<JavaPackage> _packagesWhereToLookForTypesExtending(final Class<?> type) {
		// BEWARE!
		// org.reflections is used to scan subtypes of CoreService. This library requires
		// ALL the packages in the type hierarchy to be given to the scan methods:
		// <pre class='brush:java'>
		//  		CoreService
		//  			|-- interface 1
		//  					|--  interface 2
		//  							|-- all the core service impl
		// </pre>
		// The packages where CoreService, interface 1 and interface 2 resides MUST be handed
		// to the subtypeOfScan method of org.reflections

		// find the hierarchy between the given interface and CoreService
		TypeOutline typeOutline = new TypeOutline(type);
		if (log.isDebugEnabled()) log.debug("{} type hierarchy outline:\n{}",
			     						    type,typeOutline.debugInfo());
		return typeOutline.getNodesFromGeneralToSpezialized()
						  .stream()
						  .map(JavaPackage::of)
						  .filter(pckg -> !pckg.isJavaLang())
						  .collect(Collectors.toUnmodifiableSet());
	}	
}
