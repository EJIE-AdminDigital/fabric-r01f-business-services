package r01f.services.bootstrap.core.guice;

import com.google.inject.PrivateBinder;
import com.google.inject.PrivateModule;

/**
 * Each {@link ServicesBootstrapCoreGuiceModuleBase}s is installed in it's own guice's {@link PrivateModule} (see {@link ServicesBootstrapGuiceModulesFactory} and {@link ServicesBootstrapCorePrivateGuiceModule})
 * ... so any binding inside the {@link ServicesBootstrapCoreGuiceModuleBase} is NOT VISIBLE from the outside world.
 * During tests sometimes an internal binding is needed so a way to allow a {@link ServicesBootstrapCoreGuiceModuleBase} expose some of it's internal bindings
 * is let the module implement this interface
 */
public interface ServicesBootstrapCoreGuiceModuleExposesBindings {
	/**
	 * Provides the {@link ServicesBootstrapCoreGuiceModuleBase} with the {@link PrivateBinder} to give him
	 * the oportunity to expose something to the outside world
	 * @param privateBinder
	 */
	public void exposeBindings(final PrivateBinder privateBinder);
}
