package r01f.services.bootstrap.guice;

import com.google.inject.Binder;
import com.google.inject.Injector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.bootstrap.services.ServiceHandlerLifecycle;
import r01f.service.ServiceHandler;

/**
 * Utility type that encapsulates the services life cycle operations
 * <ul>
 * 	<li>Guice injector creation</li>
 * 	<li>Start / Stop of services that needs an explicit starting (ie Persistence services, thread pools, indexers, etc)</li>
 * </ul>
 *
 * This type is mainly used at:
 * <ul>
 * 	<li>ServletContextListeners of web apps that controls the lifecycle of the app</li>
 * 	<li>Test init classes</li>
 * </ul>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ServicesBootstrapGuiceUtil {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Starts services that needs to be started
	 * @param hasServiceHandlerTypes
	 * @param injector
	 */
	public static void startServices(final Injector injector) {
		ServiceHandlerLifecycle.startServices(injector);
	}
	/**
	 * Stops services that needs to be started
	 * @param hasServiceHandlerTypes
	 * @param injector
	 */
	public static void stopServices(final Injector injector) {
		ServiceHandlerLifecycle.stopServices(injector);
	}
	/**
	 * Binds a service handler type and exposes it if it's a private binder
	 * @param binder
	 * @param serviceHandlerType
	 * @param name
	 */
	public static void bindServiceHandler(final Binder binder,
										  final Class<? extends ServiceHandler> serviceHandlerType,final String name) {
		ServiceHandlerLifecycle.bindServiceHandler(binder,
												   serviceHandlerType,name);
	}
}
