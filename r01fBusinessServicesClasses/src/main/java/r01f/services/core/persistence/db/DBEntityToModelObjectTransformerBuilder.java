package r01f.services.core.persistence.db;


import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.ModelObject;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasCreationData;
import r01f.model.facets.HasEntityVersion;
import r01f.model.facets.HasLastUpdateData;
import r01f.patterns.IsBuilder;
import r01f.securitycontext.SecurityContext;


/**
 * Transformer functions between {@link DBEntity} and {@link ModelObject}
 */
@Slf4j
public class DBEntityToModelObjectTransformerBuilder
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link ModelObject}
	 * @param securityContext
	 * @param modelObjType
	 * @return
	 */
	public static <DB extends DBEntity,
				   M extends ModelObject> Function<DB,M> createFor(final SecurityContext securityContext,
						  				   						   final TransformsDBEntityIntoModelObject<DB,M> dbEntityToModelObjectTransformer) {
		return DBEntityToModelObjectTransformerBuilder.createFor(securityContext,
																 dbEntity -> {
																		try {
																			// transform the dbentity to a model object
																			return dbEntityToModelObjectTransformer.dbEntityToModelObject(securityContext,
																																		  dbEntity);
																		} catch (final Throwable th) {
																			String descriptoInfo = null;
																			if (dbEntity instanceof DBEntityHasModelObjectDescriptor hasDescriptor) {
																				descriptoInfo = hasDescriptor.getDescriptor();																			}
																			log.error("DBEntityToModelObjectTransformerBuilder error with db registry {} :{} , descriptorInfo {}",
																					        th.getMessage(),
																							th,descriptoInfo);
																		}
																		return null;//Return null if any exception happens, so must be applied a no null filtering!
																});
	}
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link ModelObject}
	 * @param securityContext
	 * @param transformer another transformer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <DB extends DBEntity,
				   M extends ModelObject> Function<DB,M> createFor(final SecurityContext securityContext,
						  				   						   final Function<DB,M> transformer) {
		return dbEntity -> {
					try {
						// Transform to model object
						M outModelObj = transformer.apply(dbEntity);
						// ensure that the model object has the tacking info and entity version
						if (outModelObj != null) {
							if (outModelObj instanceof PersistableModelObject) {
								DBEntityToModelObjectTransformerBuilder.copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,
																															 (PersistableModelObject<? extends OID>)outModelObj);
							}
						}
						return outModelObj;
					} catch (Exception ex1) {
						log.error("DBEntityToModelObjectTransformerBuilder error :{}",ex1.getMessage(),ex1);
					} catch (Throwable ex2) {
						log.error("DBEntityToModelObjectTransformerBuilder error :{}",ex2.getMessage(),ex2);
					}
					return null;//Return null if any exception happens, so must be applied a not null filtering!

			  };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STATIC UTIL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private static void copyDBEntiyTrackingInfoAndEntityVersionToModelObject(final DBEntity dbEntity,
																		     final PersistableModelObject<?> modelObject) {
		// do not forget!
		ModelObjectTracking trackingInfo = new ModelObjectTracking();

		if (dbEntity instanceof HasCreationData hasCreationData) {
			if (hasCreationData.getCreateTimeStamp() != null) {
				trackingInfo.setCreateDate(hasCreationData.getCreateTimeStamp());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getCreateDate() != null) {
				trackingInfo.setCreateDate(modelObject.getTrackingInfo().getCreateDate());
			}
			if (hasCreationData.getCreatorUserOid() != null) {
				trackingInfo.setCreatorUserOid(hasCreationData.getCreatorUserOid());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getCreatorUserOid() != null) {
				trackingInfo.setCreatorUserOid(modelObject.getTrackingInfo().getCreatorUserOid());
			}
			if (hasCreationData.getCreatorUserCode() != null) {
				trackingInfo.setCreatorUserCode(hasCreationData.getCreatorUserCode());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getCreatorUserCode() != null) {
				trackingInfo.setCreatorUserCode(modelObject.getTrackingInfo().getCreatorUserCode());
			}
		}

		if (dbEntity instanceof HasLastUpdateData hasUpdateData) {
			if (hasUpdateData.getLastUpdateTimeStamp() != null) {
				trackingInfo.setLastUpdateDate(hasUpdateData.getLastUpdateTimeStamp());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getLastUpdateDate() != null) {
				trackingInfo.setLastUpdateDate(modelObject.getTrackingInfo().getLastUpdateDate());
			}
			if (hasUpdateData.getLastUpdatorUserOid() != null) {
				trackingInfo.setLastUpdatorUserOid(hasUpdateData.getLastUpdatorUserOid());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getLastUpdatorUserOid() != null) {
				trackingInfo.setLastUpdatorUserOid(modelObject.getTrackingInfo().getLastUpdatorUserOid());
			}
			if (hasUpdateData.getLastUpdatorUserCode() != null) {
				trackingInfo.setLastUpdatorUserCode(hasUpdateData.getLastUpdatorUserCode());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getLastUpdatorUserCode() != null) {
				trackingInfo.setLastUpdatorUserCode(modelObject.getTrackingInfo().getLastUpdatorUserCode());
			}
		}

		modelObject.setTrackingInfo(trackingInfo);

		if (dbEntity instanceof HasEntityVersion hasEntityVersion) {
			modelObject.setEntityVersion(hasEntityVersion.getEntityVersion());
		}
	}
}
