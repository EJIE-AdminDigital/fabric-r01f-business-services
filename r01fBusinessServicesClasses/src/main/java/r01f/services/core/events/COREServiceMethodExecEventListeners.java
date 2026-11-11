package r01f.services.core.events;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.eventbus.Event;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecErrorEvent;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;



/**
 * Event listeners
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class COREServiceMethodExecEventListeners {
/////////////////////////////////////////////////////////////////////////////////////////
//  Listens to OK PersistenceOperationOKEvent
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface COREServiceMethodExecOKEventListener
					extends COREEventBusEventListener<COREServiceMethodExecOKEvent> {
		public void onPersistenceOperationOK(final COREServiceMethodExecOKEvent opOKEvent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Listens to NOK PersistenceOperationErrorEvent
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface COREServiceMethodExecErrorEventListener<E extends Event>
					extends COREEventBusEventListener<COREServiceMethodExecErrorEvent> {
		public void onPersistenceOperationError(final COREServiceMethodExecErrorEvent opErrorEvent);
	}
}
