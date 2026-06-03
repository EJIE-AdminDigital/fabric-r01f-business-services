package r01f.services.core.persistence.db.config;

import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import r01f.file.FileName;
import r01f.guids.CommonOIDs.AppCode;
import r01f.types.Path;
import r01f.util.types.StringConverter;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

public class DBSchemaCreationForEclipseLink 
     extends DBSchemaCreationBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBSchemaCreationForEclipseLink(final DBSchemaTablesDDLActionForEclipseLink ddlAction,
										  final Path ddlScriptPath,
									   	  final FileName ddlCreateFileName,final FileName ddlDropFileName,
										  final Path dataLoadScriptPath,
										  final Properties otherProps) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  dataLoadScriptPath,
			  otherProps);
	}
	public DBSchemaCreationForEclipseLink(final DBSchemaTablesDDLActionForEclipseLink ddlAction,
										  final Path ddlScriptPath,final FileName ddlCreateFileName,final FileName ddlDropFileName,
										  final Path dataLoadScriptPath) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  dataLoadScriptPath,
			  (Properties)null);
	}
	public DBSchemaCreationForEclipseLink(final DBSchemaTablesDDLActionForEclipseLink ddlAction,
										  final Path ddlScriptPath,final FileName ddlCreateFileName,final FileName ddlDropFileName,
										  final Properties otherProps) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  null,
			  otherProps);
	}
	public DBSchemaCreationForEclipseLink(final DBSchemaTablesDDLActionForEclipseLink ddlAction,
										  final Path ddlScriptPath,final FileName ddlCreateFileName,final FileName ddlDropFileName) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  null,
			  (Properties)null);
	}
	public DBSchemaCreationForEclipseLink(final DBSchemaTablesDDLActionForEclipseLink ddlAction,
										  final Path dataLoadScriptPath,
										  final Properties otherProps) {
		super(ddlAction,
			  null,
			  null,null,
			  dataLoadScriptPath,
			  otherProps);
	}
	public DBSchemaCreationForEclipseLink(final DBSchemaTablesDDLActionForEclipseLink ddlAction,
										  final Path dataLoadScriptPath) {
		super(ddlAction,
			  null,
			  null,null,
			  dataLoadScriptPath,
			  (Properties)null);
	}
	public DBSchemaCreationForEclipseLink(final AppCode appCode,
										  final DBSpec spec,
										  final XMLPropertiesForAppComponent xmlProps) {
		super(appCode,
			  spec,
			  xmlProps);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	DBSchemaTablesDDLAction _DDLActionFromString(final String ddlActionStr) {
		// Generate schema
		// see
		//		https://wiki.eclipse.org/EclipseLink/Release/2.5/JPA21
		//		http://www.eclipse.org/eclipselink/documentation/2.5/jpa/extensions/p_ddl_generation_output_mode.htm
		//		http://wiki.eclipse.org/EclipseLink/DesignDocs/368365)
		return StringConverter.wrap(ddlActionStr)
					   		  .asEnumElement(DBSchemaTablesDDLActionForEclipseLink.class)
					   		  .orDefault(DBSchemaTablesDDLActionForEclipseLink.NONE);
	}
	@Override
	public Properties asProperties() {
		Properties props = new Properties();

		if (_ddlAction == DBSchemaTablesDDLActionForEclipseLink.NONE) return props;

		// [1] == Schema generation from the object/relational mapping metadata
		// specifies the action to be taken by the persistence provider with regard to the database artifacts

		// props.put(PersistenceUnitProperties.SCHEMA_GENERATION_CONNECTION,"");
		props.put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_DATABASE_SCHEMAS,"false");						// do NOT generate the schema
		props.put(PersistenceUnitProperties.DDL_GENERATION_MODE,PersistenceUnitProperties.DDL_BOTH_GENERATION);		// database / sql-script / both

		// specifies whether the creation of database artifacts is to occur on the basis of
		//		(1) the object/relational mapping metadata,
		//		(2) DDL script,
		//		(3) a combination (1) and (2)
       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SOURCE,"metadata");			// metadata / script / metadata-then-script / script-then-metadata
       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SOURCE,"metadata");				// metadata / script / metadata-then-script / script-then-metadata

       	if (_ddlAction != null) {
	       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION,_ddlAction.getJpaAction());	// none / create / create-or-extend-tables / drop-and-create / drop
	       	props.put(PersistenceUnitProperties.DDL_GENERATION,_ddlAction.getVendorAction());				// none / create-tables / create-or-extend-tables / drop-and-create-tables

			// Generate indexes for foreign keys: http://java-persistence-performance.blogspot.com.es/2013/06/cool-performance-features-of.html
			props.put(PersistenceUnitProperties.DDL_GENERATION_INDEX_FOREIGN_KEYS,"true");
       	}

		// [2] == Schema generation from DDL scripts
		// specify the file system directory in which EclipseLink writes (outputs) DDL files. - See more at: http://www.eclipse.org/eclipselink/documentation/2.5/jpa/extensions/p_application_location.htm#CACHGDEJ
		if (_ddlScriptPath != null) {
			props.put(PersistenceUnitProperties.APP_LOCATION,_ddlScriptPath.asAbsoluteString());

			// specifies which scripts are to be generated by the persistence provider
	       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION,"drop-and-create");		// none / create / drop-and-create / drop

	       	// props.put("PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE","META-INF/create.sql");
			// props.put("PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SCRIPT_SOURCE,"META-INF/drop.jdbc");


			// If JPA GENERATES the schema generation scripts to be executed later
			props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET,_ddlCreateFileName.asString());
			props.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE,_ddlCreateFileName.asString());

			props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET,_ddlDropFileName.asString());
			props.put(PersistenceUnitProperties.DROP_JDBC_DDL_FILE,_ddlDropFileName.asString());
		}

		// [3] == Initial load script
		// If the database has to be loaded with initial data this property specifies the
		// SQL load script for database initialization
		if (_dataLoadScriptPath != null) {
			props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SQL_LOAD_SCRIPT_SOURCE,_dataLoadScriptPath.asAbsoluteString());
		}

		// [4] == Other properties
		if (_otherProps != null) props.putAll(_otherProps);

		return props;
	}
}

