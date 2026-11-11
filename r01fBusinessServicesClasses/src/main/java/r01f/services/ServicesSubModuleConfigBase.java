package r01f.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.oids.CommonOIDs.IsAppComponent;

@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class ServicesSubModuleConfigBase<K extends IsAppComponent,C extends ContainsConfigData> { 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final K _component;
	@Getter private final C _config;	
}
