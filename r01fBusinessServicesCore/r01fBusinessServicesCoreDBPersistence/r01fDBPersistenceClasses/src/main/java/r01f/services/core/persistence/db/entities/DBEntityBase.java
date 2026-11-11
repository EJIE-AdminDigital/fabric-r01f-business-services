package r01f.services.core.persistence.db.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.model.HasTrackingInfo;
import r01f.model.ModelObjectTracking;
import r01f.model.facets.HasEntityVersion;
import r01f.model.security.context.SecurityContext;
import r01f.oids.OID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.services.core.persistence.db.DBEntity;
import r01f.util.types.Strings;

/**
 * An entity
 * @param <R>
 */
@MappedSuperclass

@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityBase
		   implements DBEntity,
		   			  HasEntityVersion,
		   			  HasTrackingInfo {

	private static final long serialVersionUID = 7091182557128314416L;

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
////////// --- CREATE
	/**
	 * Create date
	 */
	@Column(name="CREATED_AT",
			insertable=true,updatable=false)
	@Getter @Setter protected Instant _createDate; 			// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * The creator [security principal] id
	 */
	@Column(name="CREATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creator;
	/**
	 * The creator [security principal] oid
	 */
	@Column(name="CREATED_BY_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creatorOid;
	
////////// --- UPDATE
	/**
	 * Last update date
	 */
	@Column(name="LASTMODIFIED_AT",
			insertable=false,updatable=true)
	@Getter @Setter protected Instant _lastUpdateDate;		// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/	
	/**
	 * The updater [security principal] id
	 */
	@Column(name="UPDATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdater;
	/**
	 * The updater [security principal] oid
	 */
	@Column(name="UPDATED_BY_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdaterOid;
	
////////// --- IMPERSONATION
	/**
	 * The impersonating [security principal] oid (if any)
	 */
	@Column(name="IMPERSONATING_PRINCIPAL_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _impersonatingPrincipalOid;	
	
////////// --- ENTITY VERSION
	/**
	 * Entity Version to prevent conflicts
	 * See:
	 * 		http://www.eclipse.org/eclipselink/documentation/2.5/concepts/descriptors002.htm#CHEEEIEA
	 * 		http://en.wikibooks.org/wiki/Java_Persistence/Locking
	 *
	 * Optimistic locking is used (assumed that conflicts are unlike to happen)
	 * i.e. There are two web processes running in parallel, both processing the
	 * 		stock of an store item
	 * 		... let's say that initially we have stock=100
	 * 			----------[100]----------
	 * 			|						|
	 * 		  Load                    Load
	 *          |-1						|-1
	 *        [99]                     [99]
	 *          |						|
	 *        Save                    Save
	 *          |---------[99]			|
	 *          		  [99]----------| <---WTF!! the stock should have been 98
	 *          									but it ends being 99: WRONG!!
	 * To prevent this situation a last update timestamp or an incrementing version is used
	 * Every time a process want to update an entity it MUST tell us what the version is so
	 * if a conflict occurs it could be detected:
	 *
	 * 			----------[100]----------
	 * 			|	   (version=1)		|
	 * 			|						|
	 * 	      Load 				       Load
	 * 	   (version=1) 		       (version=1)
	 *          |-1						|-1
	 *        [99]                     [99]
	 *          |						|
	 *        Save                      |
	 *     (version=1)                  |
	 *          |---------[99]			|
	 *          	   (version=2)		|
	 *          			|		   Save
	 *          		CONFLICT!<--(Version=1)
	 *
	 * As seen, to be able to detect conflicts:
	 * 		- A version number (a timestamp) MUST be stored with the record
	 * 		- The version number MUST be loaded alongside the record and stored at the processing client
	 * 		- The version number MUST be send alongside the record in any update operation
	 * 		  so the received version could be compared with the provided one
	 * IMPORTANT!!
	 * 		Using ECLIPSELINK, set @OptimisticLocking(type=OptimisticLockingType.VERSION_COLUMN,
	 *			   									  cascade=true)
	 *	    at the entity type
	 */
	@Column(name="ENTITY_VERSION") @Version
	@Getter @Setter protected long _entityVersion;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  AUTO-UPDATE CREATE_DATE AND LAST_UPDATE_DATE
/////////////////////////////////////////////////////////////////////////////////////////
	@PrePersist
	void createdAt() {
		_createDate = Instant.now();
		_lastUpdateDate = _createDate;
		_preCreate();
	}
	@PreUpdate
	void updatedAt() {
		_lastUpdateDate = Instant.now();
		_preUpdate();
	}
	protected abstract void _preCreate();
	protected abstract void _preUpdate();

/////////////////////////////////////////////////////////////////////////////////////////
// 	CREATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Instant getCreatedAt() {
		return _createDate;
	}
	@Override
	public void setCreatedAt(final Instant newTS) {
		throw new UnsupportedOperationException("Do not call setCreatedAt at the persistence layer!");
	}
	@Override
	public SecurityPrincipalID getCreatorSecurityPrincipalId() {
		return Strings.isNOTNullOrEmpty(_creator) ? SecurityPrincipalID.forId(_creator) : null;
	}
	@Override
	public void setCreatorSecurityPrincipalId(final SecurityPrincipalID creator) {
		_creator = creator != null ? creator.asString() : null;
	}
	@Override
	public SecurityPrincipalOID getCreatorSecurityPrincipalOid() {
		return Strings.isNOTNullOrEmpty(_creatorOid) ? SecurityPrincipalOID.forId(_creatorOid) : null;
	}
	@Override
	public void setCreatorSecurityPrincipalOid(final SecurityPrincipalOID creatorOid) {
		_creatorOid = creatorOid != null ? creatorOid.asString() : null;
	}
	public void setCreatorFrom(final SecurityContext securityContext) {
		this.setCreatorSecurityPrincipalId(securityContext.getSecurityPrincipalId());
		this.setCreatorSecurityPrincipalOid(securityContext.getSecurityPrincipal()
														   .getOid());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	LAST UPDATE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Instant getLastUpdatedAt() {
		return _lastUpdateDate;
	}
	@Override
	public void setLastUpdatedAt(final Instant newTS) {
		throw new UnsupportedOperationException("Do not call setLastUpdatedAt at the persistence layer!");
	}
	@Override
	public SecurityPrincipalID getLastUpdaterSecurityPrincipalId() {
		return Strings.isNOTNullOrEmpty(_lastUpdater) ? SecurityPrincipalID.forId(_lastUpdater) : null;
	}
	@Override
	public void setLastUpdaterSecurityPrincipalId(final SecurityPrincipalID lastUpdater) {
		_lastUpdater = lastUpdater != null ? lastUpdater.asString() : null;
	}
	@Override
	public SecurityPrincipalOID getLastUpdaterSecurityPrincipalOid() {
		return Strings.isNOTNullOrEmpty(_lastUpdaterOid) ? SecurityPrincipalOID.forId(_lastUpdaterOid) : null;
	}
	@Override
	public void setLastUpdaterSecurityPrincipalOid(final SecurityPrincipalOID lastUpdaterOid) {
		_lastUpdaterOid = lastUpdaterOid != null ? lastUpdaterOid.asString() : null;
	}
	public void setLastUpdaterFrom(final SecurityContext securityContext) {
		this.setLastUpdaterSecurityPrincipalId(securityContext.getSecurityPrincipalId());
		this.setLastUpdaterSecurityPrincipalOid(securityContext.getSecurityPrincipal()
														       .getOid());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	IMPERSONATION
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public SecurityPrincipalOID getImpersonatingSecurityPrincipalOid() {
		return Strings.isNOTNullOrEmpty(_impersonatingPrincipalOid) ? SecurityPrincipalOID.forId(_impersonatingPrincipalOid) : null;
	}
	@Override
	public void setImpersonatingSecurityPrincipalOid(final SecurityPrincipalOID impersonatingPrincipalOid) {
		_impersonatingPrincipalOid = impersonatingPrincipalOid != null ? impersonatingPrincipalOid.asString() : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public ModelObjectTracking getTrackingInfo() {
		ModelObjectTracking modelObjectTracking = new ModelObjectTracking();
		
		// create
		if (_createDate != null) modelObjectTracking.setCreatedAt(_createDate);
		if (_creatorOid != null) modelObjectTracking.setCreatorSecurityPrincipalOid(SecurityPrincipalOID.forId(_creatorOid));
		if (_creator != null) modelObjectTracking.setCreatorSecurityPrincipalId(SecurityPrincipalID.forId(_creator));
		
		// last update
		if (_lastUpdateDate != null) modelObjectTracking.setLastUpdatedAt(_lastUpdateDate);
		if (_lastUpdaterOid != null) modelObjectTracking.setLastUpdaterSecurityPrincipalOid(SecurityPrincipalOID.forId(_lastUpdaterOid));
		if (_lastUpdater != null) modelObjectTracking.setLastUpdaterSecurityPrincipalId(SecurityPrincipalID.forId(_lastUpdater));
		
		// impersonation
		if (_impersonatingPrincipalOid != null) modelObjectTracking.setImpersonatingSecurityPrincipalOid(SecurityPrincipalOID.forId(_impersonatingPrincipalOid));
		
		return modelObjectTracking;
	}
	@Override
	public void setTrackingInfo(final ModelObjectTracking tracking) {
		ModelObjectTracking theTracking = tracking != null ? tracking
														   : new ModelObjectTracking();
		// create (beware!! do not use this.setCreatedAt()
		_createDate = theTracking.getCreatedAt() != null ? theTracking.getCreatedAt() : null;
		_creatorOid = theTracking.getCreatorSecurityPrincipalOid() != null ? theTracking.getCreatorSecurityPrincipalOid().asString() : null;
		_creator = theTracking.getCreatorSecurityPrincipalId() != null ? theTracking.getCreatorSecurityPrincipalId().asString() : null;
		
		// last update
		_lastUpdateDate = theTracking.getLastUpdatedAt() != null ? theTracking.getLastUpdatedAt() : null;
		_lastUpdaterOid = theTracking.getLastUpdaterSecurityPrincipalOid() != null ? theTracking.getLastUpdaterSecurityPrincipalOid().asString() : null;
		_lastUpdater = theTracking.getLastUpdaterSecurityPrincipalId() != null ? theTracking.getLastUpdaterSecurityPrincipalId().asString() : null;
		
		// impersonation
		_impersonatingPrincipalOid = theTracking.getImpersonatingSecurityPrincipalOid() != null ? theTracking.getImpersonatingSecurityPrincipalOid().asString() : null;
	}
}