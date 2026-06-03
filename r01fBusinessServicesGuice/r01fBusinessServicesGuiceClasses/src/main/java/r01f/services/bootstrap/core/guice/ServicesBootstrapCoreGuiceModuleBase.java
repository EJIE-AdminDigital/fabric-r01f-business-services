package r01f.services.bootstrap.core.guice;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.eventbus.EventBus;
import r01f.eventbus.EventBusListener;
import r01f.inject.HasMoreBindings;
import r01f.inject.Matchers;
import r01f.reflection.ReflectionUtils;
import r01f.services.core.HasServiceCoreSubModuleConfig;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.ServicesCoreSubModuleConfig;
import r01f.services.core.annotations.ServicesCoreConfigForImpl;
import r01f.services.core.events.COREEventBusEventListener;
import r01f.services.core.events.COREServiceMethodExecEventListeners.COREServiceMethodExecErrorEventListener;
import r01f.services.core.events.COREServiceMethodExecEventListeners.COREServiceMethodExecOKEventListener;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
public abstract class ServicesBootstrapCoreGuiceModuleBase 
  		   implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
// 	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Code app code
	 */
	@Getter protected final CoreAppCode _coreAppCode;
	/**
	 * Core module
	 */
	@Getter protected final CoreModule _coreModule;
	/**
	 * Has to be installed as a private module
	 */
	@Getter protected final boolean _isolated;
	/**
	 * The installed modules
	 */
	protected final Collection<Module> _installedModules;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesBootstrapCoreGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
												final boolean isolated) {
		_coreAppCode = coreAppCode;
		_coreModule = coreModule;
		
		_isolated = isolated;
		
		_installedModules = Lists.newArrayList();
	}
	protected ServicesBootstrapCoreGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
												   final boolean isolated,
											   	   final Collection<? extends Module> otherModules) {
		this(coreAppCode,coreModule,
			 isolated);
		// modules to install
		if (CollectionUtils.hasData(otherModules)) otherModules.forEach(m -> _installedModules.add(m));
	}
	protected ServicesBootstrapCoreGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
												   final boolean isolated,
												   final Module module,
											   	   final Collection<? extends Module> otherModules) {
		this(coreAppCode,coreModule,
			 isolated);
		// modules to install
		if (module != null) _installedModules.add(module);
		if (CollectionUtils.hasData(otherModules)) otherModules.forEach(m -> _installedModules.add(m));		
	}
	protected ServicesBootstrapCoreGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
												   final boolean isolated,
												   final Module module1,final Module module2,
											   	   final Collection<? extends Module> otherModules) {
		this(coreAppCode,coreModule,
			 isolated);
		// modules to install
		if (module1 != null) _installedModules.add(module1);
		if (module2 != null) _installedModules.add(module2);
		if (CollectionUtils.hasData(otherModules)) otherModules.forEach(m -> _installedModules.add(m));		
	}
	protected ServicesBootstrapCoreGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
												   final boolean isolated,
											   	   final Module... otherModules) {
		this(coreAppCode,coreModule,
			 isolated,
			 CollectionUtils.hasData(otherModules) ? Arrays.asList(otherModules) : Collections.emptyList());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		Binder theBinder = binder; 
		
		// [1]: Install Modules & bind specific properties
		if (CollectionUtils.hasData(_installedModules)) { 
			_installedModules.forEach(m -> {
											log.warn("Install {} guice module",
													 m.getClass());
											theBinder.install(m);
									  });
		}
		// [2]: Bind ServicesCoreModuleConfig
		//		Every installed module that implements HasServiceCoreSubModuleConfig can "contribute" with a sub-module config
		//		if the DBModule
		Collection<ServicesCoreSubModuleConfig<?>> subModCfgs = CollectionUtils.hasData(_installedModules)
																	? _installedModules.stream()
																				 	   .filter(m -> m instanceof HasServiceCoreSubModuleConfig)
																				 	   .map(m -> ((HasServiceCoreSubModuleConfig)m).getSubModuleConfig())
																				 	   .collect(Collectors.toList())
																	: null;
		if (CollectionUtils.hasData(subModCfgs)) {
			ServicesCoreModuleConfig coreModuleConfig = new ServicesCoreModuleConfig(_coreAppCode,_coreModule,
																					 subModCfgs);
			// a) bind the instance
			binder.bind(ServicesCoreModuleConfig.class)
				  .toInstance(coreModuleConfig);
			
			// b) also bind annotated
			String bindingId = String.format("%s.%s",
											 _coreAppCode,_coreModule);
			binder.bind(ServicesCoreModuleConfig.class)
				  .annotatedWith(new ServicesCoreConfigForImpl(bindingId))
				  .toInstance(coreModuleConfig);
			binder.bind(ServicesCoreModuleConfig.class)
				  .annotatedWith(Names.named(bindingId))
				  .toInstance(coreModuleConfig);			
		}
		
		// [3]: Other bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(theBinder);
		}
	}
//////////////////////////////////////////////////////////////////////////////////
//  ServicesCoreBootstrapGuiceModule
/////////////////////////////////////////////////////////////////////////////////////////
	public Collection<Class<? extends Module>> getInstalledModuleTypes() {
		return _installedModules.stream()
								.map(// do NOT convert to lambda expression
								 	 new Function<Module,Class<? extends Module>>() {
								 		 	private boolean _nothing;	// an unused var to disable lambda conversion
								 		 	
											@Override
											public Class<? extends Module> apply(final Module mod) {
												return mod.getClass();
											}
						 			 })
								.toList();
	}
	public boolean isModuleInstalled(final Class<? extends Module> modType) {
		if (CollectionUtils.isNullOrEmpty(_installedModules)) return false;
		
		return this.getInstalledModuleTypes()
				   .stream()
				   .anyMatch(mT -> ReflectionUtils.isSubClassOf(mT,modType));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EVENTS
/////////////////////////////////////////////////////////////////////////////////////////	
	protected void _bindEventListeners(final Binder theBinder) {
		if (!(this instanceof CoreServicesBootstrapGuiceModuleBindsEventListeners)) return;
		
		// [1] - Automatic registering of [event listeners] to the [event bus] avoiding the manual registering of every listener;
		// 		 this code simply LISTEN for guice's binding events: when an [event listener] extending  gets binded, it's is automatically registered at the [event bus]
		Provider<EventBus> eventBusProvider = theBinder.getProvider(EventBus.class);
		EventBusSubscriberTypeListener typeListener = new EventBusSubscriberTypeListener(eventBusProvider);	// inject an event bus provider !!!
		theBinder.bindListener(Matchers.subclassesOf(COREEventBusEventListener.class),		// listen for injection of COREEventBusEventListener subtypes
						       typeListener);												// ... when a COREEventBusEventListener subtype is binded by guice
																							//	   the typeLister registers the [event listeners] at the EventBus
		
		// [2] - bind the concrete [event listeners]
		//		 when this [event listers] are binded, they are automatically registered at the [event bus] by the previous code
		((CoreServicesBootstrapGuiceModuleBindsEventListeners)this).bindEventListeners(theBinder);
	}
	/**
	 * Guice {@link TypeListener} that gets called when a {@link COREEventBusEventListener} subtype
	 * is injected (or created) (this is called ONCE per type)
	 *
	 * ... so a {@link COREEventBusEventListener} subtype (ie  {@link COREServiceMethodExecOKEventListener} or {@link COREServiceMethodExecErrorEventListener})
	 * is AUTOMATICALLY registered as listener at the {@link EventBus}
	 */
	@RequiredArgsConstructor
	protected class EventBusSubscriberTypeListener
	     implements TypeListener {

		// The EventBus cannot be injected because it cannot be created inside a module
		// however an EventBus provider can be injected and in turn it's injected with
		// it's dependencies
		// see r01f.persistence.jobs.EventBusProvider
		private final Provider<EventBus> _eventBusProvider;

		@Override
		public <I> void hear(final TypeLiteral<I> type,
							 final TypeEncounter<I> encounter) {
			encounter.register(// AFTER the type is injected it MUST be registered at the EventBus
							   new InjectionListener<I>() {
										@Override
										public void afterInjection(final I injecteeEventListener) {
											log.info("\tRegistering {} event listener at event bus {}",
													 injecteeEventListener.getClass(),
													 _eventBusProvider.get());
											_eventBusProvider.get()
													 		 .register((EventBusListener)injecteeEventListener);	// register the indexer (the indexer is an event listener)
										}
							   });
		}
	}
}
