package r01f.services.core;

import java.util.Collection;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.config.ContainsConfigData;
import r01f.services.ServicesConfigObject;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.services.ids.ServiceIDs.CoreSubModule;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
public class ServicesCoreModuleConfig 
  implements ServicesConfigObject {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The core app code
	 */
	@Getter protected final CoreAppCode _coreAppCode;
	/**
	 * The core module
	 */
	@Getter protected final CoreModule _coreModule;
	/**
	 * Sub-modules config
	 */
	@Getter protected final Collection<ServicesCoreSubModuleConfig<?>> _subModulesCfgs;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public ServicesCoreModuleConfig(final CoreAppCode coreAppCode,final CoreModule coreModule) {
		_coreAppCode = coreAppCode;
		_coreModule = coreModule;
		_subModulesCfgs = null;
	}
	public ServicesCoreModuleConfig(final CoreAppCode coreAppCode,final CoreModule coreModule,
							  		final Collection<ServicesCoreSubModuleConfig<?>> subModulesCfgs) {
		_coreAppCode = coreAppCode;
		_coreModule = coreModule;
		_subModulesCfgs = subModulesCfgs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SUB-MODULE CONFIGS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public <CFG extends ContainsConfigData> CFG getSubModuleConfigFor(final CoreSubModule component) {
		if (CollectionUtils.isNullOrEmpty(_subModulesCfgs)) {
//			log.warn("NO sub-modules config was set for {}",component);
//			throw new IllegalStateException("NO sub-modules config was set for " + component);
			return null;
		}
		ServicesCoreSubModuleConfig<CFG> subCfg = (ServicesCoreSubModuleConfig<CFG>)_subModulesCfgs.stream()
																								   .filter(cfg -> cfg.getComponent().is(component))
																								   .findFirst().orElse(null);
		return subCfg != null ? subCfg.getConfig() : null;
	}
	@SuppressWarnings("unchecked")
	public <CFG extends ContainsConfigData> CFG getSubModuleConfigFor(final CoreSubModule component,final Class<CFG> cfgType) {
		if (CollectionUtils.isNullOrEmpty(_subModulesCfgs)) {			
			return null;
		}
		ServicesCoreSubModuleConfig<CFG> subCfg = (ServicesCoreSubModuleConfig<CFG>)_subModulesCfgs.stream()
																								   .filter(cfg -> cfg.getComponent().is(component) 
																										 	   && cfg.getConfig().getClass().equals(cfgType))
																								   .findFirst().orElse(null);
		return subCfg != null ? subCfg.getConfig() : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("services core config for appCode.appModule={}.{}",
								  _coreAppCode,_coreModule);
	}
}