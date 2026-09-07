package r01f.test.persistence;

import com.google.common.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.patterns.CommandOn;
import r01f.patterns.Factory;
import r01f.services.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.services.api.delegates.ClientAPIDelegateForModelObjectFindServices;

/**
 * JVM arguments:
 * -javaagent:D:/develop/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 */
@Slf4j
public abstract class TestPersistableModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final ClientAPIDelegateForModelObjectCRUDServices<O,M> _crudApi;
	protected final ClientAPIDelegateForModelObjectFindServices<O,M> _findApi;
	protected final ManagesTestMockModelObjsLifeCycle<O,M> _managesTestMockObjects;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	protected TestPersistableModelObjectBase(final ManagesTestMockModelObjsLifeCycle<O,M> managesTestMockObjects,
											 final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudApi,final ClientAPIDelegateForModelObjectFindServices<O,M> findApi) {
		_managesTestMockObjects = managesTestMockObjects;
		_crudApi = crudApi;
		_findApi = findApi;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public ManagesTestMockModelObjsLifeCycle<O,M> getTestMockObjsLifeCycleManager() {
		return _managesTestMockObjects;
	}
	public Factory<? extends M> getMockObjectsFactory() {
		return _managesTestMockObjects.getMockObjectsFactory();
	}
	public <F extends Factory<? extends M>> F getMockObjectsFactoryAs(final Class<F> factoryType) {
		return _managesTestMockObjects.getMockObjectsFactoryAs(factoryType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public void tearDownCreatedMockObjs() {
		_managesTestMockObjects.tearDownCreatedMockObjs();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	public void doCRUDTest() {
		this.doCRUDTest(true);	// test delete
	}
	public void doCRUDTest(final boolean testDelete) {
		// [1]: Basic persistence tests
		TestPersistableModelObjectCRUD<O,M> crudTest = TestPersistableModelObjectCRUD.create(// crud api
																							 _crudApi,
																							 // model objects factory
																							 _managesTestMockObjects);
		crudTest.testPersistence(_modelObjectStateUpdateCommand(),
								 testDelete);
	}
	/**
	 * @return a {@link CommandOn} that changes the model object's state (simulate a user update action)
	 */
	protected abstract CommandOn<M> _modelObjectStateUpdateCommand();

/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	public void doFindTest() {
		this.doFindTest(true);	// delete created objs
	}
	public void doFindTest(final boolean deleteCreatedObjs) {
		// [1]: Basic find tests
		TestPersistableModelObjectFind<O,M> findTest = TestPersistableModelObjectFind.create(// find api
																							 _findApi,
																							 // mock objects factory
																							 _managesTestMockObjects);
		findTest.testFind(deleteCreatedObjs);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATES
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked" })
	protected <A extends ClientAPIDelegateForModelObjectCRUDServices<O,M>> A getClientCRUDApiAs(final Class<A> apiType) {
		return (A)_crudApi;
	}
	@SuppressWarnings({ "unchecked" })
	protected <A extends ClientAPIDelegateForModelObjectCRUDServices<O,M>> A getClientCRUDApiAs(final TypeToken<A> apiType) {
		return (A)_crudApi;
	}	
	@SuppressWarnings({ "unchecked" })
	protected <A extends ClientAPIDelegateForModelObjectFindServices<O,M>> A getClientFindApiAs(final Class<A> apiType) {
		return (A)_findApi;
	}
	@SuppressWarnings({ "unchecked" })
	protected <A extends ClientAPIDelegateForModelObjectFindServices<O,M>> A getClientFindApiAs(final TypeToken<A> apiType) {
		return (A)_findApi;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
}
