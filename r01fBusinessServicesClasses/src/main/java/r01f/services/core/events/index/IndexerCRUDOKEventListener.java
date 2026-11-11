package r01f.services.core.events.index;

 

import r01f.model.PersistableModelObject;
import r01f.oids.PersistableObjectOID;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;
import r01f.services.interfaces.IndexServicesForModelObject;

/**
 * Listener to {@link COREServiceMethodExecOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * @param <M>
 */
public abstract class IndexerCRUDOKEventListener<O extends PersistableObjectOID,M extends PersistableModelObject<O>> 
              extends IndexerCRUDOKEventListenerBase<O,M,
              										 IndexServicesForModelObject<O,M>> {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexerCRUDOKEventListener(final Class<M> type,
									  final IndexServicesForModelObject<O,M> indexServices) {
		super(type,
			  indexServices);
	}
}
