package r01f.services.core.events.index;

import r01f.model.PersistableModelObject;
import r01f.oids.PersistableObjectOID;
import r01f.services.core.events.COREServiceMethodExecEventListeners.COREServiceMethodExecOKEventListener;
import r01f.services.interfaces.IndexServicesForModelObject;

/**
 * A {@link COREServiceMethodExecOKEventListener} that indexes at lucene (extends {@link IndexerCRUDOKEventListener}) 
 * @param <M>
 */
public abstract class LuceneIndexerCRUDOKEventListener<O extends PersistableObjectOID,M extends PersistableModelObject<O>> 
	          extends IndexerCRUDOKEventListener<O,M> {	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LuceneIndexerCRUDOKEventListener(final Class<M> type,
											final IndexServicesForModelObject<O,M> indexServices) {
		super(type,
			  indexServices);
	}
}
