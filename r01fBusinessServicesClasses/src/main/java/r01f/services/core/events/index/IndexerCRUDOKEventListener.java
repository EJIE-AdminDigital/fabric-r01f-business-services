package r01f.services.core.events.index;

 

import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;
import r01f.services.interfaces.IndexServicesForModelObject;

/**
 * Listener to {@link COREServiceMethodExecOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * @param <M>
 */
public abstract class IndexerCRUDOKEventListener<O extends OID,M extends IndexableModelObject> 
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
