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
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.guids.PersistableObjectOID;
import r01f.model.HasTrackingInfo;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasEntityVersion;
import r01f.objectstreamer.Marshaller;
import r01f.securitycontext.SecurityContext;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.securitycontext.SecurityOIDs.UserOID;
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
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract class QueryWrapperBase<SELF_TYPE extends QueryWrapperBase<SELF_TYPE>> {

		public abstract CriteriaBuilder getCriteriaBuilder();
		public abstract <T> CriteriaQuery<T> getCriteriaQuery();
		public abstract <RDB extends DB> Root<RDB> getRoot();

		protected Collection<Predicate> _filterPredicates;
		
		@SuppressWarnings("unchecked")
		public <RDB extends DB> Root<RDB> getRootAs(final Class<RDB> dbEntityType) {
			return (Root<RDB>)this.getRoot();
		}

		public CriteriaQuery<? extends DB> composeCriteriaQuery() {
			if (CollectionUtils.hasData(_filterPredicates)) this.getCriteriaQuery()
																.where(_filterPredicates.toArray(new Predicate[_filterPredicates.size()]));
			return this.getCriteriaQuery();
		}

		@SuppressWarnings("unchecked")
		public SELF_TYPE addPredicate(final Predicate pred) {
			if (_filterPredicates == null) _filterPredicates = Lists.newArrayList();
			_filterPredicates.add(pred);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByIsNull(final String dbEntityCol) {
			Predicate oidPredicate = this.getCriteriaBuilder().isNull(this.getRoot().<String>get(dbEntityCol));
			this.addPredicate(oidPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByEntityTypePredicate(final Class<? extends DBEntity> dbEntityType) {
			// type(e) = dbEntityType
			Predicate typePredicate = this.getCriteriaBuilder().equal(this.getRoot().type(),
																	  this.getCriteriaBuilder().literal(dbEntityType));
			this.addPredicate(typePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByOidPredicate(final OID oid,final String dbEntityCol) {
			Predicate oidPredicate = this.getCriteriaBuilder().equal(this.getRoot().<String>get(dbEntityCol),
										 							 oid.asString());
			this.addPredicate(oidPredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByBooleanPredicate(final boolean bool,final String dbEntityCol) {
			Predicate dateRangePredicate = this.getCriteriaBuilder().equal(this.getRoot().<Boolean>get(dbEntityCol),
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
		public SELF_TYPE addFilterByUserPredicate(final UserOID userCode,final String dbEntityCol) {
			Predicate userCodePredicate = this.buildUserPredicate(this.getCriteriaBuilder(),this.getRoot(),
															  	  dbEntityCol,userCode);
			this.addPredicate(userCodePredicate);
			return (SELF_TYPE)this;
		}
		@SuppressWarnings("unchecked")
		public SELF_TYPE addFilterByUserPredicate(final LoginID userCode,final String dbEntityCol) {
			Predicate userCodePredicate = this.buildUserPredicate(this.getCriteriaBuilder(),this.getRoot(),
															  	  dbEntityCol,userCode);
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
		public Predicate buildUserPredicate(final CriteriaBuilder builder,
										    final Root<? extends DB> root,final String dbColName,
											final LoginID userCode) {
			Predicate outPredicate = null;
			if (userCode != null) {
				outPredicate = builder.equal(root.<String>get(dbColName),
											 userCode.asString());
			}
			return outPredicate;
		}
		public Predicate buildUserPredicate(final CriteriaBuilder builder,
											final Root<? extends DB> root,final String dbColName,
											final UserOID userOid) {
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

}
