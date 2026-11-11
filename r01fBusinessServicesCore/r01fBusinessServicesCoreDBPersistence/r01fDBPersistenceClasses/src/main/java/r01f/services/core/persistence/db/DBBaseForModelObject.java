package r01f.services.core.persistence.db;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.model.HasTrackingInfo;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasEntityVersion;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.OID;
import r01f.oids.PersistableObjectOID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.patterns.FactoryFrom;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;
import r01f.types.Range;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Accessors(prefix="_")
@Slf4j
public abstract class DBBaseForModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
										   PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>>
			  extends DBBase
		   implements TransformsDBEntityIntoModelObject<DB,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object's type
	 */
	@Getter protected final Class<M> _modelObjectType;
	/**
	 * db entity java type
	 */
	@Getter protected final Class<DB> _DBEntityType;
	/**
	 * Transforms a db entity into a model object
	 */
	@Getter protected final TransformsDBEntityIntoModelObject<DB,M> _dbEntityIntoModelObjectTransformer;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBaseForModelObject(final Class<M> modelObjectType,
								final Class<DB> dbEntityType,
								final DBModuleConfig dbCfg,
								final EntityManager entityManager,
								final Marshaller marshaller) {
		this(modelObjectType,dbEntityType,
			 // create a default transformer using the marshaller
			 DBBase.createTransformsDBEntityIntoModelObjectUsing(marshaller,
																 modelObjectType),
			 dbCfg,
			 entityManager,
			 marshaller);
	}
	public DBBaseForModelObject(final Class<M> modelObjectType,
								final Class<DB> dbEntityType,
								final TransformsDBEntityIntoModelObject<DB,M> dbEntityIntoModelObjectTransformer,
								final DBModuleConfig dbCfg,
								final EntityManager entityManager,
								final Marshaller marshaller) {
		super(dbCfg,
			  entityManager,
			  marshaller);
		_modelObjectType = modelObjectType;
		_DBEntityType = dbEntityType;
		_dbEntityIntoModelObjectTransformer = dbEntityIntoModelObjectTransformer;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONVERTERS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public M dbEntityToModelObject(final SecurityContext securityContext,
								   final DB dbEntity) {
		M out = _dbEntityIntoModelObjectTransformer.dbEntityToModelObject(securityContext,
																		  dbEntity);
		if (out == null) throw new IllegalStateException("The model object returned by the db entity '" + dbEntity.getClass() + "' to model object transformer is null!");
		return out;
	}
	M _transformDBEntityToModelObject(final SecurityContext securityContext,
									  final DB dbEntity) {
		M out = this.dbEntityToModelObject(securityContext,
										   dbEntity);
		// ensure the tracking info ant entity version are set
		if (dbEntity instanceof HasTrackingInfo) {
			out.setTrackingInfo(((HasTrackingInfo)dbEntity).getTrackingInfo());
		}
		if (dbEntity instanceof HasEntityVersion) {
			out.setEntityVersion(((HasEntityVersion)dbEntity).getEntityVersion());
		}
		return out;
	}
	/**
	 * Builds the primary key from the model object
	 * @param entity
	 * @return
	 */
	protected PK dbEntityPrimaryKeyFor(final M entity) {
		// the key is an unique column primary key
		O oid = entity.getOid();
		PK outKey = this.dbEntityPrimaryKeyFor(oid);
		return outKey;
	}
	/**
	 * Builds the primary key for the given oid
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected PK dbEntityPrimaryKeyFor(final O oid) {
		return (PK)DBPrimaryKeyForModelObjectImpl.from(oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	QUERY WRAPPER
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract class QueryWrapperBase<SELF_TYPE extends QueryWrapperBase<SELF_TYPE>>
				     extends ContainsPredicatesBase<SELF_TYPE> {
		////////// --- Abstract methods
		public abstract <T> CriteriaQuery<T> getCriteriaQuery();
		
		////////// --- Constructors
		protected QueryWrapperBase(final CriteriaBuilder builder) {
			super(builder);
		}

		////////// --- Methods
		public CriteriaQuery<? extends DB> composeCriteriaQuery() {
			return this.composeCriteriaQuery(this.getCriteriaQuery());
		}
		public <T> CriteriaQuery<T> composeCriteriaQuery(final CriteriaQuery<T> criteriaQuery) {
			if (CollectionUtils.hasData(_filterPredicates)) criteriaQuery.where(_filterPredicates.toArray(new Predicate[_filterPredicates.size()]));
			return criteriaQuery;
		}
		public <T> Subquery<T> createSubQuery(final Class<T> dbEntityColType) {
			Subquery<T> subQuery = this.getCriteriaQuery()
									   .subquery(dbEntityColType);
			return subQuery;
		}

		
		////////// --- Order by
		@SuppressWarnings("unchecked")
		public SELF_TYPE setAscendingOrderBy(final String... dbCols) {
			if (CollectionUtils.isNullOrEmpty(dbCols)) return (SELF_TYPE)this;

			CriteriaBuilder builder = this.getCriteriaBuilder();
			Root<? extends DB> root = this.getRoot();
			List<Order> listOrder = Stream.of(dbCols)
										  .map(dbCol -> builder.asc(root.get(dbCol)))
										  .collect(Collectors.toList());
			this.getCriteriaQuery()
				.orderBy(listOrder);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE setDescendingOrderBy(final String... dbCols) {
			if (CollectionUtils.isNullOrEmpty(dbCols)) return (SELF_TYPE)this;

			CriteriaBuilder builder = this.getCriteriaBuilder();
			Root<? extends DB> root = this.getRoot();
			List<Order> listOrder = Stream.of(dbCols)
										  .map(dbCol -> builder.desc(root.get(dbCol)))
										  .collect(Collectors.toList());
			this.getCriteriaQuery()
				.orderBy(listOrder);
			return (SELF_TYPE)this;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SUB-QUERY WRAPPER
/////////////////////////////////////////////////////////////////////////////////////////				     
	@Accessors(prefix="_")
	protected abstract class SubQueryWrapperBase<T,
												 SELF_TYPE extends SubQueryWrapperBase<T,SELF_TYPE>>
				     extends ContainsPredicatesBase<SELF_TYPE> {
		////////// --- Abstract methods
		@Getter private final Subquery<T> _subQuery;
		@Getter private final Root<DB> _subQueryRoot;
		
		
		////////// --- Constructors
		protected SubQueryWrapperBase(final CriteriaBuilder builder,
									  final Subquery<T> subQuery) {
			super(builder);
			_subQuery = subQuery;
			_subQueryRoot = _subQuery.from(_DBEntityType);
		}
		
		////////// --- Methods
		@Override
		public Root<DB> getRoot() {
			return _subQueryRoot;
		}
		public Subquery<T> composeSubQuery() {
			return this.composeSubQuery((Expression<T>)null);
		}
		public Subquery<T> composeSubQuery(final FactoryFrom<CriteriaBuilder,Expression<T>> selectExpressionFactory) {
			return this.composeSubQuery(selectExpressionFactory.from(this.getCriteriaBuilder()));
			
		}
		public Subquery<T> composeSubQuery(final Expression<T> selectExpression) {
			if (selectExpression != null) this.getSubQuery()
											  .select(selectExpression);
			if (CollectionUtils.hasData(_filterPredicates)) this.getSubQuery()
															    .where(_filterPredicates.toArray(new Predicate[_filterPredicates.size()]));
			return this.getSubQuery();
		}
		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	BASE FOR QUERY AND SUBQUERY WRAPPERS
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
	protected abstract class ContainsPredicatesBase<SELF_TYPE extends ContainsPredicatesBase<SELF_TYPE>> {
		////////// --- Fields
		@Getter protected final CriteriaBuilder _criteriaBuilder;

				protected Collection<Predicate> _filterPredicates;
				
		////////// --- Root		
		public abstract <RDB extends DB> Root<RDB> getRoot();
		
		@SuppressWarnings("unchecked")
		public <RDB extends DB> Root<RDB> getRootAs(final Class<RDB> dbEntityType) {
			return (Root<RDB>)this.getRoot();
		}
				
		////////// --- Predicate addition
		@SuppressWarnings("unchecked")
		public SELF_TYPE addPredicate(final Predicate pred) {
			if (_filterPredicates == null) _filterPredicates = Lists.newArrayList();
			_filterPredicates.add(pred);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByIsNull(final String dbEntityCol) {
			Predicate oidPredicate = this.getCriteriaBuilder()
										 .isNull(this.getRoot().<String>get(dbEntityCol));
			this.addPredicate(oidPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByEntityTypePredicate(final Class<? extends DBEntity> dbEntityType) {
			// type(e) = dbEntityType
			Predicate typePredicate = this.getCriteriaBuilder()
										  .equal(this.getRoot().type(),
												 this.getCriteriaBuilder().literal(dbEntityType));
			this.addPredicate(typePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByOidPredicate(final OID oid,final String dbEntityCol) {
			Predicate oidPredicate = this.getCriteriaBuilder()
										 .equal(this.getRoot().<String>get(dbEntityCol),
										 		oid.asString());
			this.addPredicate(oidPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByBooleanPredicate(final boolean bool,final String dbEntityCol) {
			Predicate dateRangePredicate = this.getCriteriaBuilder()
											   .equal(this.getRoot().<Boolean>get(dbEntityCol),
										 			  bool);
			this.addPredicate(dateRangePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByDateRangePredicate(final Range<Instant> dateRange,final String dbEntityCol) {
			Predicate dateRangePredicate = this.buildDateRangePredicate(this.getCriteriaBuilder(),this.getRoot(),dbEntityCol,
													   					dateRange);
			this.addPredicate(dateRangePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterBySecurityPrincipalPredicate(final SecurityPrincipalOID principalOid,final String dbEntityCol) {
			Predicate userCodePredicate = this.buildSecurityPrincipalPredicate(this.getCriteriaBuilder(),this.getRoot(),
															  	 			   dbEntityCol,principalOid);
			this.addPredicate(userCodePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterBySecurityPrincipalPredicate(final SecurityPrincipalID principalId,final String dbEntityCol) {
			Predicate userCodePredicate = this.buildSecurityPrincipalPredicate(this.getCriteriaBuilder(),this.getRoot(),
															  	  			   dbEntityCol,principalId);
			this.addPredicate(userCodePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByNumberPredicate(final Number num,final String dbEntityCol) {
			Predicate numPredicate = this.getCriteriaBuilder()
										 .equal(this.getRoot().<Number>get(dbEntityCol),
										 		num);
			this.addPredicate(numPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public <E extends Enum<E>> SELF_TYPE addFilterByEnumPredicate(final E en,final String dbEntityCol) {
			Predicate enumPredicate = this.getCriteriaBuilder()
										  .equal(this.getRoot().<E>get(dbEntityCol),
										 		 en);
			this.addPredicate(enumPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public <E extends Enum<E>> SELF_TYPE addFilterByEnumWithinPredicate(final Collection<E> enums,final String dbEntityCol) {
			if (CollectionUtils.isNullOrEmpty(enums)) return (SELF_TYPE)this;
			
			CriteriaBuilder.In<E> inPredicate = this.getCriteriaBuilder()
										  		    .in(this.getRoot().<E>get(dbEntityCol)); 
			enums.forEach(inPredicate::value);
			
			this.addPredicate(inPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public <T> SELF_TYPE addSubQueryPredicate(final Subquery<T> subQuery,final String dbEntityCol,final Class<T> dbEntityColType) {
			Predicate subQueryPredicate = this.getCriteriaBuilder()
											  .equal(this.getRoot().<T>get(dbEntityCol),subQuery);
			this.addPredicate(subQueryPredicate);
			return (SELF_TYPE)this;
		}
		
		////////// --- Utility methods
		public Predicate buildDateRangePredicate(final CriteriaBuilder builder,
											     final Root<? extends DB> root,final String dbColName,
												 final Range<Instant> dateRange) {
			Predicate outPredicate = null;
			if (dateRange != null) {
				if (dateRange.hasLowerBound() && dateRange.hasUpperBound()) {
					outPredicate = builder.between(root.<Instant>get(dbColName),
												   dateRange.getLowerBound(),dateRange.getUpperBound());
				} else if (dateRange.hasLowerBound()) {
					outPredicate = builder.greaterThanOrEqualTo(root.<Instant>get(dbColName),
																dateRange.getLowerBound());
				} else if (dateRange.hasUpperBound()) {
					outPredicate = builder.lessThanOrEqualTo(root.<Instant>get(dbColName),
															 dateRange.getUpperBound());
				}
			}
			return outPredicate;
		}
		public Predicate buildBooleanPredicate(final CriteriaBuilder builder,
											   final Root<? extends DB> root,final String dbColName,
											   final boolean bool) {
			Predicate outPredicate = builder.equal(root.<String>get(dbColName),
											 	   bool);
			return outPredicate;
		}
		public Predicate buildSecurityPrincipalPredicate(final CriteriaBuilder builder,
										    	 		 final Root<? extends DB> root,final String dbColName,
										    	 		 final SecurityPrincipalID principalId) {
			Predicate outPredicate = null;
			if (principalId != null) {
				outPredicate = builder.equal(root.<String>get(dbColName),
											 principalId.asString());
			}
			return outPredicate;
		}
		public Predicate buildSecurityPrincipalPredicate(final CriteriaBuilder builder,
												 		 final Root<? extends DB> root,final String dbColName,
												 		 final SecurityPrincipalOID userOid) {
			Predicate outPredicate = null;
			if (userOid != null) {
				outPredicate = builder.equal(root.<String>get(dbColName),
											 userOid.asString());
			}
			return outPredicate;
		}
		public Predicate buildIntegerRangePredicate(final CriteriaBuilder builder,
												   	final Root<? extends DB> root,final String dbColName,
												   	final Range<Integer> intRange) {

			Predicate outPredicate = null;
			if (intRange != null) {
				if (intRange.hasLowerBound() && intRange.hasUpperBound()) {
					if (intRange.getLowerBound().equals(intRange.getUpperBound())) {
						outPredicate = builder.equal(root.<Integer>get(dbColName),
													 intRange.getLowerBound());
					} else {
						outPredicate = builder.between(root.<Integer>get(dbColName),
													   intRange.getLowerBound(),intRange.getUpperBound());
					}
				} else if (intRange.hasLowerBound()) {
					outPredicate = builder.greaterThanOrEqualTo(root.<Integer>get(dbColName),
																intRange.getLowerBound());
				} else if (intRange.hasUpperBound()) {
					outPredicate = builder.lessThanOrEqualTo(root.<Integer>get(dbColName),
															 intRange.getLowerBound());
				}
			}
			return outPredicate;
		}
	}
}
