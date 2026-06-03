package r01f.services.delegates.persistence.search;

import java.util.Collection;

import com.google.common.collect.Collections2;

import r01f.eventbus.EventBus;
import r01f.facets.HasOID;
import r01f.guids.PersistableObjectOID;
import r01f.model.search.SearchFilterForModelObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResultsProvider;
import r01f.persistence.search.Searcher;
import r01f.securitycontext.SecurityContext;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.persistence.search.SearchResultsLoader;
import r01f.services.interfaces.SearchServicesForModelObject;
import r01f.util.types.collections.CollectionUtils;

public abstract class SearchServicesForModelObjectDelegateBase<F extends SearchFilterForModelObject,I extends SearchResultItemForModelObject<?>> 
			  extends SearchServicesDelegateBase<F,I> 
		   implements SearchServicesForModelObject<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchServicesForModelObjectDelegateBase(final ServicesCoreModuleConfig coreCfg,
									  				final Searcher<F,I> searcher,
									  				final EventBus eventBus) {
		super(coreCfg,
			  eventBus,
			  searcher);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override @SuppressWarnings("unchecked")
	public <O extends PersistableObjectOID> Collection<O> filterRecordsOids(final SecurityContext securityContext, 
														   					final F filter) {
		// BEWARE!!!!
		//		This default impl is NOT OPTIMIZED since it just filter all records 
		//		and returns the oids
		
		// Validate the filer
		_validateSearchFilter(securityContext,
							  filter);
		
		// Collect all results
	 	SearchResultsProvider<F,I> resultsProvider = (theFilter,startPosition,pageSize) -> super.filterRecords(securityContext,
										 										  							   theFilter,null,	// no ordering
										 										  							   startPosition,pageSize);
	    SearchResultsLoader<F,I> loader = SearchResultsLoader.create(resultsProvider);
	    Collection<I> allResults = loader.collectAll(filter);
		
		// return 
		Collection<O> outRecords = null;
		if (CollectionUtils.hasData(allResults)) {
			outRecords = Collections2.transform(allResults,
												item -> {
													if (!(item instanceof HasOID)) throw new IllegalStateException(item.getClass() + " does NOT have an OID!");
													HasOID<O> hasOid = (HasOID<O>)item;
													return hasOid.getOid();
												});
		}
		return outRecords;
	}
}
