package r01f.services.core.persistence.search;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.model.search.SearchResultsProvider;
import r01f.model.search.query.SearchResultsOrdering;
import r01f.types.pager.Paging;

/**
 * Utility type that pages across all search results pages to collect all the result items
 * Usage: 
 * <pre class='brush:java'>
 *	// Provider of search results pages (quite verbose for java6 WTF!)
 *	SearchResultsProvider<MyFilter,MySearchResultsItem> resultsProvider = 
 *			new SearchResultsProvider<MyFilter,MySearchResultsItem>() {
 *						@Override
 *						public SearchResults<MyFilter,MySearchResultsItem> provide(final MyFilter filter,
 *																				   final Collection<SearchResultsOrdering> ordering
 *																				   final int startPosition,
 *																				   final int pageSize) {
 *							// ... use some api to retrieve the results
 *							return searchResults;
 *						}
 *			};
 *	// Loader
 *	SearchResultsLoader<MyFilter,MySearchResultsItem> loader = SearchResultsLoader.create(resultsProvider);
 *	Collection<MySearchResultsItem> outItems = loader.collectAll();
 *
 *  // If the results need to be transformed:
 *	// Search result item to model object transformer
 *	Function<MySearchResultsItem,MyModelObject> transformer = 
 *			new Function<MySearchResultsItem,MyModelObject>() {
 *					@Override
 *					public MyModelObject apply(final MySearchResultsItem item) {
 *						// ... transform from MySearchResultsItem to MyModelObject
 *						return item;
 *					}
 *			};
 * Collection<MyModelObject> outResults = loader.collectAll(transformer);
 * </pre>
 * @param <F>
 * @param <I>
 */
@RequiredArgsConstructor
public class SearchResultsLoader<F extends SearchFilter,		// search result filter
						 		 I extends SearchResultItem> {	// search result item
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private final SearchResultsProvider<F,I> _provider;
	
	public static <F extends SearchFilter,
				   I extends SearchResultItem> SearchResultsLoader<F,I> create(final SearchResultsProvider<F,I> resultsProvider) {
		return new SearchResultsLoader<>(resultsProvider);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * @return all page results as T objects
	 */
	public Collection<I> collectAll(final F filter) {
		return this.collectAll(filter,
							   (Collection<SearchResultsOrdering>)null);	// no ordering
	}
	/**
	 * @return all page results as T objects
	 */
	public Collection<I> collectAll(final F filter,final Collection<SearchResultsOrdering> ordering) {
		SearchResults<F,I> searchResults = _provider.provide(filter,
															 ordering,
															 0);		// first page (just to get the total items)
		Collection<I> outItems = null;
		if (searchResults != null && searchResults.getTotalItemsCount() > 0) {
			outItems = Lists.newArrayListWithExpectedSize(searchResults.getTotalItemsCount());
			
			// Iterate over the results page
			while (searchResults != null && searchResults.hasData()) {
				// Current page results
				Collection<I> pageResults = searchResults.getPageItems();
				outItems.addAll(pageResults);
				
				// Goto the next page (if it exists)
				if (searchResults.hasMorePages()) {
					searchResults = _provider.provide(filter,
													  ordering,
													  searchResults.getNextPageStartPosition());	// next page
				} else {
					searchResults = null;
				}
			}
		}
		return outItems;
	}
	/**
	 * @return all page results as T objects
	 */
	public <T> Collection<T> collectAll(final F filter,
										final Function<I,T> transformFunction) {
		return this.collectAll(filter,
							   (Collection<SearchResultsOrdering>)null,	// no ordering
							   transformFunction);
	}
	/**
	 * @return all page results as T objects
	 */
	public <T> Collection<T> collectAll(final F filter,
										final Collection<SearchResultsOrdering> ordering,
										final Function<I,T> transformFunction) {
		SearchResults<F,I> searchResults = _provider.provide(filter,
															 ordering,
															 0,	// first page
															 SearchResults.DEFAULT_PAGE_SIZE);
		Collection<T> outObjs = null;
		if (searchResults != null && searchResults.getTotalItemsCount() > 0) {
			outObjs = Lists.newArrayListWithExpectedSize(searchResults.getTotalItemsCount());
			
			// Iterate over the results page
			while (searchResults != null && searchResults.hasData()) {
				// Transform current page results
				Collection<I> pageResults = searchResults.getPageItems();
				for (I result : pageResults) {
					T obj = transformFunction.apply(result);
					outObjs.add(obj);
				}
				// Goto the next page (if it exists)
				if (searchResults.hasMorePages()) {
					searchResults = _provider.provide(filter,
													  ordering,
													  searchResults.getNextPageStartPosition());	// next page
				} else {
					searchResults = null;
				}
			}
		}
		return outObjs;
	}
	/**
	 * Returns an item stream
	 * @return
	 */
	public Stream<I> resultItemsStream(final F filter) {
		SearchResults<F,I> searchResults = _provider.provide(filter,0);	// first page (just to get the total items)
		if (searchResults == null || searchResults.getTotalItemsCount() == 0) return Stream.empty();
		
		long numResults = searchResults.getTotalItemsCount();
		int pageSize = searchResults.getRequestedNumberOfItems();
		
		int pageCount = Paging.computePageCountFor(numResults,pageSize);
		
		return Stream.generate(new Supplier<SearchResults<F,I>>() {
									SearchResults<F,I> prevSearchResults = searchResults;
									int _nextPageStartPosition = searchResults.getNextPageStartPosition();								
									
									@Override
									public SearchResults<F,I> get() {
										SearchResults<F,I> nextSearchResults = _provider.provide(filter,_nextPageStartPosition);
										_nextPageStartPosition = searchResults.getNextPageStartPosition();
										
										SearchResults<F,I> outSearchResults = prevSearchResults;
										prevSearchResults = nextSearchResults;
										return outSearchResults;
									}
				 				})
					 .limit(pageCount - 1)	// the first page has been generated outside the supplier!
					 .flatMap(theSearchResults -> theSearchResults.getPageItems().stream());
	}
}