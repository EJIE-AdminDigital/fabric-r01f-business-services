package r01f.services.core.persistence.db.config;

import java.util.Map;
import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.oids.CommonOIDs.IsAppCode;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
public class DBConnectionPoolDataForEclipseLink
     extends DBConnectionPoolDataBase {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBConnectionPoolDataForEclipseLink(final IsAppCode appCode,
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
	DBConnectionPoolConfigData _connectionPoolConfigDataFrom(final Map<String,String> propsMap) {
		// Driver User / password
		String loginId = propsMap.remove(PersistenceUnitProperties.JDBC_USER);
		String pwd = propsMap.remove(PersistenceUnitProperties.JDBC_PASSWORD);
		String driverJavaType = propsMap.remove(PersistenceUnitProperties.JDBC_DRIVER);
		String connectionUrl = propsMap.remove(PersistenceUnitProperties.JDBC_URL);
		// the rest of the properties
		Properties otherProps = !propsMap.isEmpty() ? CollectionUtils.toProperties(propsMap) : null;
		
		return new DBConnectionPoolConfigData(loginId,pwd, 
											  driverJavaType,
											  connectionUrl, 
											  otherProps);
	}
	@Override
	DBConnectionPoolSizeBase _dbConnectionPoolSizeFrom(final IsAppCode appCode,
											  		   final PersistenceUnitType unitType,
											  		   final DBSpec dbSpec,
											  		   final XMLPropertiesForAppComponent xmlProps) {
		return new DBConnectionPoolSizeForEclipseLink(appCode,
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
		props.put(PersistenceUnitProperties.JDBC_USER,_user.asString());
		props.put(PersistenceUnitProperties.JDBC_PASSWORD,_password.asString());
		props.put(PersistenceUnitProperties.JDBC_DRIVER,_driverClassName.asString()); // BEWARE to copy mySql jconector at $CATALINA_HOME/lib
		props.put(PersistenceUnitProperties.JDBC_URL,_connectionUrl);
		if (_poolSize != null) props.putAll(_poolSize.asProperties());
		if (_otherProps != null) props.putAll(_otherProps);
		return props;
	}
}

