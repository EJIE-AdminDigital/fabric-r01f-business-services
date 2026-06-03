package r01f.services.core.persistence.db.entities;


import java.time.Instant;

import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForVersionableModelObject;


/**
 * Interface for versionable DB entities
 * @param <R>
 */
public interface DBEntityForVersionableModelObject<PK extends DBPrimaryKeyForVersionableModelObject>
		 extends DBEntityForModelObject<PK> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the version independent oid
	 */
	public abstract String getVersionIndependentOid();
	/**
	 * @param versionIndOid
	 */
	public abstract void setVersionIndependentOid(final String versionIndOid);
	/**
	 * @return the entity version
	 */
	public abstract String getVersion();
	/**
	 * @param version the entity version
	 */
	public abstract void setVersion(String version);
	/**
	 * @return the start of use date (the date when this version is set to be active)
	 */
	public abstract Instant getStartOfUseDate();
	/**
	 * Version start of use date (the date when this version is set to be active)
	 */
	public abstract void setStartOfUseDate(Instant date);
	/**
	 * Version end of use date (the date when another version was created and this one becomes obsolete)
	 */
	public abstract Instant getEndOfUseDate();
	/**
	 * @param date Version end of use date (the date when another version was created and this one becomes obsolete)
	 */
	public abstract void setEndOfUseDate(Instant date);
	/**
	 * Next version identifier (if this version is not the current version)
	 */
	public abstract String getNextVersion();
	/**
	 * @param version Next version identifier (if this version is not the current version)
	 */
	public abstract void setNextVersion(String version);

}
