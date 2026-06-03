package r01f.services.core.events.crud;

import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;

@FunctionalInterface
public interface CRUDOKEventFilter {
	public boolean hasTobeHandled(COREServiceMethodExecOKEvent opEvent);
}
