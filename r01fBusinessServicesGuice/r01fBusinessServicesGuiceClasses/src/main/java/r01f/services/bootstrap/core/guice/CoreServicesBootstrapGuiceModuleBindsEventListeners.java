package r01f.services.bootstrap.core.guice;


import com.google.inject.Binder;

public interface CoreServicesBootstrapGuiceModuleBindsEventListeners {
	/**
	 * Binds the event listener
	 * @param binder
	 */
	public void bindEventListeners(final Binder binder);	
}
