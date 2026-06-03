package r01f.services.core.persistence.db.config;

import java.util.Properties;
import java.util.logging.Level;

import jakarta.persistence.EntityManager;
import r01f.config.ContainsConfigData;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.util.types.Strings;

/**
 * db module config
 */
public interface DBModuleConfig
		 extends ContainsConfigData {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the app code
	 */
	public CoreAppCode getAppCode();
	/**
	 * @return the app module
	 */
	public CoreModule getAppModule();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * @return the {@link PersistenceUnitType}
	 */
	public PersistenceUnitType getUnitType();
	/**
	 * @return the {@link DBSpec}
	 */
	public DBSpec getDbSpec();
	/**
	 * @return how the DB schema will be created
	 */
	public DBSchemaCreationBase getDbSchemaCreation();
	/**
	 * @return the log leve
	 */
	public Level getLogLevel();
	/**
	 * @return other {@link Properties}
	 */
	public Properties getOtherProps();
	/**
	 * @return all the properties as a {@link Properties} object
	 */
	public Properties asProperties();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Override this method like:
	 * <pre class='brush:java'>
	 * 		@Override
	 *		public boolean isFullTextSearchSupported(final EntityManager entityManager) {
	 *			return _fullTextSearchSupported.get(new Factory<Boolean>() {
	 *														@Override
	 *														public Boolean create() {
	 *															// tries to run a full-text search... if it fails full text search is NOT enabled
	 *															return _testFullText(entityManager,
	 *																				 MyDBEntity.class,
	 *																				 "_fullText");	// any full-text indexed col
	 *														}
	 *												});
	 *		}
	 * </pre>
	 * @param entityManager
	 * @return
	 */
	public boolean isFullTextSearchSupported(final EntityManager entityManager);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public default String getJPAPersistenceUnitName() {
		// case 1 (appCode + appComponent):
		//	- the persistence PROPERTIES are going to be looked after as  {appCode}.{appComponent}.persistence.properties.xml
		//	- the persistence UNIT is going to be looked after as persistenceUnit.{appCode}.{appComponent}
		// case 2 (appCode only):
		//	- the persistence PROPERTIES are going to be looked after as  {appCode}.persistence.properties.xml
		//	- the persistence UNIT is going to be looked after as persistenceUnit.{appCode}
		String jpaServiceName = this.getAppModule() == null ? this.getAppCode().asString()
													  		: Strings.customized("{}.{}",
													  				   			 this.getAppCode(),this.getAppModule());
		String persistenceUnitName = Strings.customized("persistenceUnit.{}",
												 	 	jpaServiceName);				// ie: "r01t.urlAlias"
		return persistenceUnitName;
	}
	public default String getJPAServiceName() {
		String jpaServiceName = this.getAppModule() == null ? this.getAppCode().asString()
												  		    : Strings.customized("{}.{}",
												  				   				 this.getAppCode(),this.getAppModule());
		return jpaServiceName;
	}
}

