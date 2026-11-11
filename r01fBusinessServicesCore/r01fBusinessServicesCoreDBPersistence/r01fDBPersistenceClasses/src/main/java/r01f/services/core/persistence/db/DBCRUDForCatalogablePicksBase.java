package r01f.services.core.persistence.db;

import java.util.Collection;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import r01f.facets.HasOID;
import r01f.model.catalogable.CatalogableIDs.CatalogableObjectTypeID;
import r01f.model.catalogable.CatalogableOIDs.CatalogableObjectOID;
import r01f.model.catalogable.CatalogableOIDs.CatalogablePickOID;
import r01f.model.catalogable.CatalogablePicksChangesBase;
import r01f.model.catalogable.ContainsPicksForCatalogableObject;
import r01f.model.persistence.CRUDError;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.CRUDResultBuilder;
import r01f.model.security.context.SecurityContext;
import r01f.patterns.Factory;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityBase;
import r01f.services.core.persistence.db.entities.DBEntityForCatalogablePickPrimaryKeyBase;
import r01f.services.core.persistence.internal.interfaces.CoreCRUDServicesForCatalogablePicksBase;
import r01f.util.types.collections.CollectionUtils;


@Slf4j
public abstract class DBCRUDForCatalogablePicksBase<O extends CatalogablePickOID,P extends HasOID<O>,S extends ContainsPicksForCatalogableObject<P>,
											 		PK extends DBEntityForCatalogablePickPrimaryKeyBase,DB extends DBEntityBase>
	          extends DBBase
           implements CoreCRUDServicesForCatalogablePicksBase<S> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Class<DB> _DBPickType;
	private final Factory<DB> _DBPickFactory;

	private final Class<S> _picksType;
	private final Factory<S> _picksFactory;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	protected DBCRUDForCatalogablePicksBase(final Class<S> picksType,final Factory<S> picksFactory,
											final Class<DB> dbPickType,final Factory<DB> dbPickFactory,
						   		   			final DBModuleConfig dbCfg,
						   		   			final EntityManager entityManager) {
		super(dbCfg,
			  entityManager,
			  null);	// not using the marshaller
		_picksType = picksType;
		_picksFactory = picksFactory;

		_DBPickType = dbPickType;
		_DBPickFactory = dbPickFactory;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<S> load(final SecurityContext securityContext,
							  final CatalogableObjectOID catalogableObjectOid,
							  final CatalogableObjectTypeID catalogableObjectTypeId) {
		log.debug("> loading picks for object with oid={} of type={}",
				  catalogableObjectOid,catalogableObjectTypeId);
		// [1] - Load all records for the catalogable object
		Collection<DB> dbPicks = _loadDBPicksFor(catalogableObjectOid,
												 catalogableObjectTypeId);

		// [2] - Create the picks object
		S outPicks =  _picksFactory.create();
		outPicks.setCatalogableObjectOid(catalogableObjectOid);
		outPicks.setCatalogableObjectType(catalogableObjectTypeId);
		if (CollectionUtils.hasData(dbPicks)) {
			for (DB dbPick : dbPicks) {
				P pick = _transformDBPickIntoPick(dbPick);
				outPicks.addPick(pick);
			}
		}
		// [3] - Return
		return CRUDResultBuilder.using(securityContext)
							    .on(_picksType)
							    .loaded()
							    .entity(outPicks);
	}
	private Collection<DB> _loadDBPicksFor(final CatalogableObjectOID catalogableObjectOid,
										   final CatalogableObjectTypeID catalogableObjectTypeId) {
		CriteriaBuilder dbCriteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<DB> dbQuery = dbCriteriaBuilder.createQuery(_DBPickType);
		Root<DB> dbRoot = dbQuery.from(_DBPickType);
		Predicate targetObjOidPredicate = dbCriteriaBuilder.equal(dbRoot.<String>get("_targetObjectOid"),
							 							 		  catalogableObjectOid.asString());
		Predicate targetObjTypePredicate = dbCriteriaBuilder.equal(dbRoot.<String>get("_targetObjectType"),
							 							 		   catalogableObjectTypeId.asString());
		dbQuery.where(dbCriteriaBuilder.and(targetObjOidPredicate,
											targetObjTypePredicate));
		Collection<DB> dbEntities = _entityManager.createQuery(dbQuery)
												  .getResultList();
		return dbEntities;
	}
	protected abstract P _transformDBPickIntoPick(final DB dbPick);
/////////////////////////////////////////////////////////////////////////////////////////
//	SAVE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<S> save(final SecurityContext securityContext,
						  	  final S picks) {
		log.debug("> saving {} picks for object with oid={} of type={}",
				  picks.getPicks() != null ? picks.getPicks().size() : 0,
				  picks.getCatalogableObjectOid(),picks.getCatalogableObjectType());

		// [1] - Load the currently existing picks for the target object
		S currPicks = this.load(securityContext,
								picks.getCatalogableObjectOid(),
								picks.getCatalogableObjectType())
						  .getOrThrow();
		// [2] - Compute the differences
		CatalogablePicksChangesBase<P> picksChanges = _changesBetween(currPicks.getPicks(),
																	  picks.getPicks());

		log.debug("\t...{} picks will be deleted, {} picks will be created and {} picks will remain unchanged",
				  picksChanges.getDeletedPicksSet().size(),
				  picksChanges.getNewPicksSet().size(),
				  picksChanges.getUnchangedPicksSet().size());

		// [3] - Delete deleted picks
		if (CollectionUtils.hasData(picksChanges.getDeletedPicksSet())) {
			log.debug("\t...DELETE {} picks",
					  picksChanges.getDeletedPicksSet().size());
			for (P pick : picksChanges.getDeletedPicksSet()) {
				PK dbPickPK = _dbPrimaryKeyFor(picks.getCatalogableObjectOid(),
											   picks.getCatalogableObjectType(),
											   pick);
				_delete(securityContext,
						dbPickPK);
			}
		}

		// [4] - Create new picks
		if (CollectionUtils.hasData(picksChanges.getNewPicksSet())) {
			log.debug("\t...CREATE {} picks",
					  picksChanges.getNewPicksSet().size());
			for (P pick : picksChanges.getNewPicksSet()) {
				_create(securityContext,
						picks.getCatalogableObjectOid(),picks.getCatalogableObjectType(),
						pick);
			}
		}
		// [3] - Return
		CRUDResult<S> outResult = CRUDResultBuilder.using(securityContext)
												   .on(_picksType)
												   .created()
												   .entity(picks);
		return outResult;
	}
	private DB _create(final SecurityContext securityContext,
					   final CatalogableObjectOID catalogableObjectOid,
					   final CatalogableObjectTypeID catalogableObjectTypeId,
					   final P pick) {
		// [1] - Create a dbPick
		DB dbPick = _DBPickFactory.create();
		_transferPickToDBPick(securityContext,
							  catalogableObjectOid,
							  catalogableObjectTypeId,
							  pick,dbPick);
		// [2] - persist
		this.getEntityManager()
				.persist(dbPick);
		// [3] - Return
		return dbPick;
	}
	protected abstract <C extends CatalogablePicksChangesBase<P>> C _changesBetween(final Set<P> current,
																					final Set<P> received);

	protected abstract void _transferPickToDBPick(final SecurityContext securityContext,
												  final CatalogableObjectOID catalogableObjectOid,
												  final CatalogableObjectTypeID catalogableObjectTypeId,
												  final P pick,final DB dbPick);
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<S> delete(final SecurityContext securityContext,
								final CatalogableObjectOID catalogableObjectOid,
								final CatalogableObjectTypeID catalogableObjectTypeId) {
		log.debug("> deleting picks for object with oid={} of type={}",
				  catalogableObjectOid,catalogableObjectTypeId);
		// [1] - Load the currently existing picks
		CRUDResult<S> picksLoadResult = this.load(securityContext,
												  catalogableObjectOid,
												  catalogableObjectTypeId);
		// [2] - Delete
		CRUDResult<S> outResult = null;
		if (picksLoadResult.hasSucceeded()) {
			if (CollectionUtils.hasData(picksLoadResult.getOrThrow().getPicks())) {
				Set<P> deletedPicksSet = picksLoadResult.getOrThrow().getPicks();
				log.debug("\t...DELETE {} picks",
						  deletedPicksSet.size());
				for (P pick : deletedPicksSet) {
					PK dbPickPK = _dbPrimaryKeyFor(catalogableObjectOid,
												   catalogableObjectTypeId,
												   pick);
					_delete(securityContext,
							dbPickPK);
				}
				outResult = CRUDResultBuilder.using(securityContext)
											 .on(_picksType)
											 .deleted()
											 .entity(picksLoadResult.getOrThrow());
			} else {
				log.debug("\t...NO picks to DELETE for objet with oid={} of type={}",
						  catalogableObjectOid,catalogableObjectTypeId);
				outResult = CRUDResultBuilder.using(securityContext)
											 .on(_picksType)
											 .notDeleted()
											 .becauseClientRequestedEntityWasNOTFound()
											 .build();
			}
		} else {
			CRUDError<S> err = picksLoadResult.asCRUDError();
			log.error("\t...ERROR while deleting picks for objet with oid={} of type={}: {}",
					  catalogableObjectOid,catalogableObjectTypeId,
					  err.getErrorMessage(),
					  err.getPersistenceException());
			outResult = CRUDResultBuilder.using(securityContext)
										 .on(_picksType)
										 .notDeleted()
										 .because(err)
										 .build();
		}
		// [3] - Return
		return outResult;
	}
	private DB _delete(final SecurityContext securityContext,
					   final PK dbPickPK) {
		// [1] - Check the existence of the entity to be deleted
		DB dbPick = this.getEntityManager().find(_DBPickType,
								  	  	  		 dbPickPK);
		// [2] - Delete
		if (dbPick != null) {
			log.debug("\t...delete [pick] for {}",
					  dbPickPK.asString());
			this.getEntityManager().remove(dbPick);
		} else {
			log.warn("\t...could NOT delete [pick] for {}: NO record exists!",
					  dbPickPK.asString());
		}
		return dbPick;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract PK _dbPrimaryKeyFor(final CatalogableObjectOID catalogableObjectOid,
										   final CatalogableObjectTypeID catalogableObjectTypeId,
										   final P pick);
}
