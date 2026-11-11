package r01f.persistence.search;


import r01f.model.PersistableObject;
import r01f.model.security.context.SecurityContext;

/**
 * Interface to be implemented by {@link Searcher} subtypes that creates a model object instance
 * from the indexed data
 * For example if the search index is a Lucene index, a {@link Searcher} implementing this interface can create model objects
 * from lucene's {@link Document}
 * @param <INDEX_DATA> the index data (ie Lucene's {@link Document}s or a BBDD record set
 * @param <P>
 */
public interface SearcherCreatesResultItemFromIndexData<INDEX_DATA,
														P extends PersistableObject>
		 extends SearcherSearchResultItemFromIndexDataTransformStrategy {
	/**
	 * Creates a model object from the search index stored data
	 * @param securityContext
	 * @param indexData
	 * @return
	 */
	public P createModelObjectFrom(final SecurityContext securityContext,
								   final INDEX_DATA indexData);
}
