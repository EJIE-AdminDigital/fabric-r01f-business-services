package r01f.services.core.events;

import r01f.eventbus.EventBus;

/**
 * Interface for types that contains an {@link EventBus}
 */
public interface HasCOREEventBus {
	/**
	 * @return the {@link EventBus} instance
	 */
	public EventBus getEventBus();
}
