package r01f.persistence.index;

import r01f.model.PersistableObject;
import r01f.model.security.context.SecurityContext;
import r01f.oids.CommonOIDs.TenantID;

/**
 * Interface to be implemented by types that holds a {@link IndexerProvider}
 */
public interface HasIndexerProvider<M extends PersistableObject> {
	/**
	 * @return the repository {@link Indexer}
	 */
	public IndexerProvider<M> getIndexerProvider();
	/**
	 * Uses the {@link IndexerProvider} to get a fresh new {@link Indexer} instance
	 * @return
	 */
	public Indexer<M> getFreshNewIndexer();
	/**
	 * Uses the {@link IndexerProvider} to get a fresh new {@link Indexer} instance
	 * @param tenantId
	 * @return
	 */
	public Indexer<M> getFreshNewIndexer(final TenantID tenantId);
	/**
	 * Uses the {@link IndexerProvider} to get a fresh new {@link Indexer} instance
	 * @param securityContext
	 * @return
	 */
	public Indexer<M> getFreshNewIndexer(final SecurityContext securityContext);
}
