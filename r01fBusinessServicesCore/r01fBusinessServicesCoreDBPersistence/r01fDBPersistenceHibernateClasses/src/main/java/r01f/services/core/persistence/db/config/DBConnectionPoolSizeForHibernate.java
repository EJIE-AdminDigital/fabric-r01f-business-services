package r01f.services.core.persistence.db.config;

import java.util.Properties;

import org.w3c.dom.Node;

import r01f.guids.CommonOIDs.IsAppCode;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

public class DBConnectionPoolSizeForHibernate
     extends DBConnectionPoolSizeBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final String _provider;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBConnectionPoolSizeForHibernate(final IsAppCode appCode,
							    			final PersistenceUnitType unitType,
							    			final DBSpec dbSpec,
							    			final XMLPropertiesForAppComponent xmlProps) {
		super(appCode,
			  unitType,
			  dbSpec,
			  xmlProps);
		String provider = DBPoolConfigForHibernate.HIBERNATE_DEFAULT_PROVIDER;
		String xPath = "persistence/unit[@type='" + unitType + "']/connectionPool";
		Node node = xmlProps.propertyAt(xPath)
							.node();
		if (node != null && node.getAttributes() != null) {
			provider = DBPoolConfigForHibernate.HIKARI_PROVIDER;
		}
		_provider = provider;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Properties asProperties() {
		Properties props = new Properties();
		if (_provider != null) props.put(DBPoolConfigForHibernate.HIKARI_PROVIDER_CLASS,_provider);
		if (_initial != null) props.put(DBPoolConfigForHibernate.HIKARI_MINIMUM_IDLE,_initial);
		if (_min != null) props.put(DBPoolConfigForHibernate.HIKARI_MINIMUM_IDLE,_min);
		if (_max != null) props.put(DBPoolConfigForHibernate.HIKARI_MAXIMUM_POOL_SIZE,_max);
		return props;
	}
}

