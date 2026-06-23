package r01f.services.core.persistence.db.config;

import r01f.debug.Debuggable;

/**
 * Convenience interface to mark DBSpec implementation
 */
public interface DBSpec
		 extends Debuggable {

	DBVendor getVendor();

    int getMajorVersion();

    int getMinorVersion();

    DBPersistenceProviderVendorDBHints getVendorDBHints();

}