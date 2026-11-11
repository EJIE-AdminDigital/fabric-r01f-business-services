package r01f.services.core.events;

import r01f.eventbus.Event;
import r01f.eventbus.EventBusListener;

public interface COREEventBusEventListener<E extends Event>
	     extends EventBusListener<E> {
	// just a marker interface
}
