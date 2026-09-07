package r01f.services.core.persistence.db.config;

import java.util.Properties;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.SchemaToolingSettings;
import org.hibernate.tool.schema.Action;

import r01f.file.FileName;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.types.Path;
import r01f.util.types.StringConverter;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

public class DBSchemaCreationForHibernate 
     extends DBSchemaCreationBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBSchemaCreationForHibernate(final DBSchemaTablesDDLActionForHibernate ddlAction,
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
	public DBSchemaCreationForHibernate(final DBSchemaTablesDDLActionForHibernate ddlAction,
										final Path ddlScriptPath,
										final FileName ddlCreateFileName,final FileName ddlDropFileName,
										final Path dataLoadScriptPath) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  dataLoadScriptPath,
			  (Properties)null);
	}
	public DBSchemaCreationForHibernate(final DBSchemaTablesDDLActionForHibernate ddlAction,
										final Path ddlScriptPath,
										final FileName ddlCreateFileName,final FileName ddlDropFileName,
										final Properties otherProps) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  null,
			  otherProps);
	}
	public DBSchemaCreationForHibernate(final DBSchemaTablesDDLActionForHibernate ddlAction,
										final Path ddlScriptPath,
										final FileName ddlCreateFileName,final FileName ddlDropFileName) {
		super(ddlAction,
			  ddlScriptPath,
			  ddlCreateFileName,ddlDropFileName,
			  null,
			  (Properties)null);
	}
	public DBSchemaCreationForHibernate(final DBSchemaTablesDDLActionForHibernate ddlAction,
										final Path dataLoadScriptPath,
										final Properties otherProps) {
		super(ddlAction,
			  null,
			  null,null,
			  dataLoadScriptPath,
			  otherProps);
	}
	public DBSchemaCreationForHibernate(final DBSchemaTablesDDLActionForHibernate ddlAction,
										final Path dataLoadScriptPath) {
		super(ddlAction,
			  null,
			  null,null,
			  dataLoadScriptPath,
			  (Properties)null);
	}
	public DBSchemaCreationForHibernate(final CoreAppCode appCode,
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
					   		  .asEnumElement(DBSchemaTablesDDLActionForHibernate.class)
					   		  .orDefault(DBSchemaTablesDDLActionForHibernate.NONE);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Properties asProperties() {
		Properties props = new Properties();

	    // If no schema generation action is required, return an empty properties object
	    if (_ddlAction == DBSchemaTablesDDLActionForHibernate.NONE) return props;
	    
	    // [1] == Schema generation from the object/relational mapping metadata
		// specifies the action to be taken by the persistence provider with regard to the database artifacts
	    
	    props.put(AvailableSettings.HBM2DDL_AUTO, DBSchemaTablesDDLActionForHibernate.CREATE_TABLES.getVendorAction());
	    props.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, "false");	// do NOT generate the schema
	    props.put(AvailableSettings.COLUMN_ORDERING_STRATEGY, "legacy"); // values: legacy - ColumnOrderingStrategyLegacy, default - ColumnOrderingStrategyStandard, ...etc. 

       	if (_ddlAction != null) {
       		props.put(AvailableSettings.HBM2DDL_AUTO,_ddlAction.getVendorAction());
			props.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, "true"); // Generate indexes for foreign keys
       	}

	    // [2] == Schema generation from DDL scripts (if specified)
	    if (_ddlScriptPath != null) {
	    	props.put(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, _ddlAction.getVendorAction()); // Important! Necessary to configure the database mode.
	        props.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.ACTION_CREATE_THEN_DROP);
	        props.put(AvailableSettings.HBM2DDL_SCRIPTS_CREATE_APPEND, false); // Not append SQL-Shema definitions
	        props.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_CREATE_TARGET, 
	        		  String.format("%s/%s", 
	        				        _ddlScriptPath, 
	        				        _ddlCreateFileName.asString()));
	        props.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_DROP_TARGET,
	        		  String.format("%s/%s", 
	        				    	_ddlScriptPath, 
	        				    	_ddlDropFileName.asString()));
	    }
	    
	    // [3] == Load initial data script
	    // If the database needs to be populated with initial data, this property specifies the SQL load script
	    if (_dataLoadScriptPath != null) {
	        // Hibernate uses `hibernate.hbm2ddl.import_files` to load initial data.
	        props.put(SchemaToolingSettings.JAKARTA_HBM2DDL_CREATE_SCRIPT_SOURCE, _dataLoadScriptPath.asAbsoluteString());
	    }
	
	    // [4] == Other properties
	    // If there are additional properties, add them here
	    if (_otherProps != null) {
	        props.putAll(_otherProps);
	    }
	    return props;
	}
}

