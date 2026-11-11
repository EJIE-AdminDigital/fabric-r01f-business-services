package r01f.services.core.persistence.db;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Lists;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CountResult;
import r01f.model.persistence.CountResultBuilder;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class DBCountForModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
													  PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>>
			  extends DBBaseForModelObject<O,M,PK,DB>
	       implements DBCountForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBCountForModelObjectBase(final Class<M> modelObjectType,
									 final Class<DB> dbEntityType,
									 final DBModuleConfig dbCfg,
									 final EntityManager entityManager,
									 final Marshaller marshaller) {
		super(modelObjectType,
			  dbEntityType,
			  dbCfg,
			  entityManager,
			  marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CountResult<M> countAll(final SecurityContext securityContext) {
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);
		
		criteriaQuery.select(criteriaBuilder.count(root));
		long countResult = _entityManager.createQuery(criteriaQuery)
										 .getSingleResult();
		return CountResultBuilder.using(securityContext)
								 .on(_modelObjectType)
								 .counted("all")
								 .resulting(countResult);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static String _buildJpqlSelect(final Class<? extends DBEntityForModelObject<?>> dbEntityType) {
		return Strings.customized("SELECT COUNT(e) " + 
									"FROM {} e",
								  dbEntityType.getSimpleName());
	}
	protected static String _assembleJpql(final String select,
									  	  final String... filterClauses) {
		return _assembleJpql(select,
							 CollectionUtils.hasData(filterClauses) ? Lists.newArrayList(filterClauses)
								 									: null);
	}
	protected static String _assembleJpql(final String select,
									  	  final Collection<String> filterClauses) {
		StringBuilder outJpql = new StringBuilder();
		outJpql.append(select);
		
		if (CollectionUtils.hasData(filterClauses)) {
			StringBuilder where = new StringBuilder();
			for (Iterator<String> filterClausesIt = filterClauses.iterator(); filterClausesIt.hasNext(); ) {
				String filterClause = filterClausesIt.next();
				if (Strings.isNullOrEmpty(filterClause)) continue;
				
				where.append(filterClause);
				if (filterClausesIt.hasNext()) where.append(" AND ");
			}
			outJpql.append(" WHERE ")
				   .append(where);
		}
		String outJpqlStr = outJpql.toString();
		log.debug("Count JPQL: {}",outJpqlStr);
		return outJpqlStr;
	}
	protected static void _setQueryParams(final Query qry,
									  	  final Map<String,Object> params) {
		if (CollectionUtils.isNullOrEmpty(params)) return;
		params.entrySet()
			  .stream()
			  .forEach(me -> {
				  			String paramName = me.getKey();
				  			Object paramVal = me.getValue();
				  			
				  			if (paramVal != null) qry.setParameter(paramName,paramVal);
			  		   });
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Accessors(prefix="_")
	protected class CountQueryWrapper
			extends QueryWrapperBase<CountQueryWrapper> {
		@Getter protected final CriteriaQuery<Long> _criteriaQuery;
		@Getter protected final Root<? extends DB> _root;
		
		@Getter protected final String _countSpec;	// a string that describes what is being counted (for logging purposes)

		public CountQueryWrapper(final String countSpec) {
			this(_DBEntityType,
				 countSpec);
		}
		public CountQueryWrapper(final Class<? extends DB> dbEntityType,
								 final String countSpec) {
			super(_entityManager.getCriteriaBuilder());
			_criteriaQuery = _criteriaBuilder.createQuery(Long.class);
			_root = _criteriaQuery.from(dbEntityType);
			
			_countSpec = countSpec;
		}
		public CountResult<M> countUsing(final SecurityContext securityContext) {
			_criteriaQuery.select(_criteriaBuilder.count(_root));
			this.composeCriteriaQuery();
			long countResult = _entityManager.createQuery(_criteriaQuery)
													.setHint(DBCountForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true)
											 .getSingleResult();
			return CountResultBuilder.using(securityContext)
									 .on(_modelObjectType)
									 .counted(_countSpec)
									 .resulting(countResult);
		}
	}
}
