package r01f.services.interfaces;


import r01f.model.PersistableModelObjectWithID;
import r01f.model.facets.Versionable;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;

/**
 * CRUD (create, read, update, delete) interface for not {@link Versionable} model object
 * @param <O>
 * @param <M>
 */
public interface CRUDServicesForModelObjectWithID<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>>
		 extends CRUDServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Loads an object using it's id
	 * @param userContext
	 * @param id
	 * @return
	 */
	public CRUDResult<M> loadById(final SecurityContext securityContext,
								  final I id);
/////////////////////////////////////////////////////////////////////////////////////////
//	DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Deletes an object using it's id
	 * @param securityContext
	 * @param id
	 * @return
	 */
	public CRUDResult<M> deleteById(final SecurityContext securityContext,
								    final I id);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks if an object exists using it's id
	 * @param securityContext
	 * @param id
	 * @return
	 */
	public PersistenceOperationResult<Boolean> exists(final SecurityContext securityContext,
												 	  final I id);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the id of an object using it's oid
	 * @param securityContext
	 * @param oid
	 * @return
	 */
	public PersistenceOperationResult<I> getIdOf(final SecurityContext securityContext,
												 final O oid);
	/**
	 * Gets the oid of an object using it's id
	 * @param securityContext
	 * @param oid
	 * @return
	 */
	public PersistenceOperationResult<O> getOidOf(final SecurityContext securityContext,
												  final I id);
}
