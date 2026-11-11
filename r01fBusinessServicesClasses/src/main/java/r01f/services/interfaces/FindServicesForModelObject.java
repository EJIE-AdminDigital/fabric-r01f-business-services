package r01f.services.interfaces;


import java.time.Instant;
import java.util.stream.Stream;

import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.FindResult;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectOID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.types.Range;

/**
 * Finding
 * @param <O>
 * @param <M>
 */
public interface FindServicesForModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
		 extends ServiceInterfaceForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FINDING
/////////////////////////////////////////////////////////////////////////////////////////
////////// --- FIND ALL
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findAll(final SecurityContext securityContext);
	/**
	 * Finds all persisted model object oids as a {@link Stream}
	 * @param securityContext
	 * @return
	 */
	public Stream<O> findAllAsStream(final SecurityContext securityContext);
////////// --- FIND BY CREATE DATE
	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param createDate
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByCreateDate(final SecurityContext securityContext,
											  final Range<Instant> createDate);
	/**
	 * Finds all persisted model object oids which create date is in the provided range as a {@link Stream}
	 * @param securityContext
	 * @param createDate
	 * @return
	 */
	public Stream<O> findByCreateDateAsStream(final SecurityContext securityContext,
											  final Range<Instant> createDate);
////////// --- FIND BY LAST UPDATE DATE
	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext the user auth data & context info
	 * @param lastUpdateDate
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByLastUpdateDate(final SecurityContext securityContext,
											  	  final Range<Instant> lastUpdateDate);
	/**
	 * Finds all persisted model object oids which last update date is in the provided range as a {@link Stream}
	 * @param securityContext
	 * @param lastUpdateDate
	 * @return
	 */
	public Stream<O> findByLastUpdateDateAsStream(final SecurityContext securityContext,
											  	  final Range<Instant> lastUpdateDate);
////////// --- FIND BY CREATOR OID	
	/**
	 * Finds all persisted model object oids created by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param creatorPrincipalOid
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
									   	   final SecurityPrincipalOID creatorPrincipalOid);
	/**
	 * Finds all persisted model object oids created by the provided [principal] as a {@link Stream}
	 * @param securityContext
	 * @param creatorPrincipalOid
	 * @return
	 */
	public Stream<O> findByCreatorAsStream(final SecurityContext securityContext,
									   	   final SecurityPrincipalOID creatorPrincipalOid);
////////// --- FIND BY CREATOR ID
	/**
	 * Finds all persisted model object oids created by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param creatorPrincipalId
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
									   	   final SecurityPrincipalID creatorPrincipalId);
	/**
	 * Finds all persisted model object oids created by the provided [principal] as a {@link Stream}
	 * @param securityContext
	 * @param creatorPrincipalId
	 * @return
	 */
	public Stream<O> findByCreatorAsStream(final SecurityContext securityContext,
									   	   final SecurityPrincipalID creatorPrincipalId);
////////// --- FIND BY LAST UPDATER	OID
	/**
	 * Finds all persisted model object oids last updated by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param updaterPrincipalOid
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
										   	   final SecurityPrincipalOID updaterPrincipalOid);
	/**
	 * Finds all persisted model object oids last updated by the provided [principal] as a {@link Stream}
	 * @param securityContext
	 * @param updaterPrincipalOid
	 * @return
	 */
	public Stream<O> findByLastUpdaterAsStream(final SecurityContext securityContext,
										   	   final SecurityPrincipalOID updaterPrincipalOid);
////////// --- FIND BY LAST UPDATER ID	
	/**
	 * Finds all persisted model object oids last updated by the provided [principal]
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the
	 * currently active versions
	 * @param securityContext 
	 * @param updaterPrincipalId
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
										   	   final SecurityPrincipalID updaterPrincipalId);
	/**
	 * Finds all persisted model object oids last updated by the provided [principal] as a {@link Stream}
	 * @param securityContext
	 * @param updaterPrincipalId
	 * @return
	 */
	public Stream<O> findByLastUpdaterAsStream(final SecurityContext securityContext,
										   	   final SecurityPrincipalID updaterPrincipalId);
}
