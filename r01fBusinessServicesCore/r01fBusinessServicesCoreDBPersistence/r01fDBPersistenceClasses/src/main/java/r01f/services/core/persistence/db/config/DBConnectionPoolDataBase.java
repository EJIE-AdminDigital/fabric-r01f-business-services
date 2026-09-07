package r01f.services.core.persistence.db.config;

import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.IsAppCode;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.securitycontext.SecurityIDS.Password;
import r01f.types.JavaTypeName;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
public abstract class DBConnectionPoolDataBase
  		   implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter protected final DBConnectionPoolSizeBase _poolSize;

	@Getter protected final LoginID _user;
	@Getter protected final Password _password;

	@Getter protected final JavaTypeName _driverClassName;
	@Getter protected final String _connectionUrl;

	@Getter protected final Properties _otherProps;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBConnectionPoolDataBase(final IsAppCode appCode,
									final PersistenceUnitType unitType,
									final DBSpec dbSpec,
							   		final XMLPropertiesForAppComponent xmlProps) {
		DBConnectionPoolConfigData data = null;

		String xPath = "persistence/unit[@type='" + unitType + "']/connection[@vendor='" + dbSpec.getVendor() + "']";
		Properties props = xmlProps.propertyAt(xPath)
							   			.asProperties();
		if (CollectionUtils.isNullOrEmpty(props)) {
			log.error("Could NO load the DB connection properties at {} at properties file {}: does it contains a connection section?",
					  xPath,appCode);
		} else {
			Map<String,String> propsMap = CollectionUtils.toMap(props);
			
			data = _connectionPoolConfigDataFrom(propsMap);
		}
		_poolSize = _dbConnectionPoolSizeFrom(appCode,
									     	  unitType,
									     	  dbSpec,
									     	  xmlProps);
		_user = Strings.isNOTNullOrEmpty(data.loginId) ? LoginID.forId(data.loginId)
											      	   : LoginID.forId(appCode.asString());
		_password = Strings.isNOTNullOrEmpty(data.pwd) ? Password.forId(data.pwd)
												  	   : Password.forId(appCode.asString());
		_driverClassName = Strings.isNOTNullOrEmpty(data.driverJavaType) ? new JavaTypeName(data.driverJavaType)
																		 : new JavaTypeName("com.mysql.jdbc.Driver");
		_connectionUrl = Strings.isNOTNullOrEmpty(data.connectionUrl) ? data.connectionUrl
																 	  : "jdbc:mysql://localhost:3306/pci";
		_otherProps = data.otherProps;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("DB connection pool: size={}, usr/pwd={}/{}, driver={}",
								  _poolSize.debugInfo(),
								  _user,_password,
								  _driverClassName);
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
// ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public abstract Properties asProperties();
	abstract DBConnectionPoolConfigData _connectionPoolConfigDataFrom(final Map<String,String> propsMap);
	abstract DBConnectionPoolSizeBase _dbConnectionPoolSizeFrom(final IsAppCode appCode,
											  					final PersistenceUnitType unitType,
											  					final DBSpec dbSpec,
											  					final XMLPropertiesForAppComponent xmlProps);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	record DBConnectionPoolConfigData(String loginId,String pwd,
									  String driverJavaType,
									  String connectionUrl,
									  Properties otherProps) {
		// nothing
	}
}