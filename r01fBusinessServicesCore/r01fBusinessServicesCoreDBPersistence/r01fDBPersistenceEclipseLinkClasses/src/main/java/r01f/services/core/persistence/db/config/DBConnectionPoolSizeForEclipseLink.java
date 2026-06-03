package r01f.services.core.persistence.db.config;

import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import r01f.guids.CommonOIDs.AppCode;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

public class DBConnectionPoolSizeForEclipseLink
     extends DBConnectionPoolSizeBase {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBConnectionPoolSizeForEclipseLink(final AppCode appCode,
							    			  final PersistenceUnitType unitType,
							    			  final DBSpec dbSpec,
							    			  final XMLPropertiesForAppComponent xmlProps) {
		super(appCode,
			  unitType,
			  dbSpec,
			  xmlProps);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Properties asProperties() {
		Properties props = new Properties();
		if (_initial != null) props.put("eclipselink.jdbc.connection_pool.default." + PersistenceUnitProperties.CONNECTION_POOL_INITIAL,_initial);
		if (_min != null) 	  props.put("eclipselink.jdbc.connection_pool.default." + PersistenceUnitProperties.CONNECTION_POOL_MIN,_min);
		if (_max != null) 	  props.put("eclipselink.jdbc.connection_pool.default." + PersistenceUnitProperties.CONNECTION_POOL_MAX,_max);
		return props;
	}
}

