package r01f.persistence.index;

import jakarta.inject.Provider;

import r01f.model.IndexableModelObject;

public interface IndexerProvider<M extends IndexableModelObject>
		 extends Provider<Indexer<M>> {

//	public Class<? extends M> getIndexableObjectType();
}
