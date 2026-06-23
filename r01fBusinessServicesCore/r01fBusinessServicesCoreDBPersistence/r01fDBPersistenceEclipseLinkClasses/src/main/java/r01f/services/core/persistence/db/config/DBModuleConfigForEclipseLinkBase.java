package r01f.services.core.persistence.db.config;

import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
public abstract class DBModuleConfigForEclipseLinkBase
    	   	  extends DBModuleConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBModuleConfigForEclipseLinkBase(final CoreAppCode appCode,
							  				final CoreModule appModule,
							  				final PersistenceUnitType unitType,
							  				final DBSpecBase dbSpec,
							  				final DBSchemaCreationForEclipseLink dbSchemaCreation,
							  				final Level logLevel,
							  				final Properties otherProps) {
		super(appCode, 
			  appModule, 
			  unitType, 
			  dbSpec, 
			  dbSchemaCreation, 
			  logLevel, 
			  otherProps);
	}
	public DBModuleConfigForEclipseLinkBase(final CoreAppCode appCode,
							  				final CoreModule appModule,
							  				final PersistenceUnitType unitType,
							  				final DBSpecBase dbSpec,
							  				final DBSchemaCreationForEclipseLink dbSchemaCreation,
							  				final Level logLevel) {
		super(appCode, 
			  appModule, 
			  unitType, 
			  dbSpec, 
			  dbSchemaCreation, 
			  logLevel);
	}
	public DBModuleConfigForEclipseLinkBase(final CoreAppCode appCode,
							  				final CoreModule appModule,
							  				final PersistenceUnitType unitType,
							  				final DBSpecBase dbSpec,
							  				final DBSchemaCreationForEclipseLink dbSchemaCreation) {
		super(appCode, 
			  appModule, 
			  unitType, 
			  dbSpec, 
			  dbSchemaCreation);
	}
	public DBModuleConfigForEclipseLinkBase(final XMLPropertiesForAppComponent xmlProps,
							  				final PersistenceUnitType unitType) {
		super(xmlProps,
			  unitType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	DBSpec _dbSpecFromString(final String dbSpecStr) {
		return DBEclipseLinkSpec.valueOf(dbSpecStr);
	}
	@Override
	DBSchemaCreationBase _dbSchemaCreationFrom(final AppCode appCode,
											   final DBSpec spec,
											   final XMLPropertiesForAppComponent xmlProps) {
		return new DBSchemaCreationForEclipseLink(appCode,
											      spec,
												  xmlProps);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Properties dbVendorProperties() {
		Properties props = new Properties();

		props.put(PersistenceUnitProperties.TARGET_DATABASE,_dbSpec.getVendor().getCode());	// MySQLPlatformExtension.class.getCanonicalName()

		// used when generating schema
		props.put(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME,_dbSpec.getVendor().getCode());
		props.put(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION,_dbSpec.getMajorVersion());
		props.put(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION,_dbSpec.getMinorVersion());

		// enable innoDB tables in MySql (needed for full-text searching)
		if (_dbSpec.getVendor().is(DBVendor.MySQL)) {
			props.put("eclipselink.ddl.default-table-suffix","engine=InnoDB");
		}
		return props;
	}
	@Override
	public Properties logProperties() {
		// Set the log level: see Logging: http://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
		Properties props = new Properties();
		props.put(PersistenceUnitProperties.LOGGING_LOGGER,"ServerLogger");
		props.put(PersistenceUnitProperties.LOGGING_LEVEL,_logLevel != null ? _logLevel.getName() : Level.INFO.getName());
		props.put(PersistenceUnitProperties.LOGGING_TIMESTAMP,"false");
		props.put(PersistenceUnitProperties.LOGGING_THREAD,"true");
		props.put(PersistenceUnitProperties.LOGGING_SESSION,"true");
		props.put(PersistenceUnitProperties.LOGGING_CONNECTION,"true");
		props.put(PersistenceUnitProperties.LOGGING_EXCEPTIONS,"true");
		props.put(PersistenceUnitProperties.LOGGING_PARAMETERS,"true");
		return props;
	}
}
