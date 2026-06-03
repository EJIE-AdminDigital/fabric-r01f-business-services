package r01f.services.core.persistence.db;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.facets.util.Facetables;
import r01f.guids.OIDs;
import r01f.guids.PersistableObjectOID;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.FindOIDsResultBuilder;
import r01f.model.persistence.FindResult;
import r01f.model.persistence.FindResultBuilder;
import r01f.model.persistence.FindSummariesResult;
import r01f.model.persistence.FindSummariesResultBuilder;
import r01f.objectstreamer.Marshaller;
import r01f.patterns.FactoryFrom;
import r01f.reflection.ReflectionUtils;
import r01f.securitycontext.SecurityContext;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.securitycontext.SecurityOIDs.UserOID;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
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
public abstract class DBFindForModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
							     			   PK extends DBPrimaryKeyForModelObject,DB extends DBEntity & DBEntityForModelObject<PK>>
			  extends DBBaseForModelObject<O,M,
			  							   PK,DB>
	       implements DBFindForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBFindForModelObjectBase(final Class<M> modelObjectType,
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
	public DBFindForModelObjectBase(final Class<M> modelObjectType,
								    final Class<DB> dbEntityType,
									final TransformsDBEntityIntoModelObject<DB,M> dbEntityIntoModelObjectTransformer,
									final DBModuleConfig dbCfg,
									final EntityManager entityManager,
									final Marshaller marshaller) {
		super(modelObjectType,
			  dbEntityType,
			  dbEntityIntoModelObjectTransformer,
			  dbCfg,
			  entityManager,
			  marshaller);
	}
	public DBFindForModelObjectBase(final Class<M> modelObjectType,final Class<DB> dbEntityType,
									final Function<DB,M> dbEntityIntoModelObjectTransformer,
									final DBModuleConfig dbCfg,
									final EntityManager entityManager,
									final Marshaller marshaller) {
		super(modelObjectType,
			  dbEntityType,
			  (securityContext,dbEntity) -> dbEntityIntoModelObjectTransformer.apply(dbEntity),	// TransformsDBEntityIntoModelObject
			  dbCfg,
			  entityManager,
			  marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsResult<O> findAll(final SecurityContext securityContext) {
		return new FindQueryWrapper()
						.findOidsUsing(securityContext);
	}
	@Override
	public FindOIDsResult<O> findByCreateDate(final SecurityContext securityContext,
											  final Range<Instant> createDate) {
		return new FindQueryWrapper()
						.addFilterByDateRangePredicate(createDate,"_createDate")
						.findOidsUsing(securityContext);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdateDate(final SecurityContext securityContext,
												  final Range<Instant> lastUpdateDate) {
		return new FindQueryWrapper()
						.addFilterByDateRangePredicate(lastUpdateDate,"_lastUpdateDate")
						.findOidsUsing(securityContext);
	}
	@Override
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
										   final UserOID creatorUserOid) {
		return new FindQueryWrapper()
						.addFilterByUserPredicate(creatorUserOid,"_creatorOid")
						.findOidsUsing(securityContext);
	}
	@Override
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
										   final LoginID creatorUserCode) {
		return new FindQueryWrapper()
						.addFilterByUserPredicate(creatorUserCode,"_creator")
						.findOidsUsing(securityContext);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdator(final SecurityContext securityContext,
											   final UserOID lastUpdatorOid) {
		return new FindQueryWrapper()
						.addFilterByUserPredicate(lastUpdatorOid,"_lastUpdatorOid")
						.findOidsUsing(securityContext);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdator(final SecurityContext securityContext,
											   final LoginID lastUpdatorUserCode) {
		return new FindQueryWrapper()
						.addFilterByUserPredicate(lastUpdatorUserCode,"_lastUpdator")
						.findOidsUsing(securityContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Find all the db entities 
	 * (just for testing purposes)
	 * @return
	 */
	protected Collection<DB> _findAllDBEntities() {
		// Build a find all query
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<DB> query = criteriaBuilder.createQuery(_DBEntityType);
		query.from(_DBEntityType);
		List<DB> dbObjs = _entityManager.createQuery(query)
									    //.setHint(this.getVendorDBHints().getReadOnlyHintKey(),true)
									    .getResultList();
		return dbObjs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	protected class FindQueryDBEntityWrapper
			extends QueryWrapperBase<FindQueryDBEntityWrapper> {
		@Getter protected final CriteriaBuilder _criteriaBuilder;
		@Getter protected final CriteriaQuery<? extends DB> _criteriaQuery;
		@Getter protected final Root<? extends DB> _root;

		public FindQueryDBEntityWrapper() {
			this(_DBEntityType);
		}
		public FindQueryDBEntityWrapper(final Class<? extends DB> dbEntityType) {
			_criteriaBuilder = _entityManager.getCriteriaBuilder();
			_criteriaQuery = _criteriaBuilder.createQuery(dbEntityType);
			_root = _criteriaQuery.from(dbEntityType);
		}
		public FindResult<M> findUsing(final SecurityContext securityContext) {
			this.composeCriteriaQuery();
			Collection<? extends DB> dbEntities = _entityManager.createQuery(_criteriaQuery)
																	.setHint(DBFindForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true)
																.getResultList();
			FindResult<M> outObjs = _buildResultsFromDBEntities(securityContext,
															    dbEntities);
			return outObjs;
		}
		public FindResult<M> findUsing(final SecurityContext securityContext,
									   final int numResults) {
			if (numResults <= 0) throw new IllegalArgumentException("The number of db entities to be returnes MUST be > 0");
			this.composeCriteriaQuery();
			Collection<? extends DB> dbEntities = _entityManager.createQuery(_criteriaQuery)
																	.setHint(DBFindForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true)
																	.setMaxResults(numResults)
																.getResultList();
			FindResult<M> outObjs = _buildResultsFromDBEntities(securityContext,
															    dbEntities);
			return outObjs;
		}
		public FindOIDsResult<O> findOidsUsing(final SecurityContext securityContext) {
			this.composeCriteriaQuery();
			Collection<? extends DB> dbEntities = _entityManager.createQuery(_criteriaQuery)
																	.setHint(DBFindForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true)
																.getResultList();
			FindOIDsResult<O> outOids = _buildOIDsResultsFromDBEntities(securityContext,
																		dbEntities);
			return outOids;
		}
	}
	@Accessors(prefix="_")
	protected class FindQueryWrapper
			extends QueryWrapperBase<FindQueryWrapper> {

		@Getter protected final CriteriaBuilder _criteriaBuilder;
		@Getter protected final CriteriaQuery<Tuple> _criteriaQuery;
		@Getter protected final Root<? extends DB> _root;

		public FindQueryWrapper(final String... colNames) {
			this(_DBEntityType,
				 colNames);
		}
		public FindQueryWrapper(final Class<? extends DB> dbEntityType,
								final String... colNames) {
			_criteriaBuilder = _entityManager.getCriteriaBuilder();
			_criteriaQuery = _criteriaBuilder.createTupleQuery();
			_root = _criteriaQuery.from(dbEntityType);

			////////// compose the sel cols
			Set<String> theColNames = Sets.newLinkedHashSet();

			// if no cols are set, at least return the descriptor
			// if (CollectionUtils.isNullOrEmpty(colNames)
			//  && DBEntityHasModelObjectDescriptor.class.isAssignableFrom(dbEntityType)) {
			// 	 theColNames.add("_descriptor");
			// }

			// ... ensure the oid is within the col names
			if (Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class)) {
				theColNames.add("_versionIndependentOid");
				theColNames.add("_version");
			} else {
				theColNames.add("_oid");
			}

			// add the col names
			if (CollectionUtils.hasData(colNames)) {
				for (String colName : colNames) theColNames.add(colName);
			}
			// use projections to return ONLY the given cols (see http://stackoverflow.com/questions/12618489/jpa-criteria-api-select-only-specific-columns)
			Selection<?>[] multisel = theColNames.stream()
											     .map(colName -> {
															Selection<?> outSel = _root.get(colName);
															if (outSel == null) throw new IllegalArgumentException(colName + " is NOT a valid selection at [db entity] of type " + _DBEntityType);
															outSel.alias(colName);
															return outSel;
												      })
											     .toArray(Selection[]::new);
			if (CollectionUtils.hasData(multisel)) _criteriaQuery.select(_criteriaBuilder.tuple(multisel));	// multiselect method is deprecated: _criteriaQuery.multiselect(multisel);
		}
		public FindOIDsResult<O> findOidsUsing(final SecurityContext securityContext) {
			this.composeCriteriaQuery();
			TypedQuery<Tuple> typedQuery = _entityManager.createQuery(_criteriaQuery);											
			if ( DBFindForModelObjectBase.this.getVendorDBHints() != null ){
				typedQuery.setHint(DBFindForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true);
			}
			List<Tuple> tupleResult =typedQuery.getResultList();											
			FindOIDsResult<O> outOids = _buildOIDsResultsFromDBTuples(securityContext,
															      	  tupleResult);
			return outOids;
		}
		public FindQueryExecResultTransform findEntitiesUsing(final SecurityContext securityContext) {
			this.composeCriteriaQuery();
			TypedQuery<Tuple> typedQuery = _entityManager.createQuery(_criteriaQuery);
			if ( DBFindForModelObjectBase.this.getVendorDBHints() != null ){
				typedQuery.setHint(DBFindForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true);
			}
			List<Tuple> tupleResult =typedQuery.getResultList();
			return new FindQueryExecResultTransform(securityContext,
													tupleResult);
		}
		public FindQueryExecResultTransform findSummariesUsing(final SecurityContext securityContext) {
			this.composeCriteriaQuery();
			TypedQuery<Tuple> typedQuery = _entityManager.createQuery(_criteriaQuery);			
			if ( DBFindForModelObjectBase.this.getVendorDBHints() != null ){
				typedQuery.setHint(DBFindForModelObjectBase.this.getVendorDBHints().getReadOnlyHintKey(),true);
			}
			List<Tuple> tupleResult =typedQuery.getResultList();
			return new FindQueryExecResultTransform(securityContext,
													tupleResult);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindQueryExecResultTransform {
		private final SecurityContext _securityContext;
		private final List<Tuple> _tupleResult;

		public FindResult<M> convertingTupleValuesToObjectUsing(final Function<Object[],M> dbTupleToEntityConverter) {
			FindResult<M> outFindResult = _buildResultFromTupleValues(_securityContext,
															 		  _tupleResult,dbTupleToEntityConverter);
			return outFindResult;
		}
		public FindResult<M> convertingTupleToObjectUsing(final Function<Tuple,M> dbTupleToEntityConverter) {
			FindResult<M> outFindResult = _buildResultFromTuple(_securityContext,
															    _tupleResult,dbTupleToEntityConverter);
			return outFindResult;
		}
		public FindResult<M> convertingTupleToObjectUsingDescriptorData() {
			if (!ReflectionUtils.isImplementing(_DBEntityType,DBEntityHasModelObjectDescriptor.class)) throw new UnsupportedOperationException(_DBEntityType + " does NOT implement " + DBEntityHasModelObjectDescriptor.class + ": " +
																																			   "cannot use the marshaller to convert the [db tuple] to [object]!");
			return this.convertingTupleToObjectUsing(// [db tuple] to [obj]
													 dbTuple -> {
														String desc = dbTuple.get("_descriptor",String.class);	// DBEntityHasModelObjectDescriptor

														M outObj = _modelObjectsMarshaller.forReading()
										   												  .fromXml(desc,
										   														   _modelObjectType);
														return outObj;
													 });
		}
		public <S extends SummarizedModelObject<M>> FindSummariesResult<M> convertingTupleValuesToSummaryUsing(final Function<Object[],S> dbTupleToSummaryConverter) {
			FindSummariesResult<M> outFindResult = _buildSummariesResultFromTupleValues(_securityContext,
															 					 	    _tupleResult,dbTupleToSummaryConverter);
			return outFindResult;
		}
		public <S extends SummarizedModelObject<M>> FindSummariesResult<M> convertingTupleToSummaryUsing(final Function<Tuple,S> dbTupleToSummaryConverter) {
			FindSummariesResult<M> outFindResult = _buildSummariesResultFromTuple(_securityContext,
															 					  _tupleResult,dbTupleToSummaryConverter);
			return outFindResult;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RESULTS BUILDING
/////////////////////////////////////////////////////////////////////////////////////////
	protected FindResult<M> _buildResultsFromDBEntities(final SecurityContext securityContext,
													    final Collection<? extends DB> dbEntities) {
		return _buildResultsFromDBEntities(securityContext,
										   dbEntities,
										   dbEntity -> _transformDBEntityToModelObject(securityContext,
												   									   dbEntity));
	}
	protected FindResult<M> _buildResultsFromDBEntities(final SecurityContext securityContext,
													    final Collection<? extends DB> dbEntities,
													    final Function<? super DB,M> dbEntityToModelObjectConverter) {
		FindResult<M> outResult = null;
		if (CollectionUtils.hasData(dbEntities)) {
			// dbEntity -> model object
			Collection<M> modelObjs = dbEntities.stream()
											 	.map(dbEntityToModelObjectConverter)
												.collect(Collectors.toList());
			// build result
			outResult = FindResultBuilder.using(securityContext)
										 .on(_modelObjectType)
										 .foundEntities(modelObjs);
		} else {
			outResult = FindResultBuilder.using(securityContext)
										 .on(_modelObjectType)
										 .noEntityFound();
		}
		return outResult;
	}
	protected FindResult<M> _buildResultFromTuple(final SecurityContext securityContext,
												  final Collection<Tuple> dbTuples,final Function<Tuple,M> dbTupleToObjectConverter) {
		FindResult<M> outObjs = null;
		if (CollectionUtils.hasData(dbTuples)) {
			Collection<M> objs = Lists.newArrayListWithExpectedSize(dbTuples.size());
			for (Tuple tuple : dbTuples) {
				M obj = dbTupleToObjectConverter.apply(tuple);
				objs.add(obj);
			}
			outObjs = FindResultBuilder.using(securityContext)
									   .on(_modelObjectType)
									   .foundEntities(objs);
		} else {
			outObjs = FindResultBuilder.using(securityContext)
									   .on(_modelObjectType)
									   .noEntityFound();
		}
		return outObjs;

	}
	protected FindResult<M> _buildResultFromTupleValues(final SecurityContext securityContext,
														final Collection<Tuple> dbTuples,final Function<Object[],M> dbTupleToObjectConverter) {
		FindResult<M> outObjs = null;
		if (CollectionUtils.hasData(dbTuples)) {
			Collection<M> objs = Lists.newArrayListWithExpectedSize(dbTuples.size());
			for (Tuple tuple : dbTuples) {
				M obj = dbTupleToObjectConverter.apply(tuple.toArray());
				objs.add(obj);
			}
			outObjs = FindResultBuilder.using(securityContext)
										 	.on(_modelObjectType)
										 	.foundEntities(objs);
		} else {
			outObjs = FindResultBuilder.using(securityContext)
									   .on(_modelObjectType)
									   .noEntityFound();
		}
		return outObjs;
	}
	protected FindOIDsResult<O> _buildOIDsResultsFromDBEntities(final SecurityContext securityContext,
													    		final Collection<? extends DB> dbEntities) {
		FindOIDsResult<O> outResult = null;
		if (CollectionUtils.hasData(dbEntities)) {
			Collection<O> oids = dbEntities.stream()
										   .map(dbEntity -> OIDs.<O,M>createOIDFor(_modelObjectType,
																	 	  		   dbEntity.getOid()))
										   .collect(Collectors.toList());
			outResult = FindOIDsResultBuilder.using(securityContext)
										 	 .on(_modelObjectType)
										 	 .foundEntitiesWithOids(oids);
		} else {
			outResult = FindOIDsResultBuilder.using(securityContext)
										 	 .on(_modelObjectType)
										 	 .noEntityFound();
		}
		return outResult;
	}
	protected FindOIDsResult<O> _buildOIDsResultsFromDBTuples(final SecurityContext securityContext,
													      	  final Collection<Tuple> dbTuples) {
		FindOIDsResult<O> outOids = null;
		if (CollectionUtils.hasData(dbTuples)) {
			Class<O> oidType = OIDs.oidTypeFor(_modelObjectType);
			boolean isVersionableOid = Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class);
			FactoryFrom<Tuple,O> oidFactory = _createDefaultOidFromTupleFactory(oidType,isVersionableOid);
			outOids = _buildOIDsResultsFromDBTuples(securityContext,
													dbTuples,
													oidFactory);
		} else {
			outOids = FindOIDsResultBuilder.using(securityContext)
										   .on(_modelObjectType)
										   .noEntityFound();
		}
		return outOids;
	}
	protected FindOIDsResult<O> _buildOIDsResultsFromDBTuples(final SecurityContext securityContext,
													      	  final Collection<Tuple> dbTuples,
													      	  final FactoryFrom<Tuple,O> oidFromStringFactory) {
		FindOIDsResult<O> outOids = null;
		if (CollectionUtils.hasData(dbTuples)) {
			Collection<O> oids = Lists.newArrayListWithExpectedSize(dbTuples.size());
			for (Tuple tuple : dbTuples) {
				O oid = oidFromStringFactory.from(tuple);
				oids.add(oid);
			}
			outOids = FindOIDsResultBuilder.using(securityContext)
										   .on(_modelObjectType)
										   .foundEntitiesWithOids(oids);
		} else {
			outOids = FindOIDsResultBuilder.using(securityContext)
										   .on(_modelObjectType)
										   .noEntityFound();
		}
		return outOids;
	}
	protected <S extends SummarizedModelObject<M>> FindSummariesResult<M> _buildSummariesResultFromDBEntities(final SecurityContext securityContext,
																											  final Collection<DB> dbEntities,final Function<DB,S> dbEntityToSummaryConverter) {
		FindSummariesResult<M> outSummaries = null;
		if (CollectionUtils.hasData(dbEntities)) {
			outSummaries = FindSummariesResultBuilder.using(securityContext)
													 .on(_modelObjectType)
													 .foundDBEntities(dbEntities)
													 .transformedToSummarizedModelObjectUsing(dbEntityToSummaryConverter);
		} else {
			outSummaries = FindSummariesResultBuilder.using(securityContext)
													 .on(_modelObjectType)
													 .noSummaryFound();
		}
		return outSummaries;
	}
	protected <S extends SummarizedModelObject<M>> FindSummariesResult<M> _buildSummariesResultFromTupleValues(final SecurityContext securityContext,
																										 	   final Collection<Tuple> dbTuples,final Function<Object[],S> dbTupleToSummaryConverter) {
		FindSummariesResult<M> outSummaries = null;
		if (CollectionUtils.hasData(dbTuples)) {
			Collection<S> summaries = Lists.newArrayListWithExpectedSize(dbTuples.size());
			for (Tuple tuple : dbTuples) {
				S summary = dbTupleToSummaryConverter.apply(tuple.toArray());
				summaries.add(summary);
			}
			outSummaries = FindSummariesResultBuilder.using(securityContext)
													 .on(_modelObjectType)
													 .foundSummaries(summaries);
		} else {
			outSummaries = FindSummariesResultBuilder.using(securityContext)
													 .on(_modelObjectType)
													 .noSummaryFound();
		}
		return outSummaries;
	}
	protected <S extends SummarizedModelObject<M>> FindSummariesResult<M> _buildSummariesResultFromTuple(final SecurityContext securityContext,
																										 final Collection<Tuple> dbTuples,final Function<Tuple,S> dbTupleToSummaryConverter) {
		FindSummariesResult<M> outSummaries = null;
		if (CollectionUtils.hasData(dbTuples)) {
			Collection<S> summaries = Lists.newArrayListWithExpectedSize(dbTuples.size());
			for (Tuple tuple : dbTuples) {
				S summary = dbTupleToSummaryConverter.apply(tuple);
				summaries.add(summary);
			}
			outSummaries = FindSummariesResultBuilder.using(securityContext)
													 .on(_modelObjectType)
													 .foundSummaries(summaries);
		} else {
			outSummaries = FindSummariesResultBuilder.using(securityContext)
													 .on(_modelObjectType)
													 .noSummaryFound();
		}
		return outSummaries;
	}
	private FactoryFrom<Tuple,O> _createDefaultOidFromTupleFactory(final Class<O> oidType,final boolean isVersionableOid) {
		// factory from dbTuple
		return dbTuple -> {
					O oid = null;
					if (isVersionableOid) {
						String versionIndependentOidAsString = (String)dbTuple.get(0);
						String versionOid = (String)dbTuple.get(1);
						oid = OIDs.createVersionableOIDFromString(oidType,
								 					 		      versionIndependentOidAsString,versionOid);
					} else {
						String oidAsString = (String)dbTuple.get(0);
						oid = OIDs.createOIDFromString(oidType,
													   oidAsString);
					}
					return oid;
		};
	}
}