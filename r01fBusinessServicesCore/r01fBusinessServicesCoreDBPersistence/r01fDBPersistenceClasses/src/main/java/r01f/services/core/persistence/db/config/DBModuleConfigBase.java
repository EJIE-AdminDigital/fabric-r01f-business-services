package r01f.services.core.persistence.db.config;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.patterns.Memoized;
import r01f.patterns.MemoizedUponFactory;
import r01f.services.core.persistence.db.DBEntity;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
public abstract class DBModuleConfigBase
    	   implements DBModuleConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final CoreAppCode _appCode;
	@Getter protected final CoreModule _appModule;
	@Getter protected final PersistenceUnitType _unitType;
	@Getter protected final DBSpec _dbSpec;
	@Getter protected final DBSchemaCreationBase _dbSchemaCreation;
	@Getter protected final Level _logLevel;
	@Getter protected final Properties _otherProps;
/////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBModuleConfigBase(final CoreAppCode appCode,final CoreModule appModule,
							  final PersistenceUnitType unitType,
							  final DBSpec dbSpec,
							  final DBSchemaCreationBase dbSchemaCreation,
							  final Level logLevel,
							  final Properties otherProps) {
		_appCode = appCode;
		_appModule = appModule;
		_unitType = unitType;
		_dbSpec = dbSpec;
		_dbSchemaCreation = dbSchemaCreation;
		_logLevel = logLevel;
		_otherProps = otherProps;
	}
	public DBModuleConfigBase(final CoreAppCode appCode,
							  final CoreModule appModule,
							  final PersistenceUnitType unitType,
							  final DBSpec dbSpec,
							  final DBSchemaCreationBase dbSchemaCreation,
							  final Level logLevel) {
		this(appCode,appModule,
			 unitType,dbSpec,
			 dbSchemaCreation,
			 logLevel,
			 null);
	}
	public DBModuleConfigBase(final CoreAppCode appCode,
							  final CoreModule appModule,
							  final PersistenceUnitType unitType,
							  final DBSpec dbSpec,
							  final DBSchemaCreationBase dbSchemaCreation) {
		this(appCode,appModule,
			 unitType,dbSpec,
			 dbSchemaCreation,
			 Level.INFO,
			 null);
	}
	public DBModuleConfigBase(final XMLPropertiesForAppComponent xmlProps,
							  final PersistenceUnitType unitType) {
		// [0] - Get the appcode and module
		//		 (beware that the module can be [module].dbpersistence so the 'dbpersistence' suffix must be stripped out)
		_appCode = CoreAppCode.of(xmlProps.getAppCode());
		String modStr = xmlProps.getAppComponent().asString();
		_appModule = modStr.contains(".dbpersistence")
							? CoreModule.forId(modStr.substring(0,modStr.indexOf(".dbpersistence")))
							: CoreModule.of(xmlProps.getAppComponent());

		// [1] - Unit Type
		_unitType = unitType;
		
		// [2] - Log level
		String logXPath = "persistence/debugSQL";
		Level logLevel = Level.INFO;

		// the debugSQL property might contain true/false or a log Level name (off, warn, error, etc)
		String debugSQLValue = xmlProps.propertyAt(logXPath)
											.asString("false");
		if (debugSQLValue.equalsIgnoreCase("false")) {
			logLevel = Level.INFO;
		} else if (debugSQLValue.equalsIgnoreCase("true")) {
			logLevel = Level.FINEST;
		} else {
			// the debugSQL property might contain a Level name
			try {
				logLevel = Level.parse(debugSQLValue);
			} catch (Throwable th) {
				th.printStackTrace(System.out);
				logLevel = Level.FINEST;
			}
		}
		_logLevel = logLevel;

		// [3] - Caching
		// CACHING http://wiki.eclipse.org/EclipseLink/Examples/JPA/Caching -->
		// Es importante DESHABILITAR el cache en AWS ya que hay multiples instancias del servidor de apps -->
		// En caso de HABILITAR el cache en AWS hay que coordinar las caches: http://wiki.eclipse.org/EclipseLink/UserGuide/JPA/sandbox/caching/Cache_Coordination -->
		// <property name="eclipselink.cache.shared.default" value="false"/>

		// [4] - Other properties
		String otherPropsXPath = "persistence/unit[@type='" + _unitType + "']/properties";
		Properties otherProps = xmlProps.propertyAt(otherPropsXPath)
							   			.asProperties();
		Properties customProps = new Properties();
		if (CollectionUtils.hasData(otherProps)) {
			for (Entry<Object,Object> e : otherProps.entrySet()) {
				String key = (String)e.getKey();
				String val = (String)e.getValue();

				if (Strings.isNOTNullOrEmpty(key)
				 && Strings.isNOTNullOrEmpty(val)) {
					String customVal = val.replaceAll("\\{APPCODE\\}",_appCode.asString())
										  .replaceAll("\\{APP_MODULE\\}",_appModule.asString());
					customProps.put(key,customVal);
				}
			}
		}
		_otherProps = customProps;
		if (CollectionUtils.isNullOrEmpty(_otherProps)) log.info("There're NO aditional JPA properties at {} at properties file {}",
					   											 otherPropsXPath,_appCode);
		
		// [5] - DBSpec
		String targetDB = xmlProps.propertyAt("persistence/unit[@type='" + _unitType + "']/@targetDB")
								  .asString();
		if (Strings.isNullOrEmpty(targetDB)) {
			targetDB = xmlProps.propertyAt("persistence/unit[@type='" + _unitType + "']/@targetBBDD")
									.asString();
			if (Strings.isNOTNullOrEmpty(targetDB)) log.warn("using an old db persistence properties XML file spec: xpath 'persistence/unit[@type='{}']/@targetBBDD' should be 'persistence/unit[@type='{}']/@targetBBDD' (the word targetBBDD has changed to targetDB",
															 _unitType,_unitType);
		}
		if (Strings.isNullOrEmpty(targetDB)) {
			log.warn("Could NOT load the DB vendor and version from persistence/unit[@type='{}']/@targetDB at {} properties file... {} is used by default",
					  _unitType,_appCode,DBSpecBase.DEFAULT);
			targetDB = DBSpecBase.DEFAULT; // MySql is the default
		}
		_dbSpec = _dbSpecFromString(targetDB);

		// Schema creation
		_dbSchemaCreation = _dbSchemaCreationFrom(_appCode,
												  _dbSpec,
												  xmlProps);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Properties asProperties() {
		Properties appServerVendorProps = this.appServerVendorProperties();
		Properties dbVendorProps = this.dbVendorProperties();
		Properties schemaCreateProps = this.getDbSchemaCreation().asProperties();
		Properties logProps = this.logProperties();
		Properties otherProps = this.getOtherProps();

		// a bit of log
		if (appServerVendorProps != null) 	_logProps("APPSERVER VENDOR",appServerVendorProps);
		if (dbVendorProps != null) 			_logProps("DB VENDOR",dbVendorProps);
		if (schemaCreateProps != null) 		_logProps("DB SCHEMA CREATE",schemaCreateProps);
		if (logProps != null) 				_logProps("LOG",logProps);
		if (otherProps != null) 			_logProps("OTHER",otherProps);

		// put all props in a single properties
		Properties outProps = new Properties();
		if (appServerVendorProps != null) 	outProps.putAll(appServerVendorProps);
		if (dbVendorProps != null) 			outProps.putAll(dbVendorProps);
		if (schemaCreateProps != null) 		outProps.putAll(schemaCreateProps);
		if (logProps != null) 				outProps.putAll(logProps);
		if (otherProps != null) 			outProps.putAll(otherProps);

		return outProps;
	}
	public Properties appServerVendorProperties() {
		Properties props = new Properties();
		return props;
	}
	protected static void _logProps(final String propsId,final Properties props) {
		log.info("\tDDBB {} PROPERTIES:::::::::::::::::::::::::",propsId);
		Set<Entry<Object,Object>> propEntries = props.entrySet();
		for (Entry<Object,Object> propEntry : propEntries) {
			log.info("\t\t{} = {}",propEntry.getKey(),propEntry.getValue());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
// ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	abstract DBSpec _dbSpecFromString(final String dbSpecStr);
	abstract DBSchemaCreationBase _dbSchemaCreationFrom(final AppCode appCode,
														final DBSpec spec,
														final XMLPropertiesForAppComponent xmlProps);
	
	protected abstract Properties dbVendorProperties();

	protected abstract Properties logProperties();
/////////////////////////////////////////////////////////////////////////////////////////
//  TEST FULLT-TEXT DB CAPABILITIES
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isFullTextSearchSupported(final EntityManager entityManager) {
		return false;	// not supported by default
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TEST FULLT-TEXT DB CAPABILITIES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * tries to run a full-text search... if it fails full text search is NOT enabled
	 * <ul>
	 * 	<li>If full text support is FALSE, the full-text queries are translated to LIKE queries</li>
	 *  <li>If full text support is TRUE, the full-text queries are translated to vendor-specific operators
	 *	  							  ...so the underlying database MUST support full-text searching
	 *	  		<ul>
	 *				<li>MySQL: Tables MUST be MyISAM and an index MUST be created on the cols to be searched</li>
	 *				<li>ORACLE: Oracle-Text MUST be enabled and an index MUST be created on the cols to be searched</li>
	 *			</ul>
	 *  </li>
	 * </ul>
	 *
	 * MySQL full-text
	 * ===============
     * IMPORTANT!! see: http://dev.mysql.com/doc/refman/5.0/en/fulltext-search.html / http://devzone.zend.com/26/using-mysql-full-text-searching/
     *		Tables MUST be MyISAM (InnoDB)) type; to change the table type:
     *			ALTER TABLE [table] engine=MyISAM;
     *
     *		also a FULLTEXT index must be added to the cols:
     *			ALTER TABLE [table] ADD FULLTEXT [NOMBRE INDICE](col1,col2,...);
     *
     *		Once the above is done, a FULL-TEXT search can be executed like:
     *			select *
     *			  from [table]
     *			 where MATCH(col1,col2) AGAINST ('[text]');
	 *
	 * BEWARE!!! by default, the FULLTEXT index is used in "natural language mode", which, will IGNORE results that happen in 50% or more of the rows
	 *			 ("words that are present in 50% or more of the rows are considered common and do not match.").
	 *			 If there's only 1 row in the table, there will ALWAYS return 0 results
	 *
	 *
	 * MEMOIZATION
	 * ===========
	 * In order to avoid calls to {@link #_isFullTextSupported()} usually a {@link Memoized} instance
	 * is used like:
	 * <pre class='brush:java'>
	 *		private final Memoized<Boolean> _fullTextSearchSupported = new Memoized<Boolean>() {
	 *																			@Override
	 *																			protected Boolean supply() {
	 *																				// tries to run a full-text search... if it fails full text search is NOT enabled
	 *																				return _testFullText(_entityManager,
	 *																									 MyDBEntity.class,
	 *																									 "_fullText");	// any full-text indexed col
	 *																			}
	 *																   };
	 *		protected boolean _isFullTextSupported() {
	 *			return _fullTextSearchSupported.get();
	 *		}
	 * </pre>
	 * ... but the EntityManager is NOT available when creating the config so a {@link MemoizedUponFactory} can be used like:
	 * <pre class='brush:java'>
	 * 		private final MemoizedUponFactory<Boolean> _fullTextSearchSupported = new MemoizedUponFactory<Boolean>();
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
	 * @param em
	 * @parm dbEntity the type of a db entity that has a full-text indexed field
	 * @param fieldName the db entity's field name
	 * @return
	 */
	public <DB extends DBEntity> boolean testFullText(final EntityManager em,
										 			  final Class<DB> dbEntity,
										 			  final String fieldName) {
		// SELECT entity
		//	 FROM dbEntity entity
		//  WHERE ... predicates ...
		StringBuilder jpql = new StringBuilder("SELECT entity " +
										  		 "FROM ").append(dbEntity.getSimpleName()).append(" entity ")
										.append("WHERE ");
		if (this.getDbSpec().getVendor().is(DBVendor.MySQL)) {
			jpql.append(Strings.customized("SQL('MATCH(?) " +
											  "AGAINST(?)',entity.{},:text)",
										   fieldName));
		} else if (this.getDbSpec().getVendor().is(DBVendor.ORACLE)) {
			// SELECT t.*
			//   FROM {table} t
			//  WHERE CONTAINS({column},'{text}',1) > 0
			jpql.append(Strings.customized("SQL('CONTAINS(?,?,1) > 0',entity.{},:text) ",
										   fieldName));
		}
		// Execute the query
		return this.testFullTextJPQL(em,
									 dbEntity,
								     jpql);
	}
	public <DB extends DBEntity> boolean testFullTextJPQL(final EntityManager em,
										 				  final Class<DB> dbEntity,
										 				  final StringBuilder jpql) {
		boolean outSupportsFullText = false;

		// Execute the query
		try {
			TypedQuery<DB> qry = em.createQuery(jpql.toString(),
												dbEntity);
			qry.setParameter("text","anything...");
			qry.getResultList();			// this should fail if full text is NOT properly enabled
			outSupportsFullText = true;		// it it reach this point... full text is enabled
		} catch (Throwable dbEx) {
			log.error("It seems the {} DB engine does NOT supports FULL-TEXT queries: testing {} leads to an error: {}",
					  this.getDbSpec().getVendor(),jpql,
					  dbEx.getMessage(),
					  dbEx);
		}
		// log
		log.debug("**************************************************************************************************************************************************************************");
		if (this.getDbSpec().getVendor().is(DBVendor.MySQL)) {
			log.debug("The {} db store does{} supports full-text searching: ensure that the tables are MyISAM type and that a FULLTEXT index exists on name cols of entity table",
					 DBVendor.MySQL,
					 outSupportsFullText ? "" : " NOT");
		} else if (this.getDbSpec().getVendor().is(DBVendor.ORACLE)) {
			log.debug("The {} db store does{} supports full-text searching: ensure that ORACLE-TEXT is enabled and that a FULLTEXT index exists on name cols of entity table",
					 DBVendor.ORACLE,
					 outSupportsFullText ? "" : " NOT");
		}
		log.debug("**************************************************************************************************************************************************************************");
		return outSupportsFullText;
	}
}