package r01f.services.bootstrap.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.services.core.IsCoreService;
import r01f.services.interfaces.IsProxyToCoreService;
import r01f.util.types.Strings;

/**
 * Models how the CLIENT access the CORE impl
 * 	- The core impl type: IN-PROCESS / REST / SERVLET / ...
 * 	- The [core service impl] or [proxy to core service impl] base types 
 * 		- if core impl type = IN-PROCESS the base types extends {@link IsCoreService}
 * 		- if core impl type = REST / SERVLET the base types extends {@link IsProxyToCoreService}
 */
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
public class ServicesCoreImplOrProxyToCoreModuleConfig
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final ServicesCoreImpl _coreImplType;
	@Getter private final Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> _coreServiceOrProxyToCoreServiceBaseTypes;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServicesCoreImplOrProxyToCoreModuleConfig create(final ServicesCoreImpl coreImplType,
												  				   final Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> coreServiceOrProxyToCoreServiceBaseTypes) {
		return new ServicesCoreImplOrProxyToCoreModuleConfig(coreImplType,coreServiceOrProxyToCoreServiceBaseTypes);
	}
	public static ServicesCoreImplOrProxyToCoreModuleConfig create(final ServicesCoreImpl coreImplType,
												  				   final Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> ... coreServiceOrProxyToCoreServiceBaseTypes) {
		return new ServicesCoreImplOrProxyToCoreModuleConfig(coreImplType,new LinkedHashSet(Arrays.asList(coreServiceOrProxyToCoreServiceBaseTypes)));
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
