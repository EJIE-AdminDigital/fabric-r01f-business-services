package r01f.services.bootstrap.core.rest.guice;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.rest.RESTResourceConfigBootstrap;
import r01f.rest.RESTResourceConfigBase;
import r01f.services.bootstrap.guice.ServicesBootstrapGuiceUtil;


/**
 * Rest Resource referenced at {@link org.glassfish.jersey.server.ResourceConfig} (Jersey 2)
 * ... implemented extending {@link @RESTResourceConfigBase }
 * in order to bootstrap dependencies from Guice to HK2  :
 * <pre>
 *  Jersey  1 vs Jersey 2
 *  ----------------------------------------------------------------------------------------------------
 *  Jersey 1 :
 *   	Container
 *			\-> GuiceFilter (Guice: initialize request scope)
 *		  		\-> GuiceServletContextListener (Guice: initialize the Guice injector)
 *					\-> JerseyServletModule (Jersey: bind Jersey Rest Resources types to Guice module)
 *			  			\-> GuiceContainer (Jersey: redirect incoming requests to mapped resource classes)
 *  Jersey 2 :
 *	 	Container
 *			\-> ResourceConfig (r01f : see [@RESTResourceConfigBootstrapBase ) :
 *					1. Register REST Resources,  TypeMappers, ExceptionMappers, etc..
 *					2. Initialize Guice Injector.
 *					3. Initialize Bridge
 *					4. Register a ContainerLifecycleListener() to start/stop  guice services (jpa, etc..)
 *			  	\-> ServletContainer  (Jersey 2: redirect incoming requests to mapped resource classes)
 *  Notes :
 * 		- ResourceConfig extending RESTResourceConfigBootstrapBase should be registered into web.xml:
 *
 *				<filter>
 *					<filter-name>JerseyApplication</filter-name>
 *					<filter-class>org.glassfish.jersey.servlet.ServletContainer</filter-class>
 *					<init-param>
 *						<param-name>jakarta.ws.rs.Application</param-name>
 *						<param-value>ResourceConfig Class extending RESTResourceConfigBootstrapBase</param-value>
 *					</init-param>
 *			   </filter>
 *				<filter-mapping>
 *					<filter-name>JerseyApplication</filter-name>
 *					<url-pattern>/*</url-pattern>
 *			   </filter-mapping>
 *
 * 		- JerseyServletModule ( and GuiceFilter) can be used [ see  Jersey2 link [ @, but don't use GuiceServletContextListener ( this was used to create Injector in Jersey 1)
 * </pre>
 */
@Slf4j
@Singleton
public abstract class RESTResourceConfigBootstrapBase
	  		  extends RESTResourceConfigBase
	  	   implements RESTResourceConfigBootstrap {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final ServiceLocator _serviceLocator;
	protected final Injector _injector;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR !! ServiceLocator must be Injected!!
/////////////////////////////////////////////////////////////////////////////////////////

	public RESTResourceConfigBootstrapBase(final ServiceLocator serviceLocator) {
		 super();
		 
		 // 1. HK2 Service Locator
		 _serviceLocator = serviceLocator;
		 // 2. Guice InJector
		 _injector =  _createInjector();
		 // 2. Init Guice Into HK2 Brige
		 _initGuiceIntoHK2Bridge(serviceLocator,_injector);
		 
		  // After ResourceConfigBootstrapGuiceHK2Delegate bridge, create a ContainerLifecycleListener to Start Guice Services.Do this order.
		 this.register(new ContainerLifecycleListener() {
								@Override
								public void onStartup(final Container container) {
									// Init JPA's Persistence Service, Lucene indexes , Hazelcast , etc..and everything that has to be started
									// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
									log.warn("######################################################################################");
									log.warn(" [] ContainerLifecycleListener onStartup (ServicesBootstrapUtil.startServices)");
									log.warn("######################################################################################");
									ServicesBootstrapGuiceUtil.startServices(_injector);
								}
								@Override
								public void onReload(final Container container) {
									// On Reload.
								}
								@Override
								public void onShutdown(final Container container) {
									log.warn("######################################################################################");
									log.warn(" [] ContainerLifecycleListener onShutdown (ServicesBootstrapUtil.stopServices)");
									log.warn("######################################################################################");
									ServicesBootstrapGuiceUtil.stopServices(_injector);
	
								}
				  	  });
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROTECTED METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _initGuiceIntoHK2Bridge(final ServiceLocator serviceLocator, 
												final Injector injector) {
		GuiceBridge.getGuiceBridge()
				   .initializeGuiceBridge(serviceLocator);
		GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		guiceBridge.bridgeGuiceInjector(injector);
	}
	protected Injector _createInjector() {
		return Guice.createInjector(_createGuiceModules());
	}
	protected abstract Iterable<? extends Module> _createGuiceModules();
}