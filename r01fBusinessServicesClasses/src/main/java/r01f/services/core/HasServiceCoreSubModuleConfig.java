package r01f.services.core;

import r01f.config.ContainsConfigData;

/**
 * Used to mark a sub-module (ie DBModule or SearhModule) that
 * contains the sub-module config which is going to be set
 * at the {@link ServicesCoreModuleConfig}
 * @param <C>
 */
public interface HasServiceCoreSubModuleConfig {
	public ServicesCoreSubModuleConfig<? extends ContainsConfigData> getSubModuleConfig();
}
