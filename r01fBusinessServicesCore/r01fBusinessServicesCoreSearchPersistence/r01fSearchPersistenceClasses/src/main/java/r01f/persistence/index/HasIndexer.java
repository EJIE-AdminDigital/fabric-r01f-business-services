package r01f.persistence.index;

import r01f.model.PersistableObject;

/**
 * Interface to be implemented by types that holds a {@link Indexer}
 */
public interface HasIndexer<M extends PersistableObject> {
	/**
	 * @return the repository {@link Indexer}
	 */
	public Indexer<M> getIndexer();
}
