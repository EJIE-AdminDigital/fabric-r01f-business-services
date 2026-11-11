package r01f.services.api.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;
import r01f.oids.refs.IsPersistableObjectWithIDRef;
import r01f.oids.refs.IsPersistableObjectWithIDRef.PersistableObjectWithIDRefFactory;

/**
 * A Bi-directional map to cache the OID-ID associations for the objects.
 * Just extend like:
 * <pre class='brush:java'>
 *		@Singleton
 *		@Accessors(prefix="_")
 *		public class SamplePersistableObjRefSolver
 *		     extends PersistableObjectRefSolverBase<SamplePersistableObjOID,SamplePersistableObjID> {
 *
 *			@Inject
 *			public SamplePersistableObjRefSolver(// the system security context provider needed for the cache
 *							@SecurityContextProviderForSystemUser final Provider<SecurityContext> securityContextProvider,
 *												// crud services
 *												final CRUDServicesForSamplePersistableObj crud) {
 *				super(// retrieve the ID of given the OID
 *					  oid -> crud.getIdOf(securityContextProvider.get(),
 *												oid)
 *										.getOrDefault(null),
 *					  // retrieve the OID given the ID
 *					  id -> crud.getOidOf(securityContextProvider.get(),
 *												id)
 *					  				  .getOrDefault(null));
 *			}
 *			public SamplePersistableObjRefSolver(final SecurityContext securityContext,
 *									   			 final CRUDServicesForSamplePersistableObj crud) {
 *				super(// retrieve the ID given the OID
 *					  oid -> crud.getIdOf(securityContext,
 *										  oid)
 *								 .getOrDefault(null),
 *					  // retrieve the OID given the ID
 *					  id -> crud.getOidOf(securityContext,
 *										  id)
 *					  			.getOrDefault(null));
 *			}
 *		
 *			public SamplePersistableObjRef completeRefIfNeeded(final SamplePersistableObjRef ref) {
 *				return super.completeRefIfNeeded(ref,
 *												 SamplePersistableObjRef::new);
 *			}
 *		}
 * </pre>
 * @param <O>
 * @param <I>
 */
@Slf4j
public abstract class PersistableObjectRefSolverBase<O extends PersistableObjectOID,I extends PersistableObjectID<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A bidirectional map to cache the OID-ID associations for the objects in the cache.
	 */
	protected final BidirMapForOIDAndID _oidAndIdBidiMap = new BidirMapForOIDAndID();
	
	protected final Function<O,I> _oidToIdResolver;
	protected final Function<I,O> _idToOidResolver;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected PersistableObjectRefSolverBase(final Function<O,I> oidToIdResolver,
										     final Function<I,O> idToOidResolver) {
		_oidToIdResolver = oidToIdResolver;
		_idToOidResolver = idToOidResolver;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public I getIdOf(final O oid) {
		I outId = null;
		
		// [1] - Try the cache
		outId = _oidAndIdBidiMap.getIdOf(oid);
		if (outId != null) return outId;
		
		// [2] - The ID-OID association is not in the cache retrieve it 
		outId = _oidToIdResolver.apply(oid);
		if (outId != null) _oidAndIdBidiMap.put(oid,outId);	// cache the association for future use
		
		// [99] - Return the ID (if any)
		return outId;
	}
	public O getOidOf(final I id) {
		O outOid = null;		
		// [1] - Try the cache
		outOid = _oidAndIdBidiMap.getOidOf(id);
		
		if (outOid != null) return outOid;
		
		// [2] - The OID-ID association is not in the cache retrieve it
		outOid = _idToOidResolver.apply(id);
		if (outOid != null) _oidAndIdBidiMap.put(outOid,id);	// cache the association for future use	
		
		// [99] - Return the OID (if any)
		return outOid;
	}
	public <R extends IsPersistableObjectWithIDRef<O,I>> R getRefOf(final O oid,
																	final PersistableObjectWithIDRefFactory<O,I,R> refFactory) {
		I id = this.getIdOf(oid);
		return refFactory.createRefFrom(oid,id);
	}
	public <R extends IsPersistableObjectWithIDRef<O,I>> R getRefOf(final I id,
																	final PersistableObjectWithIDRefFactory<O,I,R> refFactory) {
		O oid = this.getOidOf(id);
		return refFactory.createRefFrom(oid,id);
	}
	public <R extends IsPersistableObjectWithIDRef<O,I>> R completeRefIfNeeded(final R ref,
								 											   final PersistableObjectWithIDRefFactory<O,I,R> refFactory) {
		if (ref == null) return null;
		
		R outRef = null;
		if (ref.getOid() != null && ref.getId() != null) {
			// already complete ref
			outRef = refFactory.createRefFrom(ref.getOid(),ref.getId());	// clone the ref to avoid modifying the original one
			
			// check if the ref is valid 
			// BEWARE that this MIGHT issue a DB hit to retrieve the ID of the OID if the ID-OID association is NOT cached
			I loadedOrCachedId = this.getIdOf(ref.getOid());

			// if the loaded ID is NOT null and is NOT equal to the received ID, then the ref is NOT valid
			if (loadedOrCachedId != null && loadedOrCachedId.isNOT(ref.getId())) {
				log.error("The ref {} is NOT VALID: the OID {} is associated with the ID {} but the received ID is {}",
						  ref,ref.getOid(),loadedOrCachedId,ref.getId()); 
				outRef.setId(null);	// invalidate the ID of the ref
			} else if (loadedOrCachedId == null) {
				log.warn("The ref {} is NOT VALID: the OID does NOT exists",ref);
				outRef.setId(null);	// invalidate the ID of the ref
			}
		} else if (ref.getOid() != null) {
			// ref with OID but no ID: retrieve the ID and complete the ref
			I id = this.getIdOf(ref.getOid());	// might be null if the OID is not known by the origin admin
			outRef = refFactory.createRefFrom(ref.getOid(),id);
		} else if (ref.getId() != null) {
			// ref with ID but no OID: retrieve the OID and complete the ref
			O oid = this.getOidOf(ref.getId());	// might be null if the ID is not known by the origin admin
			outRef = refFactory.createRefFrom(oid,ref.getId());
		}
		return outRef;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	BidiMap between OID and ID (alternative to Guava's BiMap or Apache Commons BidiMap)
/////////////////////////////////////////////////////////////////////////////////////////	
	private class BidirMapForOIDAndID {
		private final Map<O,I> _oidToId = new ConcurrentHashMap<>();
		private final Map<I,O> _idToOid = new ConcurrentHashMap<>();
		private final Lock _lock = new ReentrantLock();
		
		public void put(final O oid,final I id) {
			if (oid == null || id == null) throw new IllegalArgumentException("OID and ID cannot be null");
			
			// use a ReentrantLock that ensures that the put and remove operations are atomic.
			// AVOID an state where _oidToId has the data but idToOid doesn't yet
			_lock.lock();
	        try {
	            // a. If [id from oid] exists, remove the mapping from the [oid from id] map
	            I idFRomOid = _oidToId.get(oid);
	            if (idFRomOid != null) _idToOid.remove(idFRomOid);
	
	            // b. If [oid from id] exists, remove the mapping from the [id from oid] map
	            O oidFromId = _idToOid.get(id);
	            if (oidFromId != null) _oidToId.remove(oidFromId);
	
	            // c. Perform the actual insertion
	            _oidToId.put(oid,id);
	            _idToOid.put(id,oid);
	        } finally {
	            _lock.unlock();
	        }
		}
		@SuppressWarnings("unused")
		public boolean removeByOid(final O oid) {
			_lock.lock();
	        try {
	            I idFromOid = _oidToId.remove(oid);
	            if (idFromOid != null) _idToOid.remove(idFromOid);
	            return idFromOid != null;
	        } finally {
	            _lock.unlock();
	        }
		}
		@SuppressWarnings("unused")
		public boolean removeById(final I id) {
			_lock.lock();
	        try {
	            O oidFromId = _idToOid.remove(id);
	            if (oidFromId != null) _oidToId.remove(oidFromId);
	            return oidFromId != null;
	        } finally {
	            _lock.unlock();
	        }
		}
		@SuppressWarnings("unused")
		public boolean containsOid(final O oid) {
			return _oidToId.containsKey(oid);
		}
		@SuppressWarnings("unused")
		public boolean containsId(final I id) {
			return _idToOid.containsKey(id);
		}
		public I getIdOf(final O oid) {
			return _oidToId.get(oid);
		}
		public O getOidOf(final I id) {
			return _idToOid.get(id);
		}
		@SuppressWarnings("unused")
		public void debugIdToOidMap() {
			log.warn("ConcurrentHashMap contents: {}", _idToOid);
		}
		@SuppressWarnings("unused")
		public void debugOidToIdMap() {
			log.warn("ConcurrentHashMap contents: {}", _oidToId);
		}
	}
}
