package r01f.services.core.events;

import org.slf4j.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.eventbus.Event;
import r01f.eventbus.EventBus;
import r01f.model.security.context.SecurityContext;
import r01f.reflection.ReflectionUtils;
import r01f.services.bootstrap.config.ServicesBootstrapConfig;
import r01f.services.core.callback.COREMethodCallback;
import r01f.services.core.callback.spec.COREServiceMethodBeanCallbackSpec;
import r01f.services.core.callback.spec.COREServiceMethodCallbackSpec;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecEvent;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;
import r01f.util.types.Strings;

/**
 * Listener to {@link IsCOREEvent}s thrown by through the {@link EventBus}
 * 
 * ========================================================
 * BEWARE!!!!!
 * ========================================================
 * If no event is handled ensure that:
 * [1] - The [event listener] is binded at a bootstrap module extending CoreServicesBootstrapGuiceModuleBindsEventListeners
 * 		 <pre class='brush:java'>
 * 	            @Override
 *	            public void bindEventListeners(final Binder binder) {
 *	            	binder.bind(MyCRUDOKEventListener.class)
 *	            		  .asEagerSingleton();	
 *	            }
 * 		 </pre>
 * [2] - The {@link ServicesBootstrapConfig} specifies how to handle events:
 * 		 <pre class='brush:java'>
 *		        ServicesBootstrapConfig bootCfg = ServicesBootstrapConfigBuilder.forClient(clientBootstrapCfg)
 *		        																.ofCoreModules(// core DB persistence
 *		        																			   dbPersistenceCoreBootstrapCfg,
 *		        																			   // UI
 *		        																			   uiBootstrapCfg)
 *		        																// event handling
 *		        																.coreEventsHandledSynchronously()
 *		        																.build();
 * 		 </pre>
 * @param <M>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class COREEventListenerBase<E extends Event>
           implements COREEventBusEventListener<E> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A filter is needed because guava's event bus does NOT supports generic event types
	 *  - Subscriptions to the event bus are done by event type, that's by {@link COREServiceMethodExecEvent} type
	 *  - BUT since guava {@link EventBus} does NOT supports generics, {@link COREServiceMethodExecEvent} cannot be generic
	 *  - ... so ALL listeners will be attached to the SAME event type: {@link COREServiceMethodExecEvent}
	 *  - ... and ALL listeners will receive {@link COREServiceMethodExecEvent} events
	 *  - ... but ONLY one should handle it.
	 * In order for the event handler (listener) to discriminate events to handle an {@link IsCOREEventFilter} is used
	 */
	protected final transient IsCOREEventFilter<E> _eventFilter;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public COREEventListenerBase(final IsCOREEventFilter<E> eventFilter) {
		_eventFilter = eventFilter;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEFAULT DEAD EVENT LISTENER
/////////////////////////////////////////////////////////////////////////////////////////
	@Subscribe
	public void handleDeadEvent(final DeadEvent deadEvent) {
		log.warn("> Not handled event:  {}",
				 deadEvent.getEvent());
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	CALLBACK SEND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sends a callback
	 * @param securityContext
	 * @param event
	 */
	public static void sendCallbackFor(final SecurityContext securityContext,
									   final COREServiceMethodExecEvent event) {
		if (event.getCallbackSpec() != null) {
			COREMethodCallback callback = _createCallbackInstance(event.getCallbackSpec());
			if (event.isForCOREMethodCallOK()) {
				callback.onCOREMethodCallOK(securityContext,
										    event.getCOREServiceMethodExecResult()
											     .asCOREServiceMethodExecOK());
			}
			else if (event.isForCOREMethodCallError()) {
				callback.onCOREMethodCallError(securityContext,
											   event.getCOREServiceMethodExecResult()
												    .asCOREServiceMethodExecError());
			}
		} else {
			log.warn("NO CALLBACK FOR CRUDOK!!!!!!");
		}
	}
	private static COREMethodCallback _createCallbackInstance(final COREServiceMethodCallbackSpec callbackSpec) {
		COREMethodCallback callback = null;
		if (callbackSpec instanceof COREServiceMethodBeanCallbackSpec) {
			COREServiceMethodBeanCallbackSpec beanCallbackSpec = (COREServiceMethodBeanCallbackSpec)callbackSpec;
			Class<? extends COREMethodCallback> callbackType = beanCallbackSpec.getImplType();
			callback = ReflectionUtils.createInstanceOf(callbackType);
		}
		else {
			throw new UnsupportedOperationException();
		}
		return callback;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _debugEvent(final Logger logger,
						  	   		  final COREServiceMethodExecOKEvent opOKEvent,final boolean hasToBeHandled) {
		if (logger.isTraceEnabled()) {
			logger.trace(_debugEvent(opOKEvent,
						  		  hasToBeHandled));
		} else if (logger.isDebugEnabled() && hasToBeHandled) {
			logger.debug(_debugEvent(opOKEvent,
						  		  hasToBeHandled));
		}
	}
	protected static String _debugEvent(final COREServiceMethodExecOKEvent opOKEvent,
							   	 		final boolean hasToBeHandled) {
		return Strings.customized("CRUDOKEventListener: handle the event of {}: {}",
							  	  opOKEvent.debugInfo(),
							  	  hasToBeHandled);
	}
}
