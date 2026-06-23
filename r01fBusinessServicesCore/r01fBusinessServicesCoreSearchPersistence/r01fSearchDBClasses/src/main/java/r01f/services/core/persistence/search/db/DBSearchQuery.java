package r01f.services.core.persistence.search.db;

import java.util.Collection;

import com.google.common.collect.Lists;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import r01f.locale.Language;
import r01f.model.search.SearchFilter;
import r01f.model.search.query.SearchResultsOrdering;
import r01f.patterns.ProviderFrom;
import r01f.persistence.search.QueryBase;
import r01f.services.core.persistence.db.DBEntity;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.util.types.collections.CollectionUtils;

/**
 * Can be used to run DB search queries
 * <pre class='brush:java'>
 * 		BooleanQuery filter = BooleanQueryClause.create()
 *	    							.field("oid").must().beWithin(MyEntityOID.forId("xxxlc814593391b1d721a3067bdde926665c5e952"),
 *	    							 							  MyEntityOID.forId("xxxlc9145932d760a241b21e51fc9b298602689bd"))
 *	    							.field("createDate").must().beInsideDateRange(Range.atMost(new Date()))
 *	    							.build();
 *
 * 		// Put eclipselink.jar and the mysql connector driver in the classpath
 * 		// ... also copy the persistence.xml in the META-INF dir
 *	    Properties props = new Properties();
 *	    props.put("javax.persistence.jdbc.user","r01e");
 *	    props.put("javax.persistence.jdbc.password","r01e");
 *	    props.put("javax.persistence.jdbc.driver","com.mysql.jdbc.Driver");		// IMPORTANT!! When in Tomcat & MySQL, set jconector MySQL's driver at $CATALINA_HOME/lib
 *	    props.put("javax.persistence.jdbc.url","jdbc:mysql://localhost:3306/r01e");
 *	    props.put("eclipselink.target-database","org.eclipse.persistence.platform.database.MySQLPlatform");		// org.eclipse.persistence.platform.database.oracle.OraclePlatform
 *
 *
 *	    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistenceUnit.xxx",props);
 *	    EntityManager em = emf.createEntityManager();
 *
 *		Query q = DBSearchQueryForModelObjectBuilder.forDBEntityType(MyDBEntity.class)
 *														  .usingDBModuleConfig(dbModuleConfig)
 *														  .with(_entityManager)
 *														  .noUILanguage()
 *														  .withFilterType(filterType)
 *												    .getCountQuery(filter);
 *
 *	    Query jpaQry = qry.getCountQuery();
 *	    System.out.println(">>>" + jpaQry.getSingleResult());
 * </pre>
 * @param <F>
 * @param <DB>
 */
public class DBSearchQuery<F extends SearchFilter,
					       DB extends DBEntity>
	 extends QueryBase<DBSearchQuery<F,DB>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final DBModuleConfig _dbModuleConfig;
	protected final EntityManager _entityManager;
	protected final ProviderFrom<F,Class<? extends DB>> _dbEntityTypeFromFilter;
	protected final DBSearchQueryToJPQLTranslator<F,DB> _searchQueryToJpq;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBSearchQuery(final Class<DB> dbEntityType,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager) {
		this(dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 (Language)null);
	}
	public DBSearchQuery(final Class<DB> dbEntityType,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager,
						 final Language uiLanguage) {
		this(dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 uiLanguage,
			 new DBSearchQueryToJPQLTranslator<>(dbEntityType,
											     dbModuleConfig,
												 entityManager));
	}
	public DBSearchQuery(final Class<DB> dbEntityType,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager,
						 final DBSearchQueryToJPQLTranslator<F,DB> searchQueryToJpql) {
		this(dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 (Language)null,
			 searchQueryToJpql);
	}
	public DBSearchQuery(final Class<DB> dbEntityType,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager,
						 final Language uiLanguage,
						 final DBSearchQueryToJPQLTranslator<F,DB> searchQueryToJpql) {
		this(filter -> dbEntityType,
			 dbModuleConfig,
			 entityManager,
			 uiLanguage,
			 searchQueryToJpql);
	}
	public DBSearchQuery(final ProviderFrom<F,Class<? extends DB>> dbEntityTypeFromFilter,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager) {
		this(dbEntityTypeFromFilter,
			 dbModuleConfig,
			 entityManager,
			 (Language)null);
	}
	public DBSearchQuery(final ProviderFrom<F,Class<? extends DB>> dbEntityTypeFromFilter,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager,
						 final Language uiLanguage) {
		this(dbEntityTypeFromFilter,
			 dbModuleConfig,
			 entityManager,
			 uiLanguage,
			 new DBSearchQueryToJPQLTranslator<>(dbEntityTypeFromFilter,
											   	 dbModuleConfig,
												 entityManager));
	}
	public DBSearchQuery(final ProviderFrom<F,Class<? extends DB>> dbEntityTypeFromFilter,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager,
						 final DBSearchQueryToJPQLTranslator<F,DB> searchQueryToJpql) {
		this(dbEntityTypeFromFilter,
			 dbModuleConfig,
			 entityManager,
			 (Language)null,
			 searchQueryToJpql);
	}
	public DBSearchQuery(final ProviderFrom<F,Class<? extends DB>> dbEntityTypeFromFilter,
						 final DBModuleConfig dbModuleConfig,
						 final EntityManager entityManager,
						 final Language uiLanguage,
						 final DBSearchQueryToJPQLTranslator<F,DB> searchQueryToJpql) {
		super(uiLanguage);
		_dbEntityTypeFromFilter = dbEntityTypeFromFilter;
		_dbModuleConfig = dbModuleConfig;
		_entityManager = entityManager;
		_searchQueryToJpq = searchQueryToJpql;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PUBLIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public String getDBEntityAlias() {
		return _searchQueryToJpq._dbEntityAlias;
	}
	public String getDBEntityName(final F filter) {
		return _searchQueryToJpq._dbEntityTypeNameFor(filter);
	}
	
	public TypedQuery<Long> getCountQuery(final F filter) {
		// [1] - Compose the JPQL
		String countJPQL = _searchQueryToJpq.composeCountJPQL(filter);

		// [2] - Create the JPA query & set the params
		TypedQuery<Long> countQry = _entityManager.createQuery(countJPQL.toString(),
												   			   Long.class);
		_searchQueryToJpq.setJPAQueryParameters(filter,countQry);
		return countQry;
	}
	public TypedQuery<? extends DB> getResultsQueryWithAdditionalColumns(final String additionalSelectColumns,
																		 final F filter) {
		return _getResultsQuery(additionalSelectColumns,
								filter,
								(Collection<SearchResultsOrdering>)null);	// no ordering
	}
	public TypedQuery<? extends DB> getResultsQueryWithAdditionalColumns(final String additionalSelectColumns,
																		 final F filter,
																		 final SearchResultsOrdering... ordering) {
		return _getResultsQuery(additionalSelectColumns,
								filter,
								CollectionUtils.hasData(ordering) ? Lists.newArrayList(ordering) : null);
	}
	public TypedQuery<? extends DB> getResultsQuery(final F filter) {
		return _getResultsQuery(null,
								filter,
								(Collection<SearchResultsOrdering>)null);	// no ordering
	}
	public TypedQuery<? extends DB> getResultsQuery(final F filter,
										  			final SearchResultsOrdering... ordering) {
		return _getResultsQuery(null,
								filter,
								CollectionUtils.hasData(ordering) ? Lists.newArrayList(ordering) : null);
	}
	public TypedQuery<? extends DB> getResultsQuery(final F filter,
										  			final Collection<SearchResultsOrdering> ordering) {
		return _getResultsQuery(null,
								filter,
								ordering);
	}
	protected TypedQuery<? extends DB> _getResultsQuery(final String additionalSelectColumns,
														final F filter,
														final Collection<SearchResultsOrdering> ordering) {

		//TODO
		/* Be carefull!   If you want a "OR QueryClause" over the SAME COLUMN, f.e where 'appComponent'
		 *                can have two value, f.e "a" ,"b" ...The resulting JPQL will be:
		 *  ----------------------------------------------------------------------------------------
		 *   SELECT entity FROM
		 *    TheEntityDB entity
		 *      WHERE ((entity._appComponent = :appComponent)  or (entity._appComponent = :appComponent) )
		 *
		 *  ...That's a PROBLEM because the same variable name (appComponent) will be use, that is "appComponent'
		 *  ...so, the resulting query will be :
		 *      (entity._appComponent = 'a' or entity._appComponent ='a'
		 *
		 *  WORKARAOUND: Use instead ContainedInQueryClause  ( works fine )
		 *
		 */

		// [1] - Compose the JPQL
		String jpql;
		if (additionalSelectColumns!=null) {
			jpql = _searchQueryToJpq.composeRetrieveJPQLWithAdditionalColumns(additionalSelectColumns,
																			  filter,
																			  ordering);
		} else {
			jpql = _searchQueryToJpq.composeRetrieveJPQL(filter,
														 ordering);
		}
		
		// [2] - Create the JPA query & set the params
		Class<? extends DB> dbEntityType = _dbEntityTypeFromFilter.from(filter);
		TypedQuery<? extends DB> qry = _entityManager.createQuery(jpql.toString(),
																  dbEntityType);
		_searchQueryToJpq.setJPAQueryParameters(filter,qry);
		return qry;
	}
	public Query getOidsQuery(final F filter) {
		// [1] - Compose the JPQL
		String jpql = _searchQueryToJpq.composeRetrieveOidsJPQL(filter);

		// [2] - Create the JPA query & set the params
		Query outQry = _entityManager.createQuery(jpql);
		_searchQueryToJpq.setJPAQueryParameters(filter,outQry);
		return outQry;
	}
}
