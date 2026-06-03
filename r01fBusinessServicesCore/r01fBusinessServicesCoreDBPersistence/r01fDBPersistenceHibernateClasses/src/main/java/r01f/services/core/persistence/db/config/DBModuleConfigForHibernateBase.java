package r01f.services.core.persistence.db.config;

import java.util.Properties;
import java.util.logging.Level;

import org.hibernate.cfg.AvailableSettings;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.AppCode;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.xmlproperties.XMLPropertiesForAppComponent;


@Accessors(prefix="_")
public abstract class DBModuleConfigForHibernateBase
			  extends DBModuleConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBModuleConfigForHibernateBase(final CoreAppCode appCode,
							 			  final CoreModule appModule,
							  			  final PersistenceUnitType unitType,
							  			  final DBSpecBase dbSpec,
							  			  final DBSchemaCreationForHibernate dbSchemaCreation,
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
	public DBModuleConfigForHibernateBase(final CoreAppCode appCode,
							  			  final CoreModule appModule,
							  			  final PersistenceUnitType unitType,
							  			  final DBSpecBase dbSpec,
							  			  final DBSchemaCreationForHibernate dbSchemaCreation,
							  			  final Level logLevel) {
		super(appCode,
			  appModule, 
			  unitType, 
			  dbSpec, 
			  dbSchemaCreation, 
			  logLevel);
	}
	public DBModuleConfigForHibernateBase(final CoreAppCode appCode,
							  			  final CoreModule appModule,
							  			  final PersistenceUnitType unitType,
							  			  final DBSpecBase dbSpec,
							  			  final DBSchemaCreationForHibernate dbSchemaCreation) {
		super(appCode,
			  appModule, 
			  unitType, 
			  dbSpec, 
			  dbSchemaCreation);
	}
	public DBModuleConfigForHibernateBase(final XMLPropertiesForAppComponent xmlProps,
							  final PersistenceUnitType unitType) {
		super(xmlProps, 
			  unitType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	DBSpec _dbSpecFromString(final String dbSpecStr) {
		return DBHibernateSpec.valueOf(dbSpecStr);
	}
	@Override
	DBSchemaCreationBase _dbSchemaCreationFrom(final AppCode appCode,
											   final DBSpec spec,
											   final XMLPropertiesForAppComponent xmlProps) {
		return new DBSchemaCreationForHibernate(appCode,
											    spec,
												xmlProps);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Properties dbVendorProperties() {
		Properties props = new Properties();
		
		// Configure the dialect based on the vendor
		props.put(AvailableSettings.DIALECT, getHibernateDialectForVendor(_dbSpec.getVendor()));
		
		// Database product information (optional, not strictly necessary)
		props.put(AvailableSettings.JAKARTA_HBM2DDL_DB_VERSION, _dbSpec.getVendor()
																	   .getCode());
		props.put(AvailableSettings.JAKARTA_HBM2DDL_DB_MAJOR_VERSION, _dbSpec.getMajorVersion());
		props.put(AvailableSettings.JAKARTA_HBM2DDL_DB_MINOR_VERSION, _dbSpec.getMinorVersion());

		// enable innoDB tables in MySql (needed for full-text searching)
		// DDL configuration to enable InnoDB on MySQL (simulating EclipseLink behavior)
		if (_dbSpec.getVendor().is(DBVendor.MySQL)) {
		    // In Hibernate, we don't use 'eclipselink.ddl.default-table-suffix', but we can customize schema generation
		    props.put(AvailableSettings.HBM2DDL_AUTO, org.hibernate.tool.schema.Action.ACTION_UPDATE); // Can be 'create', 'update', etc.
		    props.put(AvailableSettings.STORAGE_ENGINE, "innodb"); // Customize table creation with InnoDB
		}
		return props;
	}
	private static String getHibernateDialectForVendor(final DBVendor vendorCode) {
	    switch (vendorCode) {
	        case MySQL:
	            return DBDialect.MySQL.getCode();
	        case PostgreSQL:
	            return DBDialect.PostgreSQL.getCode();
	        case ORACLE:
	            return DBDialect.ORACLE.getCode();
	        default:
	            return DBDialect.MySQL.getCode();
	    }
	}
	@Override
	public Properties logProperties() {
		// Set the log level: see Logging: http://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
		Properties props = new Properties();
		props.put(AvailableSettings.SHOW_SQL, "true");
		props.put(AvailableSettings.FORMAT_SQL, "false");
		props.put(AvailableSettings.USE_SQL_COMMENTS, "true");
		return props;
	}
}
