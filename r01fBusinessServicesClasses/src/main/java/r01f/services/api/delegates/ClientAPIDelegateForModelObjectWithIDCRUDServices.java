package r01f.services.api.delegates;

import com.google.common.reflect.TypeToken;

import jakarta.inject.Provider;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.generics.TypeRef;
import r01f.model.PersistableModelObjectWithID;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceException;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;
import r01f.services.interfaces.CRUDServicesForModelObjectWithID;

public abstract class ClientAPIDelegateForModelObjectWithIDCRUDServices<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>>
			  extends ClientAPIDelegateForModelObjectCRUDServices<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIDelegateForModelObjectWithIDCRUDServices(final Provider<SecurityContext> securityContextProvider,
													   		 final Marshaller modelObjectsMarshaller,
													   		 final CRUDServicesForModelObjectWithID<O,I,M> services) {
		super(securityContextProvider,
			  modelObjectsMarshaller,
			  services);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads an object using it's id
	 * @param id
	 * @return
	 */
	public M loadById(final I id) {
		CRUDResult<M> result = this.getServiceProxyAs(new TypeToken<CRUDServicesForModelObjectWithID<O,I,M>>() { /* nothing */ })
				   				   .loadById(this.getSecurityContext(),
							 				 id);
		M outRecord = result.getOrThrow();
		if (outRecord instanceof DirtyStateTrackable) {
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnLoaded(outRecord);
		}
		return outRecord;
	}
	/**
	 * Loads an object using it's id
	 * @param id
	 * @return
	 */
	public M loadByIdOrNull(final I id) {
		M outRecord = null;
		try {
			outRecord = this.loadById(id);
		} catch (PersistenceException persistEx) {
			if (!persistEx.isEntityNotFound()) throw persistEx;
		}
		return outRecord;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	public M deleteById(final I id) {
		CRUDResult<M> result = this.getServiceProxyAs(new TypeToken<CRUDServicesForModelObjectWithID<O,I,M>>() { /* nothing */ })
				   				   .deleteById(this.getSecurityContext(),
							 				   id);
		M outRecord = result.getOrThrow();
		if (outRecord instanceof DirtyStateTrackable) {
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnLoaded(outRecord);
		}
		return outRecord;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EXISTS
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Checks if an object exists using it's id
	 * @param id
	 * @return
	 */
	public boolean exists(final I id) {  // TypeToken vs TypeREf [todo] checks.
		PersistenceOperationResult<Boolean> result = this.getServiceProxyAs(new TypeToken<CRUDServicesForModelObjectWithID<O,I,M>>() { /* nothing */ })
														 .exists(this.getSecurityContext(),
							 				 					 id);	
		return result.getOrThrow();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Gets the id of an object using it's oid
	 * @param oid
	 * @return
	 */
	public I getIdOf(final O oid) {
		PersistenceOperationResult<I> result = this.getServiceProxyAs(new TypeRef<CRUDServicesForModelObjectWithID<O,I,M>>() { /* nothing */ })
												   .getIdOf(this.getSecurityContext(),
														    oid);	
		return result.getOrThrow();
	}
	/**
	 * Gets the oid of an object using it's id
	 * @param oid
	 * @return
	 */
	public O getOidOf(final I id) {
		PersistenceOperationResult<O> result = this.getServiceProxyAs(new TypeRef<CRUDServicesForModelObjectWithID<O,I,M>>() { /* nothing */ })
												   .getOidOf(this.getSecurityContext(),
														     id);	
		return result.getOrThrow();
	}
}
