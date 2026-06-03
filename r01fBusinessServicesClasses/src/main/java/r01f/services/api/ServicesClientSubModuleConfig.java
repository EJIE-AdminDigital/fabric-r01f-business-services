package r01f.services.api;

import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.services.ServicesSubModuleConfigBase;
import r01f.services.ids.ServiceIDs.ClientApiComponent;

@Accessors(prefix="_")
public class ServicesClientSubModuleConfig<C extends ContainsConfigData>
	 extends ServicesSubModuleConfigBase<ClientApiComponent,C> { 
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesClientSubModuleConfig(final ClientApiComponent component,
										 final C config) {
		super(component,config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static <CFG extends ContainsConfigData> ServicesClientSubModuleConfig<CFG> createFor(final ClientApiComponent component,final CFG cfg) {
		return new ServicesClientSubModuleConfig<>(component,cfg);
	}
}
