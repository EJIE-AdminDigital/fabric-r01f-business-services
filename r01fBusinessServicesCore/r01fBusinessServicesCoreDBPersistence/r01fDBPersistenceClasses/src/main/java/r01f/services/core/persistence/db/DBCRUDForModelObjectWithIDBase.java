package r01f.services.core.persistence.db;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import r01f.model.PersistableModelObjectWithID;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.CRUDResultBuilder;
import r01f.model.persistence.PersistenceOperationExecError;
import r01f.model.persistence.PersistenceOperationExecOK;
import r01f.model.persistence.PersistenceOperationResult;
import r01f.model.persistence.PersistenceServiceErrorTypes;
import r01f.model.security.context.SecurityContext;
import r01f.model.services.COREServiceMethod;
import r01f.objectstreamer.Marshaller;
import r01f.oids.OIDs;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
public abstract class DBCRUDForModelObjectWithIDBase<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>,
							     			   		 PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>> 
			  extends DBCRUDForModelObjectBase<O,M,
			  								   PK,DB> 
		   implements DBCRUDForModelObjectWithID<O,I,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBCRUDForModelObjectWithIDBase(final Class<M> modelObjectType,
								    	  final Class<DB> dbEntityType,
								    	  final DBModuleConfig dbCfg,
								    	  final EntityManager entityManager,
								    	  final Marshaller marshaller) {
		super(modelObjectType,dbEntityType,
			  dbCfg,
			  entityManager,
			  marshaller);
	}
	public DBCRUDForModelObjectWithIDBase(final Class<M> modelObjectType,
										  final Class<DB> dbEntityType,
										  final TransformsDBEntityIntoModelObject<DB,M> dbEntityIntoModelObjectTransformer,
										  final DBModuleConfig dbCfg,
										  final EntityManager entityManager,
										  final Marshaller marshaller) {
		super(modelObjectType,dbEntityType,
			  dbEntityIntoModelObjectTransformer,
			  dbCfg,
			  entityManager,
			  marshaller);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> loadById(final SecurityContext securityContext,
								  final I id) {
		// do the query
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<DB> criteriaQuery = criteriaBuilder.createQuery(_DBEntityType);
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);
		Predicate idPredicate = criteriaBuilder.equal(root.get("_id"), 
													  id.asString());
		criteriaQuery.where(idPredicate);
		
		List<DB> dbEntities = _entityManager.createQuery(criteriaQuery)
										    .getResultList();
		
		// alternatively, we could use a named query like this:
		// String namedQuery = Strings.customized("{}.loadById",
		//										  _DBEntityType.getSimpleName());
		// List<DB> dbEntities = _entityManager.createNamedQuery(namedQuery,
		//													  	 _DBEntityType)
		//			  						   .setParameter("id",id.asString())
		//			  						   .getResultList();
		DB dbEntity = CollectionUtils.hasData(dbEntities) ? CollectionUtils.firstOf(dbEntities) : null;
		if (dbEntity == null) return CRUDResultBuilder.using(securityContext)
													  .on(_modelObjectType)
													  .notLoaded()
													  .becauseClientRequestedEntityWasNOTFound()
													  .about("id",id.asString())
													  .build();
		// return
		CRUDResult<M> outCRUDResult = CRUDResultBuilder.using(securityContext)
													   .on(_modelObjectType)
													   .loaded()
													   .dbEntity(dbEntity)
													   .transformedToModelObjectUsing(this);
		return outCRUDResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> deleteById(final SecurityContext securityContext,
								    final I id) {
		// do the query
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<DB> criteriaQuery = criteriaBuilder.createQuery(_DBEntityType);
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);
		Predicate idPredicate = criteriaBuilder.equal(root.get("_id"), 
													  id.asString());
		criteriaQuery.where(idPredicate);
		
		List<DB> dbEntities = _entityManager.createQuery(criteriaQuery)
										    .getResultList();
		if (CollectionUtils.isNullOrEmpty(dbEntities)) {
			return CRUDResultBuilder.using(securityContext)
									.on(_modelObjectType)
									.notDeleted()
									.becauseClientRequestedEntityWasNOTFound()
									.about("id",id.asString())
									.build();
		}
		if (dbEntities.size() > 1) {
			return CRUDResultBuilder.using(securityContext)
									.on(_modelObjectType)
									.notDeleted()
									.becauseServerError("There exists more than a single " + _DBEntityType.getName() + " with id=" + id)
									.about("id",id.asString())
									.build();
		}

		log.debug("> deleting a {} entity with id={}",
				  _DBEntityType,
				  id.asString());

		CRUDResult<M> outResult = null;

		// [1]: Check the existence of the entity to be deleted
		DB dbEntity = dbEntities.get(0);
		
		// [2]: Delete the entity
		this.getEntityManager()
			.refresh(dbEntity);		// refresh the dbentity since it could be modified (ie by setting a bi-directional relation)
		this.getEntityManager()
			.remove(dbEntity);
		this.getEntityManager()
		 	.flush();

		M outModelObj =  _transformDBEntityToModelObject(securityContext,
											  			 dbEntity);
		outResult = CRUDResultBuilder.using(securityContext)
									 .on(_modelObjectType)
									 .deleted()
									 .entity(outModelObj);
		return outResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	MISC
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override 
	public PersistenceOperationResult<Boolean> exists(final SecurityContext securityContext,
												 	  final I id) {
		// just load by id and check if it exists
		PersistenceOperationResult<O> idOfResult = this.getOidOf(securityContext,
																 id);
		 if (idOfResult.hasSucceeded()) {
			 return new PersistenceOperationExecOK<>(COREServiceMethod.current(),
												     true);	// exists
	     } else if (idOfResult.hasFailed()
		 && idOfResult.asPersistenceOperationError()
		 			  .getErrorType().is(PersistenceServiceErrorTypes.ENTITY_NOT_FOUND)) {
			return new PersistenceOperationExecOK<>(COREServiceMethod.current(),
												    false);	// does NOT exists
		} else {
			PersistenceOperationExecError<O> error = idOfResult.asPersistenceOperationError();
			return new PersistenceOperationExecError<Boolean>(COREServiceMethod.current(),
												       		  error);	// exists
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public PersistenceOperationResult<I> getIdOf(final SecurityContext securityContext,
												 final O oid) {
		// do the query
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);

		criteriaQuery.select(root.get("_id"));	// deprecation of criteriaQuery.multiselect(root.get("_oid"));
		Predicate oidPredicate = criteriaBuilder.equal(root.<String>get("_oid"),
									 				   oid.asString());
		criteriaQuery.where(oidPredicate);
		List<String> ids = _entityManager.createQuery(criteriaQuery)
												.setHint(this.getVendorDBHints().getReadOnlyHintKey(),true)
									     .getResultList();
		
		// Compose the PersistenceOperationResult object
		PersistenceOperationResult<I> outResult = null;
		if (CollectionUtils.isNullOrEmpty(ids)) {
			outResult = new PersistenceOperationExecError<>(COREServiceMethod.current(),
															PersistenceServiceErrorTypes.ENTITY_NOT_FOUND,
														 	Strings.customized("Could NOT find any {} entity with oid={}",
																			   _modelObjectType,oid));	// does NOT exists
		} else if (ids.size() == 1) {
			String idStr = ids.getFirst();
			Class<I> idType = OIDs.idTypeFor(_modelObjectType);
			outResult = new PersistenceOperationExecOK<>(COREServiceMethod.current(),
														 OIDs.createOIDFromString(idType,idStr));
		} else if (ids.size() > 1) {
			outResult = new PersistenceOperationExecError<>(COREServiceMethod.current(),
															PersistenceServiceErrorTypes.ILLEGAL_STATUS,
															Strings.customized("There exists more than a single {} entitiy with oid={}",
																			   _modelObjectType,oid));
		}
		return outResult;
	}
	@Override
	public PersistenceOperationResult<O> getOidOf(final SecurityContext securityContext,
												  final I id) {
		// do the query
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<? extends DB> root = criteriaQuery.from(_DBEntityType);

		criteriaQuery.select(root.get("_oid"));	// deprecation of criteriaQuery.multiselect(root.get("_oid"));
		Predicate oidPredicate = criteriaBuilder.equal(root.<String>get("_id"),
									 				   id.asString());
		criteriaQuery.where(oidPredicate);
		List<String> oids = _entityManager.createQuery(criteriaQuery)
												.setHint(this.getVendorDBHints().getReadOnlyHintKey(),true)
									      .getResultList();
		
		// Compose the PersistenceOperationResult object
		PersistenceOperationResult<O> outResult = null;
		if (CollectionUtils.isNullOrEmpty(oids)) {
			outResult = new PersistenceOperationExecError<>(COREServiceMethod.current(),
															PersistenceServiceErrorTypes.ENTITY_NOT_FOUND,
														 	Strings.customized("Could NOT find any {} entity with id={}",
																			   _modelObjectType,id));	// does NOT exists
		} else if (oids.size() == 1) {
			String idStr = oids.getFirst();
			Class<O> oidType = OIDs.oidTypeFor(_modelObjectType);
			outResult = new PersistenceOperationExecOK<>(COREServiceMethod.current(),
														 OIDs.createOIDFromString(oidType,idStr));
		} else if (oids.size() > 1) {
			outResult = new PersistenceOperationExecError<>(COREServiceMethod.current(),
															PersistenceServiceErrorTypes.ILLEGAL_STATUS,
															Strings.customized("There exists more than a single {} entitiy with id={}",
																			   _modelObjectType,id));
		}
		return outResult;
	}
}
