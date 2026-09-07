package r01f.test.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.aspects.dirtytrack.DirtyTrackAdapter;
import r01f.concurrent.Threads;
import r01f.guids.PersistableObjectOID;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.reflection.ReflectionUtils;
import r01f.services.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.test.api.TestAPIBase;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
abstract class TestPersistableModelObjectManagerBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
    implements ManagesTestMockModelObjsLifeCycle<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final Class<M> _modelObjType;
	@Getter protected final Factory<? extends M> _mockObjectsFactory;
	@Getter protected final Function<M,M> _preSetupAction;
	@Getter protected final ClientAPIDelegateForModelObjectCRUDServices<O,M> _CRUDApi;
	@Getter protected final long _milisToWaitForBackgroundJobs;

	@Getter protected Collection<O> _createdMockObjsOids;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE BACKGROUND JOBS (ie indexing)
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectManagerBase(final Class<M> modelObjType,
											 	 final Factory<? extends M> mockObjectsFactory,final Function<M,M> preSetupAction,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 	 final long milisToWaitForBackgroundJobs) {
		_modelObjType = modelObjType;
		_mockObjectsFactory = mockObjectsFactory;
		_preSetupAction = preSetupAction;
		_CRUDApi = crudAPI;
		_milisToWaitForBackgroundJobs = milisToWaitForBackgroundJobs;
	}
	public TestPersistableModelObjectManagerBase(final Class<M> modelObjType,
											 	 final Factory<? extends M> mockObjectsFactory,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 	 final long milisToWaitForBackgroundJobs) {
		this(modelObjType,
			 mockObjectsFactory,Function.identity(),
			 crudAPI,
			 milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectManagerBase(final Class<M> modelObjType,
											 	 final Factory<M> mockObjectsFactory,final Function<M,M> preSetupAction,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		this(modelObjType,
			 mockObjectsFactory,preSetupAction,
			 crudAPI,
			 0l);
	}
	public TestPersistableModelObjectManagerBase(final Class<M> modelObjType,
											 	 final Factory<M> mockObjectsFactory,
											 	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		this(modelObjType,
			 mockObjectsFactory,Function.identity(),
			 crudAPI,
			 0l);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void reset() {
		_createdMockObjsOids = null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public M setUpMockObj(final M objectToBeCreated) {
		if (ReflectionUtils.isImplementing(objectToBeCreated.getClass(),
										   DirtyTrackAdapter.class)) {
			DirtyTrackAdapter.adapt(objectToBeCreated).setNew();	// ensure it's a new object!
		}
		objectToBeCreated.setEntityVersion(0);						// ensure it have a zero entity numeric id

		objectToBeCreated.setTrackingInfo(new ModelObjectTracking(TestAPIBase.TEST_USER_OID,TestAPIBase.TEST_USER,
																  Instant.now()));			// Ensure tracking info
		
		if (_preSetupAction != null) _preSetupAction.apply(objectToBeCreated);	// pre setup

		M createdModelObj = _CRUDApi.create(objectToBeCreated);
		if (createdModelObj.getOid() == null) throw new IllegalStateException("The created model object of type " + _modelObjType + " does NOT have oid value!");
		log.info("... Created {} mock object with oid={}",_modelObjType.getSimpleName(),createdModelObj.getOid());

		// add the oids to the created model obj oids
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) _createdMockObjsOids = Lists.newArrayList();
		_createdMockObjsOids.add(createdModelObj.getOid());

		return createdModelObj;
	}
	@Override
	public Collection<M> setUpMockObjs(final int numOfObjsToCreate,
									   final Function<M,M> preSetupAction) {
		Collection<M> outCreatedModelObjs = Lists.newArrayListWithExpectedSize(numOfObjsToCreate);
		// create test model objects
		Collection<O> outCreatedMockObjsOids = Sets.newLinkedHashSetWithExpectedSize(numOfObjsToCreate);
		for (int i=0; i < numOfObjsToCreate; i++) {
			M newObject = _mockObjectsFactory.create();
			
			M objectToBePersisted = _preSetupAction != null ? _preSetupAction.apply(newObject) : null;
			objectToBePersisted = preSetupAction != null ? preSetupAction.apply(newObject) : null;
			
			M createdObj = this.setUpMockObj(objectToBePersisted);
			
			outCreatedMockObjsOids.add(createdObj.getOid());
			outCreatedModelObjs.add(createdObj);
		}
		return outCreatedModelObjs;
	}
	@Override
	public void tearDownCreatedMockObjs() {
		this.tearDownCreatedMockObjWithOids(_createdMockObjsOids);
		this.reset();
	}
	@Override
	public void tearDownCreatedMockObjWithOids(final Collection<O> oids) {
		if (CollectionUtils.isNullOrEmpty(oids)) return;

		try {
			// wait for background jobs to finish
			// an error in the background job will raise if the DB records are deleted before background jobs finish (ie lucene indexing or notification tasks)
			long milisToWaitForBackgroundJobs = oids.size() * _milisToWaitForBackgroundJobs;
			if (milisToWaitForBackgroundJobs > 0) {
				log.info(".... give {} milis for background jobs (ie lucene index or notifications) to complete before deleting created DB records (lucene indexing or notifications will fail if the DB record is deleted)",
						 milisToWaitForBackgroundJobs);
				Threads.safeSleep(milisToWaitForBackgroundJobs);
			}
	
			// delete all DB records
			for (O oid : oids) {
				if (oid == null) continue;
				_CRUDApi.delete(oid);
				log.info("... Deleted {} mock object with oid={}",_modelObjType.getSimpleName(),oid);
			}
			// remove the deleted oids from the created mock objs oids
			if (_createdMockObjsOids != null) _createdMockObjsOids.removeAll(oids);
		} catch (Throwable th) {
			log.error("[tear down] > Error deleting {} mock objects with oids={}: {}",
					  _modelObjType.getSimpleName(),oids,
					  th.getMessage(),th);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public O getAnyCreatedMockObjOid() {
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) throw new IllegalStateException("There's NO created model object available at the factory");
		return CollectionUtils.of(_createdMockObjsOids).pickOneElement();
	}
	@Override
	public O getAnyCreatedMockObjOidOrCreateNew() {
		if (CollectionUtils.isNullOrEmpty(_createdMockObjsOids)) this.setUpSingleMockObj();

		return CollectionUtils.of(_createdMockObjsOids).pickOneElement();
	}
	@Override
	public M getAnyCreatedMockObj() {
		O oid = this.getAnyCreatedMockObjOid();
		M outModelObj = _CRUDApi.load(oid);
		return outModelObj;
	}
	@Override
	public M getAnyCreatedMockObjOrCreateNew() {
		O oid = this.getAnyCreatedMockObjOidOrCreateNew();
		M outModelObj = _CRUDApi.load(oid);
		return outModelObj;
	}
	@Override
	public Collection<M> getCreatedMockObjs() {
		if (_createdMockObjsOids == null) return null;
		Collection<M> outModelObjs = Lists.newArrayListWithExpectedSize(_createdMockObjsOids.size());
		for (O oid : _createdMockObjsOids) {
			outModelObjs.add(_CRUDApi.load(oid));
		}
		return outModelObjs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder sb = new StringBuilder(_modelObjType.getName());
		sb.append(" debug objects manager: ");
		if (CollectionUtils.hasData(_createdMockObjsOids)) {
			sb.append(_createdMockObjsOids.size()).append(" > ").append(_createdMockObjsOids);
		}
		else {
			sb.append(" NO objects");
		}
		return sb;
	}
}
