package r01f.services.api;

import java.util.Collection;

import lombok.Getter;
import r01f.config.ContainsConfigData;
import r01f.oids.CommonOIDs.AppComponent;
import r01f.services.ServicesConfigObject;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

public class ServicesClientConfig 
  implements ServicesConfigObject {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The client app code
	 */
	@Getter protected final ClientApiAppCode _clientApiAppCode;
	/**
	 * any client sub-module config
	 */
	@Getter protected final Collection<ServicesClientSubModuleConfig<?>> _subModulesCfgs;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public ServicesClientConfig(final ClientApiAppCode clientApiAppCode,
							   	final Collection<ServicesClientSubModuleConfig<?>> subModulesCfgs) {
		_clientApiAppCode = clientApiAppCode;
		_subModulesCfgs = subModulesCfgs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SUB-MODULE CONFIGS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public <CFG extends ContainsConfigData> CFG getSubModuleConfigFor(final AppComponent component) {
		if (CollectionUtils.isNullOrEmpty(_subModulesCfgs)) throw new IllegalStateException("NO sub-modules config was set!");
		ServicesClientSubModuleConfig<?> subCfg = _subModulesCfgs.stream()
																 .filter(cfg -> cfg.getComponent().is(component))
																 .findFirst().orElse(null);
		return (CFG)(subCfg != null ? subCfg.getConfig() : null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("services client config for appCode={}",
								  _clientApiAppCode);
	}
}