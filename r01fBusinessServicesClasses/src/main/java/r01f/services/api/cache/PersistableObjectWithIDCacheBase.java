package r01f.services.api.cache;

import jakarta.inject.Provider;
import r01f.model.PersistableModelObjectWithID;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;
import r01f.oids.refs.IsPersistableObjectWithIDRef;
import r01f.oids.refs.IsPersistableObjectWithIDRef.PersistableObjectWithIDRefFactory;
import r01f.services.interfaces.CRUDServicesForModelObjectWithID;
import r01f.services.interfaces.FindServicesForModelObject;

public abstract class PersistableObjectWithIDCacheBase<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>> 
			  extends PersistableObjectCacheBase<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A bidirectional map to cache the OID-ID associations for the objects in the cache.
	 */
	protected final PersistableObjectRefSolverBase<O,I> _refSolver;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected <CRUD extends CRUDServicesForModelObjectWithID<O,I,M>,
			   FIND extends FindServicesForModelObject<O,M>> PersistableObjectWithIDCacheBase(final Class<M> modelObjectType,
   																					   		  // the system security context provider needed for the cache
																					   		  final Provider<SecurityContext> securityContextProvider,
																					   		  // the CRUD & FIND services
																					   		  final CRUD crud,final FIND find) {
		super(modelObjectType,
			  securityContextProvider,
			  crud,find);
		_refSolver = new PersistableObjectRefSolverBase<>(// retrieve the ID of an object given its OID
														  oid -> _retrieveIddOf(oid),
														  // retrieve the OID of an object given its ID
														  id -> _retrieveOidOf(id)) {
						// nothing
					};
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private I _retrieveIddOf(final O oid) {
		return _crud().getIdOf(_securityContextProvider.get(),
							   oid)
					  .getOrDefault(null);
	}
	private O _retrieveOidOf(final I id) {
		return _crud().getOidOf(_securityContextProvider.get(),
								id)
					  .getOrDefault(null);
	}
	@SuppressWarnings("unchecked")
	private CRUDServicesForModelObjectWithID<O,I,M> _crud() {
		return (CRUDServicesForModelObjectWithID<O,I,M>)_crud;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public I getIdOf(final O oid) {
		return _refSolver.getIdOf(oid);
	}
	public O getOidOf(final I id) {
		return _refSolver.getOidOf(id);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	protected <R extends IsPersistableObjectWithIDRef<O,I>> R _completeRefIfNeeded(final R ref,
																				   final PersistableObjectWithIDRefFactory<O,I,R> refFactory) {
		return _refSolver.completeRefIfNeeded(ref,
											  refFactory);
	}
	protected <R extends IsPersistableObjectWithIDRef<O,I>> R _getRefOf(final O oid,
																	    final PersistableObjectWithIDRefFactory<O,I,R> refFactory) {
		return _refSolver.getRefOf(oid,
								   refFactory);
	}
	protected <R extends IsPersistableObjectWithIDRef<O,I>> R _getRefOf(final I id,
																	    final PersistableObjectWithIDRefFactory<O,I,R> refFactory) {
		return _refSolver.getRefOf(id,
								   refFactory);
	}
}
