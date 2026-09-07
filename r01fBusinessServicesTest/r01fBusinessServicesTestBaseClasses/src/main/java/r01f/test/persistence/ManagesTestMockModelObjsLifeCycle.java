package r01f.test.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import r01f.debug.Debuggable;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.patterns.Factory;
import r01f.services.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.util.types.collections.CollectionUtils;

public interface ManagesTestMockModelObjsLifeCycle<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
		 extends Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	OBJ TYPE
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * @return the managed model object type
	 */
	public Class<M> getModelObjType();
/////////////////////////////////////////////////////////////////////////////////////////
//	API
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * @return the crud api
	 */
	public ClientAPIDelegateForModelObjectCRUDServices<O,M> getCRUDApi();
/////////////////////////////////////////////////////////////////////////////////////////
//	MOCK OBJECTS FACTORY
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * @return the factory of mock objects
	 */
	public Factory<? extends M> getMockObjectsFactory();
	/**
	 * Returns the factory of mock objects typed
	 * @param <F>
	 * @param factoryType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default <F extends Factory<? extends M>> F getMockObjectsFactoryAs(final Class<F> factoryType) {
		return (F)this.getMockObjectsFactory();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SETU
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a number of model objects using the provided factory
	 * @param numOfObjectsToCreate
	 * @param postSetUpActions
	 * @return the created objects
	 */
	public Collection<M> setUpMockObjs(final int numOfObjsToCreate,
									   final Function<M,M> preSetupAction);
	/**
	 * Creates a number of model objects using the provided factory
	 * @param numOfObjsToCreate
	 * @return
	 */
	public default Collection<M> setUpMockObjs(final int numOfObjsToCreate) {
		return this.setUpMockObjs(numOfObjsToCreate,
								  Function.identity());
	}
	/**
	 * Creates a single model object using the provided factory
	 * @param preSetupAction
	 * @return the created obj
	 */
	public default M setUpSingleMockObj(final Function<M,M> preSetupAction) {
		Collection<M> createdObj = this.setUpMockObjs(1,
													  preSetupAction);
		return CollectionUtils.<M>pickOneAndOnlyElement(createdObj);
	}
	/**
	 * Creates a single model object using the provided factory
	 * @return the created obj
	 */
	public default M setUpSingleMockObj() {
		return this.setUpSingleMockObj(Function.identity());
	}
	/**
	 * Sets up the given mock obj
	 * @param obj
	 */
	public M setUpMockObj(final M obj);
	/**
	 * Sets up the given mock obj
	 * @param objs
	 */
	@SuppressWarnings("unchecked")
	public default Collection<M> setUpMockObjs(final M... objs) {
		if (CollectionUtils.isNullOrEmpty(objs)) return Collections.EMPTY_LIST;
		return Stream.of(objs)
					 .map(this::setUpMockObj)
					 .toList();
	}
	/**
	 * Sets up the given mock obj
	 * @param obj
	 * @return
	 */
	public default M addMockObj(final M obj) {
		return this.setUpMockObj(obj);
	}
	/**
	 * Sets up the given mock obj
	 * @param objs
	 * @return
	 */
	@SuppressWarnings("unchecked") 
	public default void addMockObjs(final M... objs) {
		this.setUpMockObjs(objs);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	TEAR DOWN
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Deletes a {@link Collection} of previously created objects
	 * @param createdObjs
	 */
	public void tearDownCreatedMockObjs();
	/**
	 * Deletes a previously created object
	 * @param oids
	 */
	public void tearDownCreatedMockObjWithOids(final Collection<O> oids);
	/**
	 * Deletes a previously created object
	 * @param oids
	 */
	@SuppressWarnings("unchecked")
	public default void tearDownCreatedMockObjWithOids(final O... oids) {
		if (CollectionUtils.isNullOrEmpty(oids)) return;
		Collection<O> fOids = Stream.of(oids)
								    .filter(Objects::nonNull)
								    .toList();
		this.tearDownCreatedMockObjWithOids(fOids);
	}
	public default void tearDownCreatedMockObjs(final Collection<M> objs) {
		if (CollectionUtils.isNullOrEmpty(objs)) return;
		Collection<O> oids = objs.stream()
								 .filter(Objects::nonNull)
								 .map(M::getOid)
								 .toList();
		this.tearDownCreatedMockObjWithOids(oids);
	}
	@SuppressWarnings("unchecked")
	public default void tearDownCreatedMockObjs(final M... objs) {
		if (CollectionUtils.isNullOrEmpty(objs)) return;
		Collection<O> oids = Stream.of(objs)
								   .filter(Objects::nonNull)
								   .map(M::getOid)
								   .toList();
		this.tearDownCreatedMockObjWithOids(oids);
	}
	/**
	 * Reset the state removing the stored created mock objs oids
	 */
	public void reset();
/////////////////////////////////////////////////////////////////////////////////////////
//	GET CREATED MOCK OBJS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns {@link Collection} of the oids of the created model objects after calling {@link #setUpMockObjs(int)}
	 * @return
	 */
	public O getAnyCreatedMockObjOid();
	/**
	 * Returns {@link Collection} of the oids of the created model objects after calling {@link #setUpMockObjs(int)}
	 * @return
	 */
	public O getAnyCreatedMockObjOidOrCreateNew();
	/**
	 * @return a {@link Collection} of the created model objects after calling {@link #setUpMockObjs(int)}
	 */
	public M getAnyCreatedMockObj();
	/**
	 * @return a {@link Collection} of the created model objects after calling {@link #setUpMockObjs(int)}
	 */
	public M getAnyCreatedMockObjOrCreateNew();
	/**
	 * @return the created model objects after calling after calling {@link #setUpMockObjs(int)}
	 */
	public Collection<M> getCreatedMockObjs();
	/**
	 * @return the oids of the created model objects
	 */
	public Collection<O> getCreatedMockObjsOids();
}
