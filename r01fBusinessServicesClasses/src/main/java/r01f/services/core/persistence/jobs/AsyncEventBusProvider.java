package r01f.services.core.persistence.jobs;

import java.util.concurrent.ExecutorService;

import jakarta.inject.Inject;
import r01f.concurrent.ExecutorServiceManager;
import r01f.eventbus.Event;
import r01f.eventbus.EventBus;
import r01f.eventbus.EventBusListener;
import r01f.eventbus.EventBusProviderBase;
import r01f.types.ExecutionMode;

public class AsyncEventBusProvider 
	 extends EventBusProviderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The jobs executor holder: manages a thread pool in charge of dispatching events
	 * In a web application environment (ie Tomcat), this thread pool MUST be destroyed
	 * when the servlet context is destroyed; to do so, the executor service manager is
	 * used in a {@link ServletContextListener}
	 * 
	 * ... so in a web app environment:
	 * 		This executor service manager MUST be binded at guice module with access to 
	 * 		the {@link ServletContext} (ie RESTJerseyServletGuiceModuleBase)
	 * 		
	 * 		This executor service manager is USED at a {@link ServletContextListener}'s destroy()
	 * 		method to kill the worker threads (ie R01VServletContextListener)
	 */
	private final ExecutorServiceManager _executorServiceManager;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public AsyncEventBusProvider(final ExecutorServiceManager execServiceManager) {
		super(ExecutionMode.ASYNC);
		_executorServiceManager = execServiceManager;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected EventBus _createEventBusInstance() {
		ExecutorService execService = _executorServiceManager.getExecutorService();
		// TODO make guava independent
		_eventBusInstance = new EventBus() {
								private final com.google.common.eventbus.EventBus _gEventBus = new com.google.common.eventbus.AsyncEventBus("R01 ASYNC EventBus",
								 		  	  							 																    execService);
								@Override
								public void post(final Event event) {
									_gEventBus.post(event);
								}
								@Override
								public void register(final EventBusListener listener) {
									_gEventBus.register(listener);
								}
							};
		return _eventBusInstance;
	}
}
