package r01f.services.core.persistence.db;

import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.model.HasTrackableFacet;
import r01f.model.HasTrackingInfo;
import r01f.model.ModelObject;
import r01f.model.facets.HasEntityVersion;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.HasMarshaller;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.config.DBPersistenceProviderVendorDBHints;


/**
 * Base type for every persistence layer type
 */
@Accessors(prefix="_")
@Slf4j
public abstract class DBBase
	       implements HasEntityManager,
	       			  HasMarshaller {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The entity manager obtained from the {@link EntityManager} {@link Provider}
	 */
	@Getter protected final EntityManager _entityManager;
	/**
	 * The db config
	 */
	@Getter protected final DBModuleConfig _dbConfig;
	/**
	 * Vendor-specific DB hints (obtained from the {@link DBModuleConfig} and the {@link EntityManager})
	 */
	@Getter protected final DBPersistenceProviderVendorDBHints _vendorDBHints;
	/**
	 * Marshaller
	 */
	@Getter protected final Marshaller _modelObjectsMarshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBase(final DBModuleConfig dbCfg,
				  final EntityManager entityManager,
				  final Marshaller marshaller) {
		_entityManager = entityManager;
		_dbConfig = dbCfg;
		_vendorDBHints = DBBase.getVendorDBHints(dbCfg,entityManager);
		
		_modelObjectsMarshaller = marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DB VENDOR HINTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static DBPersistenceProviderVendorDBHints getVendorDBHints(final DBModuleConfig dbConfig,
							   								   		  final EntityManager entityManager) {
		if (dbConfig == null) return new DBPersistenceProviderVendorDBHints(entityManager);
		return dbConfig.getDbSpec()
					   .getVendorDBHints();		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method that creates a transformer that uses the XML descriptor at the [db entity] to
	 * build a [model object]
	 * @param <DB>
	 * @param <M>
	 * @param marshaller
	 * @param modelObjectType
	 * @return
	 */
	public static <DB extends DBEntity,
				   M extends ModelObject> TransformsDBEntityIntoModelObject<DB,M> createTransformsDBEntityIntoModelObjectUsing(final Marshaller marshaller,
							  																								   final Class<M> modelObjectType) {
		return new TransformsDBEntityIntoModelObject<>() {
						@Override
						public M dbEntityToModelObject(final SecurityContext securityContext,
													   final DB dbEntity) {
								M outObj = null;
								// use the descriptor to build the model object
								if (dbEntity instanceof DBEntityHasModelObjectDescriptor hasDescriptor) {
									log.trace("The db entity of type {} implements {} so the db entity is translated bo model object using the XML descriptor",
											 dbEntity.getClass().getSimpleName(),DBEntityHasModelObjectDescriptor.class.getSimpleName());
									outObj = marshaller.forReading()
													   .fromXml(hasDescriptor.getDescriptor(),
																modelObjectType);
								} else {
									log.trace("The db entity of type {} does NOT implements {} so the db entity MUST be manually translated bo model object",
											 dbEntity.getClass().getSimpleName(),DBEntityHasModelObjectDescriptor.class.getSimpleName());
									outObj = ReflectionUtils.createInstanceOf(modelObjectType);
								}
								// copy some info from the dbEntity
								if (outObj != null) {
									if (dbEntity instanceof HasTrackingInfo hasTrackingInfoDBEntity
									 && outObj instanceof HasTrackableFacet hasTrackableFacetModelObj) {
										hasTrackableFacetModelObj.setTrackingInfo(hasTrackingInfoDBEntity.getTrackingInfo());
									} else {
										log.trace("either {} or {} has NOT tracking info",
												  dbEntity.getClass().getSimpleName(),outObj.getClass().getSimpleName());
									}
									if (dbEntity instanceof HasEntityVersion hasEntityVersionDBEntity
									 && outObj instanceof HasEntityVersion hasEntityVersionModelObj) {
										hasEntityVersionModelObj.setEntityVersion(hasEntityVersionDBEntity.getEntityVersion());
									} else {
										log.trace("either {} or {} has NOT entity version info",
												  dbEntity.getClass().getSimpleName(),outObj.getClass().getSimpleName());
									}
								}
								return outObj;
						}
				};
	}
}
