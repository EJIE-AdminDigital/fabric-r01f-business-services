package r01f.services.core.persistence.db.config;

import java.util.Properties;

import r01f.file.FileName;
import r01f.internal.R01HomeLocation;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

public abstract class DBSchemaCreationBase {

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final DBSchemaTablesDDLAction _ddlAction;
	protected final Path _ddlScriptPath;
	protected final FileName _ddlCreateFileName;
	protected final FileName _ddlDropFileName;
	protected final Path _dataLoadScriptPath;
	protected final Properties _otherProps;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBSchemaCreationBase(final DBSchemaTablesDDLAction ddlAction,
								final Path ddlScriptPath,
								final FileName ddlCreateFileName,final FileName ddlDropFileName,
								final Path dataLoadScriptPath,
								final Properties otherProps) {
		_ddlAction = ddlAction;
		_ddlScriptPath = ddlScriptPath;
		_ddlCreateFileName = ddlCreateFileName;
		_ddlDropFileName = ddlDropFileName;
		_dataLoadScriptPath = dataLoadScriptPath;
		_otherProps = otherProps;
	}
	public DBSchemaCreationBase(final DBSchemaTablesDDLAction ddlAction,
								final Path ddlScriptPath,
								final FileName ddlCreateFileName,final FileName ddlDropFileName,
								final Path dataLoadScriptPath) {
		this(ddlAction,
			 ddlScriptPath,
			 ddlCreateFileName,ddlDropFileName,
			 dataLoadScriptPath,
			 (Properties)null);
	}
	public DBSchemaCreationBase(final DBSchemaTablesDDLAction ddlAction,
							    final Path ddlScriptPath,
								final FileName ddlCreateFileName,final FileName ddlDropFileName,
								final Properties otherProps) {
		this(ddlAction,
			 ddlScriptPath,
			 ddlCreateFileName,ddlDropFileName,
			 null,
			 otherProps);
	}
	public DBSchemaCreationBase(final DBSchemaTablesDDLAction ddlAction,
								final Path ddlScriptPath,
								final FileName ddlCreateFileName,final FileName ddlDropFileName) {
		this(ddlAction,
			 ddlScriptPath,
			 ddlCreateFileName,ddlDropFileName,
			 null,
			 (Properties)null);
	}
	public DBSchemaCreationBase(final Path dataLoadScriptPath,
								final Properties otherProps) {
		this(null,		// ddl action
			 null,
			 null,null,	// create & drop file names are not specified when the ddlAction is not specified
			 dataLoadScriptPath,
			 otherProps);
	}
	public DBSchemaCreationBase(final Path dataLoadScriptPath) {
		this(null,		// ddl action
			 null,
			 null,null,	// create & drop file names are not specified when the ddlAction is not specified
			 dataLoadScriptPath,
			 (Properties)null);
	}
	public DBSchemaCreationBase(final CoreAppCode appCode,
								final DBSpec dbSpec,
								final XMLPropertiesForAppComponent xmlProps) {
		// [0] == DDL action
		_ddlAction = this._DDLActionFromString(xmlProps.propertyAt("persistence/schema/generationMode")
												  		 .asString());
		// [1] == Schema generation from DDL scripts
		// specify the file system directory in which EclipseLink writes (outputs) DDL files. - See more at: http://www.eclipse.org/eclipselink/documentation/2.5/jpa/extensions/p_application_location.htm#CACHGDEJ
		String ddlScriptPath = xmlProps.propertyAt("persistence/schema/writeDDLScriptTo")
								 			.asString();	// Path.from(Strings.customized("c:/develop/temp_dev/{}",appCode))
		if (Strings.isNOTNullOrEmpty(ddlScriptPath)) {
			String theDDLScriptPath = ddlScriptPath		// the properties specified path can have placeholders for the appCode or the home location
											 .replaceAll("\\{APPCODE\\}",appCode.asString())
											 .replaceAll("\\{R01_HOME\\}",R01HomeLocation.guessHomeLocationFromSystemEnvProp().asAbsoluteString());
			_ddlScriptPath = Path.from(theDDLScriptPath);
		} else {
			_ddlScriptPath = null;
		}
		
		_ddlCreateFileName = FileName.of(appCode.asString() + "." + xmlProps.getAppComponent().asAppComponent() + ".create.sql");
		_ddlDropFileName = FileName.of(appCode.asString() + "." + xmlProps.getAppComponent() + ".drop.sql");

		// [2] == Initial load script
		// If the database has to be loaded with initial data this property specifies the
		// SQL load script for database initialization
		String loadScriptPath = xmlProps.propertyAt("persistence/schema/loadScriptPath")
								    		.asString();
		if (Strings.isNOTNullOrEmpty(loadScriptPath)) {
			String theLoadScriptPath = loadScriptPath		// the properties specified path can have placeholders for the appCode
											 .replaceAll("\\{APPCODE\\}",appCode.asString());
			_dataLoadScriptPath = Path.from(theLoadScriptPath);
		} else {
			_dataLoadScriptPath = null;
		}

		// [3] == Other properties
		_otherProps = xmlProps.propertyAt("persistence/schema/properties[@vendor='" + dbSpec.getVendor().getCode() + "']")
							  .asProperties();
	}

/////////////////////////////////////////////////////////////////////////////////////////
// ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	abstract DBSchemaTablesDDLAction _DDLActionFromString(final String ddlActionStr);
	public abstract Properties asProperties();
}

