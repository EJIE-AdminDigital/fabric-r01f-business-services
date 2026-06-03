package r01f.services;

import org.checkerframework.checker.units.qual.K;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.guids.CommonOIDs.AppComponent;

@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class ServicesSubModuleConfigBase<K extends AppComponent,C extends ContainsConfigData> { 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final K _component;
	@Getter private final C _config;	
}
