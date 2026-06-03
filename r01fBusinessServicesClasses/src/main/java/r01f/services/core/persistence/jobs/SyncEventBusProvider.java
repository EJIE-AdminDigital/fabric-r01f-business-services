package r01f.services.core.persistence.jobs;

import jakarta.inject.Inject;
import r01f.eventbus.Event;
import r01f.eventbus.EventBus;
import r01f.eventbus.EventBusListener;
import r01f.eventbus.EventBusProviderBase;
import r01f.types.ExecutionMode;

public class SyncEventBusProvider 
	 extends EventBusProviderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public SyncEventBusProvider() {
		super(ExecutionMode.SYNC);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected EventBus _createEventBusInstance() {
		// TODO make guava independent
		_eventBusInstance = new EventBus() {
								private final com.google.common.eventbus.EventBus _gEventBus = new com.google.common.eventbus.EventBus("R01 SYNC EventBus");
								
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
