package r01f.services.core;

import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.services.ServicesSubModuleConfigBase;
import r01f.services.ids.ServiceIDs.CoreSubModule;

@Accessors(prefix="_")
public class ServicesCoreSubModuleConfig<C extends ContainsConfigData>
	 extends ServicesSubModuleConfigBase<CoreSubModule,C> { 
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreSubModuleConfig(final CoreSubModule component,
									   final C config) {
		super(component,config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createFor(final CoreSubModule component,final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(component,cfg);
	}
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createForDBPersistenceSubModule(final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(CoreSubModule.DBPERSISTENCE,cfg);
	}	
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createForSearchPersistenceSubModule(final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(CoreSubModule.SEARCHPERSISTENCE,cfg);
	}
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createForFSPersistenceSubModule(final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(CoreSubModule.FSPERSISTENCE,cfg);
	}
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createForNotifierSubModule(final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(CoreSubModule.NOTIFIER,cfg);
	}
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createForRenderSubModule(final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(CoreSubModule.RENDER,cfg);
	}
	public static <CFG extends ContainsConfigData> ServicesCoreSubModuleConfig<CFG> createForSyndicationSubModule(final CFG cfg) {
		return new ServicesCoreSubModuleConfig<>(CoreSubModule.SYNDICATION,cfg);
	}
}
