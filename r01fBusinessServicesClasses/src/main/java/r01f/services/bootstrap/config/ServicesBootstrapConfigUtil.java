package r01f.services.bootstrap.config;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.services.api.ClientAPI;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.util.types.Strings;

@Slf4j
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ServicesBootstrapConfigUtil {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public static ServicesBootstrapConfigUtilForCollection of(final Collection<ServicesBootstrapConfig> bootstrapCfgs) {
		return new ServicesBootstrapConfigUtil() { /* nothing */ }
						.new ServicesBootstrapConfigUtilForCollection(bootstrapCfgs);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesBootstrapConfigUtilForCollection {
		private final Collection<ServicesBootstrapConfig> _bootstrapCfgs;
		
		/**
		 * Groups {@link ServicesBootstrapConfig} objects by client api appCode
		 * @param bootstrapCfgs
		 * @return
		 */
		public Map<ClientApiAppCode,Collection<ServicesBootstrapConfig>> getBootstrapConfigsGroupedByClientApiAppCode() {
			Map<ClientApiAppCode,Collection<ServicesBootstrapConfig>> outMap = Maps.newHashMap();
			for (ServicesBootstrapConfig bootstrapCfg  : _bootstrapCfgs) {
				Collection<ServicesBootstrapConfig> cfgs = outMap.get(bootstrapCfg.getClientApiAppCode());
				if (cfgs != null) {
					cfgs.add(bootstrapCfg);
				} else {
					cfgs = Lists.newArrayList();
					cfgs.add(bootstrapCfg);
					outMap.put(bootstrapCfg.getClientApiAppCode(),
							   cfgs);
				}
			}
			return outMap;
		}
		/**
		 * Finds the {@link ServicesBootstrapConfig} for a given client api app code and api type
		 * @param apiAppCode
		 * @param apiType
		 * @return
		 */
		public ServicesBootstrapConfig getBootstrapConfigFor(final ClientApiAppCode apiAppCode,final Class<? extends ClientAPI> apiType) {
			return _bootstrapCfgs.stream()
								 // same api appCode && same api type
							     .filter(cfg -> apiAppCode.is(cfg.getClientApiAppCode())
							    		     && apiType == cfg.getClientApiType())		
							     .findFirst()
							     .orElseThrow(() -> {
							    				log.error("No suitable bootstrap config found for client api appCode={} and api type={}\n" +
							    						  "Configured bootstrap configs are:\n" +
							    						  "{}",
							    						  apiAppCode,apiType.getSimpleName(),
							    						  _bootstrapCfgs.stream()
							    						  				.map(cfg -> Strings.customized("\t-{} - {}",cfg.getClientApiAppCode(),cfg.getClientApiType().getSimpleName()))
							    						  				.collect(Collectors.joining("\n")));
							    				IllegalStateException outEx = new IllegalStateException(Strings.customized("No client bootstrap config found for client api appCode={} and api type={}",
							    												  				    					   apiAppCode,apiType.getSimpleName()));
							    				return outEx;
							    		    });
		}
		public ServicesCoreModuleBootstrapConfig getCoreModuleBootstrapConfigFor(final CoreAppCode coreAppCode,final CoreModule coreModule) {
			return _bootstrapCfgs.stream()
								 // filter bootstrap configs without core module bootstrap configs
								 .filter(cfg -> cfg.getCoreModulesBootstrapConfig() != null)
								 // flat map all [core module] bootstrap configs
								 .flatMap(cfg -> cfg.getCoreModulesBootstrapConfig()
												    .stream()) 
								// same api appCode && same api type
						        .filter(modCfg -> coreAppCode.is(modCfg.getCoreAppCode())
						        		       && coreModule == modCfg.getCoreModule())
						        .findFirst()
							    .orElseThrow(() -> {
							    				log.error("No [core module] bootstrap config found for core appCode={} and module={}\n" +
							    						  "Configured [core module] bootstrap configs are:\n" +
							    						  "{}",
							    						  coreAppCode,coreModule,
							    						  _bootstrapCfgs.stream()
																		.filter(cfg -> cfg.getCoreModulesBootstrapConfig() != null)
																		.flatMap(cfg -> cfg.getCoreModulesBootstrapConfig()
												   										   .stream())
																		.map(modCfg -> Strings.customized("\t-{} - {}",modCfg.getCoreAppCode(),modCfg.getCoreModule()))
																		.collect(Collectors.joining("\n")));
							    				IllegalStateException outEx = new IllegalStateException(Strings.customized("No [core module] bootstrap config found for core appCode={} and module={}",
							    												  				    					   coreAppCode,coreModule));
							    				return outEx;
							    		    });
		}
	}
}
