package r01f.services.core.persistence.db.config;

public final class DBPoolConfigForHibernate {
	
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTANTS 
/////////////////////////////////////////////////////////////////////////////////////////
	protected final static String HIBERNATE_DEFAULT_PROVIDER = "org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl";
	protected final static String HIKARI_PROVIDER = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider";
	protected final static String HIKARI_PROVIDER_CLASS = "hibernate.connection.provider_class";
	protected final static String HIKARI_MINIMUM_IDLE = "hibernate.hikari.minimumIdle";
	protected final static String HIKARI_MAXIMUM_POOL_SIZE = "hibernate.hikari.maximumPoolSize";
}
