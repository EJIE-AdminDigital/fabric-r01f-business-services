package r01f.persistence.index;

import jakarta.inject.Provider;
import r01f.model.PersistableObject;

public interface IndexerProvider<M extends PersistableObject>
		 extends Provider<Indexer<M>> {

//	public Class<? extends M> getIndexableObjectType();
}
