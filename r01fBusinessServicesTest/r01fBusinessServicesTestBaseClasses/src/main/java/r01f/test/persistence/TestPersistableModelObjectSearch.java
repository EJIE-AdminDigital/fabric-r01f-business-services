package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import com.google.common.base.Stopwatch;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.Threads;
import r01f.guids.OID;
import r01f.guids.PersistableObjectOID;
import r01f.model.IndexableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.search.SearchFilterForModelObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResults;
import r01f.patterns.Factory;
import r01f.services.api.delegates.ClientAPIDelegateForModelObjectSearchServices;
import r01f.test.search.TestSearchUtil;

@Slf4j
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TestPersistableModelObjectSearch<F extends SearchFilterForModelObject,I extends SearchResultItemForModelObject<? extends IndexableModelObject>> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	private final ClientAPIDelegateForModelObjectSearchServices<F,I> _searchAPI;
	private final Factory<F> _filterFactory;
	
	private final TestPersistableModelObjectManager<? extends OID,? extends PersistableModelObject<? extends OID>> _modelObjFactory;

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static <F extends SearchFilterForModelObject,I extends SearchResultItemForModelObject<? extends IndexableModelObject>>
		   TestPersistableModelObjectSearch<F,I> create(final ClientAPIDelegateForModelObjectSearchServices<F,I> searchApi,
				   									    final Factory<F> filterFactory,
				   										final TestPersistableModelObjectManager<? extends OID,? extends PersistableModelObject<? extends OID>> modelObjFactory) {
		return new TestPersistableModelObjectSearch<F,I>(searchApi,
													     filterFactory,
														 modelObjFactory);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tests the search api
	 * @param modelObject
	 */
	@SuppressWarnings("unchecked")
	public <O extends PersistableObjectOID,M extends PersistableModelObject<O>> void testSearch(final F filter) {
		log.warn("[init][TEST BASIC SEARCH]-----------------------------------------------------------------------");

		Stopwatch stopWatch = Stopwatch.createStarted();

		// [0]: SetUp: create some test objects
		TestPersistableModelObjectManager<O,M> modelObjFactory = (TestPersistableModelObjectManager<O,M>)_modelObjFactory;
		modelObjFactory.setUpMockObjs(5);

		// [1]: give time to the objects being indexed
		Threads.safeSleep(5000);

		// [2]: Run tests
		log.warn("SEARCH ENTITIES WITH FILTER {}",
				 filter.toCriteriaString());
		SearchResults<F,I> results = _searchAPI.search(filter).firstPage();
		Assertions.assertTrue(results.getTotalItemsCount() > 0);

		TestSearchUtil.debugSearchResults(results);

		// [99]: Delete previously created test objects to restore DB state
		_modelObjFactory.tearDownCreatedMockObjs();
		log.warn("[end ][TEST BASIC SEARCH] (elapsed time: {} millis) -------------------------",
				 NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)));
		stopWatch.stop();
	}
	/**
	 * Test the index & unindex operations
	 * @param emptyFilter
	 */
	@SuppressWarnings("unchecked")
	public <O extends PersistableObjectOID,M extends PersistableModelObject<O>> void testIndexAndUnIndex() {
		long totalItems = 0;

		log.warn("[init][TEST INDEX & UNINDEX]--------------------------------------------------------------------");
		Stopwatch stopWatch = Stopwatch.createStarted();

		// [1]: Set-up 10 model objects
		log.warn("Create 10 model objects....");
		TestPersistableModelObjectManager<O,M> modelObjFactory = (TestPersistableModelObjectManager<O,M>)_modelObjFactory;
		modelObjFactory.setUpMockObjs(10);

		// [2]: Give time for the objects to be indexed
		log.warn("... wait some time to give space for the indexer to index all objects");
		Threads.safeSleep(10000);

		// [3]: Ensure that there're 10 indexed objects
		//		BEWARE!!	If this test FAILS, maybe the search engine index should be reset
		//					since it contains zoombie data
		log.warn("...check if the previously created objects are indexed");
		F emptyFilter = _filterFactory.create();		// empty filter
		totalItems = _searchAPI.search(emptyFilter)		// an empty filter
							   .firstPage()
							   .getTotalItemsCount();
		log.warn("> There're {} indexed",
				 totalItems);
		Assertions.assertTrue(totalItems == 10);

		// [4]: Wipe previously created objects
		_modelObjFactory.tearDownCreatedMockObjs();

		// [5]: Give time for the objects to be unindexed
		log.warn("... wait some time to give space for the indexer to unindex all objects");
		Threads.safeSleep(10000);

		// [6]: Ensure that there're 10 indexed objects
		totalItems = _searchAPI.search(emptyFilter)		// an empty filter
							   .firstPage()
							   .getTotalItemsCount();
		Assertions.assertTrue(totalItems == 0);

		log.warn("[end ][TEST BASIC INDEX & UNINDEX] (elapsed time: {} milis) -------------------------",
				 NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)));
	}
}
