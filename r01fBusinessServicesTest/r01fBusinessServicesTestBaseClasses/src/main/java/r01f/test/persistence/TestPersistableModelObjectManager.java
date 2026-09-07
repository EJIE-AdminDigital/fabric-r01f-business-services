package r01f.test.persistence;

import java.util.function.Function;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.services.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;

@Slf4j
@Accessors(prefix="_")
public class TestPersistableModelObjectManager<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
	 extends TestPersistableModelObjectManagerBase<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE BACKGROUND JOBS (ie indexing)
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectManager(final Class<M> modelObjType,
											 final Factory<? extends M> mockObjectsFactory,final Function<M,M> preSetupAction,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 final long milisToWaitForBackgroundJobs) {
		super(modelObjType,
			  mockObjectsFactory,preSetupAction,
			  crudAPI,
			  milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectManager(final Class<M> modelObjType,
											 final Factory<? extends M> mockObjectsFactory,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
											 final long milisToWaitForBackgroundJobs) {
		super(modelObjType,
			  mockObjectsFactory,
			  crudAPI,
			  milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectManager(final Class<M> modelObjType,
											 final Factory<M> mockObjectsFactory,final Function<M,M> preSetupAction,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		super(modelObjType,
			  mockObjectsFactory,preSetupAction,
			  crudAPI);
	}
	public TestPersistableModelObjectManager(final Class<M> modelObjType,
											 final Factory<M> mockObjectsFactory,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		super(modelObjType,
			  mockObjectsFactory,
			  crudAPI);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final Class<M> modelObjType,
																																	 final Factory<? extends M> mockObjectsFactory,final Function<M,M> preSetupAction,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																																	 final long milisToWaitForBackgroundJobs) {
		return new TestPersistableModelObjectManager<>(modelObjType,
													   mockObjectsFactory,preSetupAction,
													   crudAPI,
													   milisToWaitForBackgroundJobs);
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final Class<M> modelObjType,
																																	 final Factory<? extends M> mockObjectsFactory,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																																	 final long milisToWaitForBackgroundJobs) {
		return new TestPersistableModelObjectManager<>(modelObjType,mockObjectsFactory,
													   crudAPI,
													   milisToWaitForBackgroundJobs);
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final Class<M> modelObjType,
																																	 final Factory<M> mockObjectsFactory,final Function<M,M> preSetupAction,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectManager<>(modelObjType,
													   mockObjectsFactory,preSetupAction,
													   crudAPI,
													   0L);		// no need to wait for crud-associated background jobs
	}
	public static <O extends PersistableObjectOID,M extends PersistableModelObject<O>> TestPersistableModelObjectManager<O,M> create(final Class<M> modelObjType,
																																	 final Factory<M> mockObjectsFactory,
																																	 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectManager<>(modelObjType,
													   mockObjectsFactory,
													   crudAPI,
													   0L);		// no need to wait for crud-associated background jobs
	}
}
