package r01f.services.bootstrap.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.model.builders.BuilderBuildStep;
import r01f.patterns.IsBuilderStep;
import r01f.services.api.ClientAPI;
import r01f.services.core.IsCoreService;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.services.interfaces.IsProxyToCoreService;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.collections.CollectionUtils;

public class ServicesBootstrapConfigBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServicesBootstrapConfigBuilderClientAPITypeStep bootstrapConfigBuilderFor(final ClientApiAppCode clientApiAppCode) {
		return new ServicesBootstrapConfigBuilder() { /* nothing */ }
						.new ServicesBootstrapConfigBuilderClientAPITypeStep(clientApiAppCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CLIENT
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesBootstrapConfigBuilderClientAPITypeStep
	  implements IsBuilderStep {
		private final ClientApiAppCode _clientApiAppCode;

		public ServicesBootstrapConfigBuilderCoreModulesStep exposingClientApiType(final Class<? extends ClientAPI> clientApiType) {
			return new ServicesBootstrapConfigBuilderCoreModulesStep(_clientApiAppCode,clientApiType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesBootstrapConfigBuilderCoreModulesStep
	  implements IsBuilderStep {
		private final ClientApiAppCode _clientApiAppCode;
		private final Class<? extends ClientAPI> _clientApiType;

		public BuilderBuildStep<ServicesBootstrapConfig> withCoreModulesBootstrapConfig(final ServicesCoreModuleBootstrapConfig... coreModulesBootstrapConfig) {
			return this.withCoreModulesBootstrapConfig(CollectionUtils.hasData(coreModulesBootstrapConfig)
															? Arrays.asList(coreModulesBootstrapConfig)
															: Collections.emptyList());
		}
		public BuilderBuildStep<ServicesBootstrapConfig> withCoreModulesBootstrapConfig(final Collection<ServicesCoreModuleBootstrapConfig> coreModulesBootstrapConfig) {
			ServicesBootstrapConfig outCfg = new ServicesBootstrapConfig(_clientApiAppCode,
											   						     _clientApiType,
											   							 coreModulesBootstrapConfig);
			return new BuilderBuildStep<>(outCfg);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServiceCoreModuleBootstrapConfigBuilderServiceInterfacesStep coreModuleBootstrapConfigBuilderFor(final CoreAppCode coreAppCode,final CoreModule coreModule) {
		return new ServicesBootstrapConfigBuilder() { /* nothing */ }
						.new ServiceCoreModuleBootstrapConfigBuilderServiceInterfacesStep(coreAppCode,coreModule);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServiceCoreModuleBootstrapConfigBuilderServiceInterfacesStep
	  implements IsBuilderStep {
		private final CoreAppCode _coreAppCode;
		private final CoreModule _coreModule;

		public ServiceCoreModuleBootstrapConfigBuilderCoreImplOrProxyStep exposingServiceInterfacesExtending(final Class<? extends ServiceInterface>... serviceInterfacesBaseTypes) {
			return this.exposingServiceInterfacesExtending(CollectionUtils.hasData(serviceInterfacesBaseTypes)
																? new LinkedHashSet<>(Arrays.asList(serviceInterfacesBaseTypes))
																: Collections.emptySet());
		}
		public ServiceCoreModuleBootstrapConfigBuilderCoreImplOrProxyStep exposingServiceInterfacesExtending(final Set<Class<? extends ServiceInterface>> serviceInterfacesBaseTypes) {
			return new ServiceCoreModuleBootstrapConfigBuilderCoreImplOrProxyStep(_coreAppCode,
																				  _coreModule,
																				  serviceInterfacesBaseTypes);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServiceCoreModuleBootstrapConfigBuilderCoreImplOrProxyStep
	  implements IsBuilderStep {
		private final CoreAppCode _coreAppCode;
		private final CoreModule _coreModule;
		private final Set<Class<? extends ServiceInterface>> _serviceInterfacesBaseTypes;

		public BuilderBuildStep<ServicesCoreModuleBootstrapConfig> ofCoreImplOrProxyToCoreImpl(final ServicesCoreImplOrProxyToCoreImplConfig... coreImplOrProxyToCoreModuleConfigs) {
			return this.ofCoreImplOrProxyToCoreImpl(CollectionUtils.hasData(coreImplOrProxyToCoreModuleConfigs)
														? Arrays.asList(coreImplOrProxyToCoreModuleConfigs)
														: Collections.emptyList());
		}
		public BuilderBuildStep<ServicesCoreModuleBootstrapConfig> ofCoreImplOrProxyToCoreImpl(final Collection<ServicesCoreImplOrProxyToCoreImplConfig> coreImplOrProxyToCoreModuleConfigs) {
			ServicesCoreModuleBootstrapConfig outCfg = new ServicesCoreModuleBootstrapConfig(_coreAppCode,
																							 _coreModule,
																							 _serviceInterfacesBaseTypes,
																							 coreImplOrProxyToCoreModuleConfigs);
			return new BuilderBuildStep<>(outCfg);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CORE MODULE BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static CoreImplBaseTypesStep createServiceCoreModuleInProcess() {
		return new ServicesBootstrapConfigBuilder() { /* nothing */ }
						.new CoreImplBaseTypesStep();
	}
	public static RESTExposedCoreProxyBaseTypesStep createServiceCoreModuleRESTExposed() {
		return new ServicesBootstrapConfigBuilder() { /* nothing */ }
						.new RESTExposedCoreProxyBaseTypesStep();
	}
	public static ServletExposedCoreProxyBaseTypesStep createServiceCoreModuleServletExposed() {
		return new ServicesBootstrapConfigBuilder() { /* nothing */ }
						.new ServletExposedCoreProxyBaseTypesStep();
	}
	////////// --- In-Proccess
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CoreImplBaseTypesStep {
		@SuppressWarnings("unchecked")
		public BuilderBuildStep<ServicesCoreImplOrProxyToCoreImplConfig> withCoreServiceImplExtending(final Class<? extends IsCoreService>... coreServiceOrProxyToCoreServiceBaseTypes) {
			return this.withCoreServiceImplExtending(CollectionUtils.hasData(coreServiceOrProxyToCoreServiceBaseTypes)
															? new LinkedHashSet<>(Arrays.asList(coreServiceOrProxyToCoreServiceBaseTypes))
															: Collections.emptySet());
		}
		public BuilderBuildStep<ServicesCoreImplOrProxyToCoreImplConfig> withCoreServiceImplExtending(final Set<Class<? extends IsCoreService>> coreServiceOrProxyToCoreServiceBaseTypes) {
			ServicesCoreImplOrProxyToCoreImplConfig outCfg = ServicesCoreImplOrProxyToCoreImplConfig.createForCoreImplBaseTypes(coreServiceOrProxyToCoreServiceBaseTypes);
			return new BuilderBuildStep<>(outCfg);
		}
	}
	////////// --- REST
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class RESTExposedCoreProxyBaseTypesStep {
		@SuppressWarnings("unchecked")
		public BuilderBuildStep<ServicesCoreImplOrProxyToCoreImplConfig> withRESTExposedCoreServiceProxiesExtending(final Class<? extends IsProxyToCoreService>... coreServiceOrProxyToCoreServiceBaseTypes) {
			return this.withRESTExposedCoreServiceProxiesExtending(CollectionUtils.hasData(coreServiceOrProxyToCoreServiceBaseTypes)
																			? new LinkedHashSet<>(Arrays.asList(coreServiceOrProxyToCoreServiceBaseTypes))
																			: Collections.emptySet());
		}
		public BuilderBuildStep<ServicesCoreImplOrProxyToCoreImplConfig> withRESTExposedCoreServiceProxiesExtending(final Set<Class<? extends IsProxyToCoreService>> coreServiceOrProxyToCoreServiceBaseTypes) {
			ServicesCoreImplOrProxyToCoreImplConfig outCfg = ServicesCoreImplOrProxyToCoreImplConfig.createForRESTExposedCoreProxies(coreServiceOrProxyToCoreServiceBaseTypes);
			return new BuilderBuildStep<>(outCfg);
		}
	}
	////////// --- SERVLET
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServletExposedCoreProxyBaseTypesStep {
		@SuppressWarnings("unchecked")
		public BuilderBuildStep<ServicesCoreImplOrProxyToCoreImplConfig> withServletExposedCoreServiceProxiesExtending(final Class<? extends IsProxyToCoreService>... coreServiceOrProxyToCoreServiceBaseTypes) {
			return this.withServletExposedCoreServiceProxiesExtending(CollectionUtils.hasData(coreServiceOrProxyToCoreServiceBaseTypes)
																			? new LinkedHashSet<>(Arrays.asList(coreServiceOrProxyToCoreServiceBaseTypes))
																			: Collections.emptySet());
		}
		public BuilderBuildStep<ServicesCoreImplOrProxyToCoreImplConfig> withServletExposedCoreServiceProxiesExtending(final Set<Class<? extends IsProxyToCoreService>> coreServiceOrProxyToCoreServiceBaseTypes) {
			ServicesCoreImplOrProxyToCoreImplConfig outCfg = ServicesCoreImplOrProxyToCoreImplConfig.createForServletExposedCoreProxies(coreServiceOrProxyToCoreServiceBaseTypes);
			return new BuilderBuildStep<>(outCfg);
		}
	}
}
