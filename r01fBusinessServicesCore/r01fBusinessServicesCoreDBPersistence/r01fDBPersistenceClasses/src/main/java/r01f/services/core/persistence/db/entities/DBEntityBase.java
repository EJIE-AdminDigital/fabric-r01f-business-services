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
import r01f.guids.OID;
import r01f.model.HasTrackingInfo;
import r01f.model.ModelObjectTracking;
import r01f.model.facets.HasEntityVersion;
import r01f.securitycontext.SecurityContext;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.securitycontext.SecurityOIDs.UserOID;
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
	/**
	 * Create date
	 */
	@Column(name="CREATED_AT",
			insertable=true,updatable=false)
	@Getter @Setter protected Instant _createDate; 			// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * Last update date
	 */
	@Column(name="LASTMODIFIED_AT",
			insertable=false,updatable=true)
	@Getter @Setter protected Instant _lastUpdateDate;		// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * The user code / application code of the creator
	 */
	@Column(name="CREATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creator;
	/**
	 * The user oid of the creator
	 */
	@Column(name="CREATED_BY_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creatorOid;
	/**
	 * The user code / application code of the last updator
	 */
	@Column(name="UPDATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdator;
	/**
	 * The user oid of the last updator
	 */
	@Column(name="UPDATED_BY_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdatorOid;
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
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Instant getCreateTimeStamp() {
		return _createDate;
	}
	@Override
	public void setCreateTimeStamp(final Instant newTS) {
		throw new UnsupportedOperationException("Do not call setCreateTimeStamp at the persistence layer!");
	}
	@Override
	public Instant getLastUpdateTimeStamp() {
		return _lastUpdateDate;
	}
	@Override
	public void setLastUpdateTimeStamp(final Instant newTS) {
		throw new UnsupportedOperationException("Do not call setLastUpdateTimeStamp at the persistence layer!");
	}
	@Override
	public LoginID getCreatorUserCode() {
		return Strings.isNOTNullOrEmpty(_creator) ? LoginID.forId(_creator) : null;
	}

	@Override
	public void setCreatorUserCode(final LoginID creator) {
		if (creator != null) _creator = creator.asString();
	}
	@Override
	public UserOID getCreatorUserOid() {
		return Strings.isNOTNullOrEmpty(_creatorOid) ? UserOID.forId(_creatorOid) : null;
	}
	@Override
	public void setCreatorUserOid(final UserOID creatorOid) {
		if (creatorOid != null) _creatorOid = creatorOid.asString();
	}
	public void setCreatorFrom(final SecurityContext securityContext) {
		this.setCreatorUserCode(securityContext.getLoginId());
		if (securityContext.isForUser()) this.setCreatorUserOid(securityContext.asForUser()
																			   .getUserOid());
	}
	@Override
	public LoginID getLastUpdatorUserCode() {
		return Strings.isNOTNullOrEmpty(_lastUpdator) ? LoginID.forId(_lastUpdator) : null;
	}
	@Override
	public void setLastUpdatorUserCode(final LoginID lastUpdator) {
		if (lastUpdator != null) _lastUpdator = lastUpdator.asString();
	}
	@Override
	public UserOID getLastUpdatorUserOid() {
		return Strings.isNOTNullOrEmpty(_lastUpdatorOid) ? UserOID.forId(_lastUpdatorOid) : null;
	}
	@Override
	public void setLastUpdatorUserOid(final UserOID lastUpdatorOid) {
		if (lastUpdatorOid != null) _lastUpdatorOid = lastUpdatorOid.asString();
	}
	public void setLastUpdatorFrom(final SecurityContext securityContext) {
		this.setLastUpdatorUserCode(securityContext.getLoginId());
		if (securityContext.isForUser()) this.setLastUpdatorUserOid(securityContext.asForUser()
																				   .getUserOid());
	}
	@Override
	public ModelObjectTracking getTrackingInfo() {
		ModelObjectTracking modelObjectTracking = new ModelObjectTracking();
		if (_createDate != null) modelObjectTracking.setCreateDate(_createDate);
		if (_lastUpdateDate != null) modelObjectTracking.setLastUpdateDate(_lastUpdateDate);
		if (_creatorOid != null) modelObjectTracking.setCreatorUserOid(UserOID.forId(_creatorOid));
		if (_creator != null) modelObjectTracking.setCreatorUserCode(LoginID.forId(_creator));
		if (_lastUpdatorOid != null) modelObjectTracking.setLastUpdatorUserOid(UserOID.forId(_lastUpdatorOid));
		if (_lastUpdator != null) modelObjectTracking.setLastUpdatorUserCode(LoginID.forId(_lastUpdator));
		return modelObjectTracking;
	}
	@Override
	public void setTrackingInfo(final ModelObjectTracking tracking) {
		ModelObjectTracking theTracking = tracking != null ? tracking
														   : new ModelObjectTracking();
		_createDate = theTracking.getCreateDate() != null ? theTracking.getCreateDate() : null;
		_lastUpdateDate = theTracking.getLastUpdateDate() != null ? theTracking.getLastUpdateDate() : null;
		_creatorOid = theTracking.getCreatorUserOid() != null ? theTracking.getCreatorUserOid().asString() : null;
		_creator = theTracking.getCreatorUserCode() != null ? theTracking.getCreatorUserCode().asString() : null;
		_lastUpdatorOid = theTracking.getLastUpdatorUserOid() != null ? theTracking.getLastUpdatorUserOid().asString() : null;
		_lastUpdator = theTracking.getLastUpdatorUserCode() != null ? theTracking.getLastUpdatorUserCode().asString() : null;
	}
}