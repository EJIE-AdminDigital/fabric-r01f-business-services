package r01f.services.core.persistence.internal.interfaces;

import r01f.model.catalogable.CatalogableIDs.CatalogableObjectTypeID;
import r01f.model.catalogable.CatalogableOIDs.CatalogableObjectOID;
import r01f.model.catalogable.ContainsPicksForCatalogableObject;
import r01f.model.persistence.CRUDResult;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.ServiceInterface;

public interface CoreCRUDServicesForCatalogablePicksBase<T extends ContainsPicksForCatalogableObject<?>>
         extends ServiceInterface {
/////////////////////////////////////////////////////////////////////////////////////////
//	CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a geo catalogable object's picks.
	 * @param securityContext the user auth data & context info
	 * @param catalogableObjectOid
	 * @param catalogableObjectTypeId
	 * @return a {@link CRUDResult} that encapsulates the entity if it was loaded successfully
	 */
	public CRUDResult<T> load(final SecurityContext securityContext,
				  			  final CatalogableObjectOID catalogableObjectOid,final CatalogableObjectTypeID catalogableObjectTypeId);	
	/**
	 * Saves a geo catalogable object's picks
	 * @param securityContext the user auth data & context info
	 * @param picks the picks to be persisted
	 * @return a {@link CRUDResult} that encapsulates the created entity
	 */
	public CRUDResult<T> save(final SecurityContext securityContext,
				  			  final T picks);
	/**
	 * Deletes geo catalogable object's picks
	 * @param securityContext the user auth data & context info
	 * @param catalogableObjectOid
	 * @param catalogableObjectTypeId
	 * @return a {@link CRUDResult} that encapsulates the deleted entity
	 */
	public CRUDResult<T> delete(final SecurityContext securityContext,
								final CatalogableObjectOID catalogableObjectOid,final CatalogableObjectTypeID catalogableObjectTypeId);
}