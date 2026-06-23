package r01f.services.bootstrap.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.services.ServicesConfigObject;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
public class ServicesCoreModuleBootstrapConfig
  implements ServicesConfigObject {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The core app code
	 */
	@Getter private final CoreAppCode _coreAppCode;
	/**
	 * The Core module
	 */
	@Getter private final CoreModule _coreModule;
	/**
	 * The java package where the services interfaces can be found
	 */
	@Getter protected final Set<Class<? extends ServiceInterface>> _serviceInterfacesBaseTypes;
	/**
	 * How is this module exposed to the client API
	 */
	@Getter protected final Collection<ServicesCoreImplOrProxyToCoreModuleConfig> _coreImplOrProxyToCoreModuleConfigs;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreImplOrProxyToCoreModuleConfig getCoreImplOrProxyToCoreModuleConfigFor(final ServicesCoreImpl coreImplType) {
		return CollectionUtils.hasData(_coreImplOrProxyToCoreModuleConfigs)
					? _coreImplOrProxyToCoreModuleConfigs.stream()
														 .filter(cfg -> cfg.getCoreImplType() == coreImplType)
														 .findFirst()
														 .orElse(null)
					: null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("* [{}.{}] CORE module services config:\n" +
								  "\t- service interfaces extending {}\n" +
								  "\t- [core impl] or [proxy] to [core impl]:\n" +
								  "{}",
								  _coreAppCode,_coreModule,
								  _serviceInterfacesBaseTypes,
								  CollectionUtils.hasData(_coreImplOrProxyToCoreModuleConfigs) 
								  		? _coreImplOrProxyToCoreModuleConfigs.stream()
																		  .map(cfg -> Strings.customized("\t\t-{}",
																				   						 cfg.debugInfo()))
										  												     .collect(Collectors.joining("\n"))
										: "\t\t NO client proxy to core defined!");
	}
}
