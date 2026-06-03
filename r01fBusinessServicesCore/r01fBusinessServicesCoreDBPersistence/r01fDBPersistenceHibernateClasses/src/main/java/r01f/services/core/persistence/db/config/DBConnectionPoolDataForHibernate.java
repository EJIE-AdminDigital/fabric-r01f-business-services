package r01f.services.core.persistence.db.config;

import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.JdbcSettings;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.AppCode;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;


@Accessors(prefix="_")
public class DBConnectionPoolDataForHibernate
     extends DBConnectionPoolDataBase {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBConnectionPoolDataForHibernate(final AppCode appCode,
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
		String loginId = propsMap.remove(JdbcSettings.JAKARTA_JDBC_USER);
		String pwd = propsMap.remove(JdbcSettings.JAKARTA_JDBC_PASSWORD);
		String driverJavaType = propsMap.remove(JdbcSettings.JAKARTA_JDBC_DRIVER);
		String connectionUrl = propsMap.remove(JdbcSettings.JAKARTA_JDBC_URL);
		// the rest of the properties
		Properties otherProps = !propsMap.isEmpty() ? CollectionUtils.toProperties(propsMap) : null;
		
		return new DBConnectionPoolConfigData(loginId,pwd, 
											  driverJavaType,
											  connectionUrl, 
											  otherProps);
	}
	@Override
	DBConnectionPoolSizeBase _dbConnectionPoolSizeFrom(final AppCode appCode,
											  		   final PersistenceUnitType unitType,
											  		   final DBSpec dbSpec,
											  		   final XMLPropertiesForAppComponent xmlProps) {
		return new DBConnectionPoolSizeForHibernate(appCode,
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
		props.put(JdbcSettings.JAKARTA_JDBC_USER,_user.asString());
		props.put(JdbcSettings.JAKARTA_JDBC_PASSWORD,_password.asString());
		props.put(JdbcSettings.JAKARTA_JDBC_DRIVER,_driverClassName.asString()); // BEWARE to copy mySql jconector at $CATALINA_HOME/lib
		props.put(JdbcSettings.JAKARTA_JDBC_URL,_connectionUrl);
		if (_poolSize != null) props.putAll(_poolSize.asProperties());
		if (_otherProps != null) props.putAll(_otherProps);
		return props;
	}
}

