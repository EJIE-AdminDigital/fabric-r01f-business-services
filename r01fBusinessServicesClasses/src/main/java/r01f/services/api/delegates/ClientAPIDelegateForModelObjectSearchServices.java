package r01f.services.api.delegates;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Provider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.model.search.query.SearchResultsOrdering;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.services.core.persistence.search.SearchResultsLoader;
import r01f.services.interfaces.SearchServices;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

/**
 * Adapts Search API method invocations to the service proxy that performs the core method invocations
 * @param <F>
 * @param <I>
 */
@Accessors(prefix="_")
public abstract class ClientAPIDelegateForModelObjectSearchServices<F extends SearchFilter,I extends SearchResultItem> 
	 		  extends ClientAPIServiceDelegateBase<SearchServices<F,I>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIDelegateForModelObjectSearchServices(final Provider<SecurityContext> securityContextProvider,
														 final Marshaller modelObjectsMarshaller,
														 final SearchServices<F,I> services) {
		super(securityContextProvider,
			  modelObjectsMarshaller,
			  services);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  COUNT
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Counts the records that matches the given filter
	 * @param filter
	 * @return
	 */
	public long count(final F filter) {
		return this.getServiceProxy()
				   .countRecords(this.getSecurityContext(),
						   		 filter);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SEARCH
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @param ordering
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter,final SearchResultsOrdering... ordering) {
		Collection<SearchResultsOrdering> theOrdering = CollectionUtils.hasData(ordering) ? Stream.of(ordering)
																								  .filter(Objects::nonNull)
																								  .collect(Collectors.toList())
																				  		  : null; 
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter,
																		  theOrdering);
	}
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @param ordering
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter,final Collection<SearchResultsOrdering> ordering) {
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter,
																		  ordering);
	}
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter) {
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter,
																		  null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ClientAPIDelegateForModelObjectSearchServicesPageStep1 {
		private final F _filter;
		private final Collection<SearchResultsOrdering> _ordering;
		
		public ClientAPIDelegateForModelObjectSearchServicesPageStep2 fromItemAt(final int firstItemNum) {
			return new ClientAPIDelegateForModelObjectSearchServicesPageStep2(_filter,_ordering,
																			  firstItemNum);
		}
		public SearchResults<F,I> firstPageOfSize(final int numberOfItems) {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		0,numberOfItems);
		}
		public SearchResults<F,I> firstPage() {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		0,SearchResults.DEFAULT_PAGE_SIZE);
		}
		public Collection<I> allItems() {
			SearchResultsLoader<F,I> loader = SearchResultsLoader.create((filter,
																		  order,
																		  startPosition,
																		  pageSize) -> this.fromItemAt(startPosition)
																						   .returning(pageSize));
			return loader.collectAll(_filter);
		}
		public <T> Collection<T> allItemsTransformed(final Function<I,T> transformingFunction) {
			if (this.allItems() == null) return Lists.newArrayList();
			return this.allItems()
					   .stream()
					   .map(transformingFunction)
					   .collect(Collectors.toList());
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ClientAPIDelegateForModelObjectSearchServicesPageStep2 {		
		private final F _filter;
		private final Collection<SearchResultsOrdering> _ordering;
		private final int _firstItemNum;
		
		public SearchResults<F,I> returning(final int numberOfItems) {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		_firstItemNum,numberOfItems);
		}
		public SearchResults<F,I> returningTheDefaultNumberOfItems() {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		_firstItemNum,SearchResults.DEFAULT_PAGE_SIZE);
		}
	}
}
