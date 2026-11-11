package r01f.services.bootstrap.config;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.services.core.IsCoreService;
import r01f.services.interfaces.IsProxyToCoreService;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Models how the CLIENT access the CORE impl
 * 	- The core impl type: IN-PROCESS / REST / SERVLET / ...
 * 	- The [core service impl] or [proxy to core service impl] base types 
 * 		- if core impl type = IN-PROCESS the base types extends {@link IsCoreService}
 * 		- if core impl type = REST / SERVLET the base types extends {@link IsProxyToCoreService}
 */
@Accessors(prefix="_")
public class ServicesCoreImplOrProxyToCoreImplConfig
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final ServicesCoreImpl _coreImplType;
	@Getter private final Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> _coreServiceOrProxyToCoreServiceBaseTypes;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreImplOrProxyToCoreImplConfig(final ServicesCoreImpl coreImplType,
												   final Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> coreServiceOrProxyToCoreServiceBaseTypes) {
		// check that the core service base types are compatible with the core impl type
		boolean allOK = CollectionUtils.hasData(coreServiceOrProxyToCoreServiceBaseTypes)
							? coreServiceOrProxyToCoreServiceBaseTypes.stream()
																	  .allMatch(coreSvcType -> coreImplType.isAllowedBaseType(coreSvcType))
							: true;
		if (!allOK) throw new IllegalArgumentException(Strings.customized("Core impl type {} is not compatible with the [core service] or proxy to [core service] base types {}",
																		  coreImplType,
																		  coreServiceOrProxyToCoreServiceBaseTypes));
		_coreImplType = coreImplType;
		_coreServiceOrProxyToCoreServiceBaseTypes = coreServiceOrProxyToCoreServiceBaseTypes;
	}
	public static ServicesCoreImplOrProxyToCoreImplConfig createForCoreImplBaseTypes(final Set<Class<? extends IsCoreService>> coreServiceBaseTypes) {
		return new ServicesCoreImplOrProxyToCoreImplConfig(ServicesCoreImpl.IN_PROCESS,
														   // ugly cast wtf!
														   CollectionUtils.hasData(coreServiceBaseTypes)
														   		? coreServiceBaseTypes.stream()
														   						  	  .map(coreSvcType -> (Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>)coreSvcType)
														   						  	  .collect(Collectors.toCollection(LinkedHashSet::new))
														   		: new LinkedHashSet<>());
	}
	public static ServicesCoreImplOrProxyToCoreImplConfig createForRESTExposedCoreProxies(final Set<Class<? extends IsProxyToCoreService>> coreServiceProxyBaseTypes) {
		return _createForCoreProxies(ServicesCoreImpl.REST,
									 coreServiceProxyBaseTypes);
	}
	public static ServicesCoreImplOrProxyToCoreImplConfig createForServletExposedCoreProxies(final Set<Class<? extends IsProxyToCoreService>> coreServiceProxyBaseTypes) {
		return _createForCoreProxies(ServicesCoreImpl.SERVLET,
									 coreServiceProxyBaseTypes);
	}
	private static ServicesCoreImplOrProxyToCoreImplConfig _createForCoreProxies(final ServicesCoreImpl coreImplType,
																				 final Set<Class<? extends IsProxyToCoreService>> coreServiceProxyBaseTypes) {
		if (coreImplType == ServicesCoreImpl.IN_PROCESS) throw new IllegalArgumentException(Strings.customized("Core impl type {} is IN_PROCESS but the core service base types are proxies to core service impls",
																											   coreImplType));
		return new ServicesCoreImplOrProxyToCoreImplConfig(coreImplType,
														   // ugly cast wtf!
														   CollectionUtils.hasData(coreServiceProxyBaseTypes)
														   		? coreServiceProxyBaseTypes.stream()
														   								   .map(coreSvcType -> (Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>)coreSvcType)
														   								   .collect(Collectors.toCollection(LinkedHashSet::new))
														   		: new LinkedHashSet<>());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("{} proxy to core exposed with types extending {}",
								  _coreImplType,_coreServiceOrProxyToCoreServiceBaseTypes);
	}
}
