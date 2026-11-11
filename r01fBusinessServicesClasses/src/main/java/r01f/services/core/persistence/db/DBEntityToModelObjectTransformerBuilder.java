package r01f.services.core.persistence.db;


import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import r01f.model.ModelObject;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasCreationData;
import r01f.model.facets.HasEntityVersion;
import r01f.model.facets.HasImpersonatingSecurityPrincipal;
import r01f.model.facets.HasLastUpdateData;
import r01f.model.security.context.SecurityContext;
import r01f.oids.OID;
import r01f.patterns.IsBuilder;


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
								_copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,(PersistableModelObject<? extends OID>)outModelObj);
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
	private static void _copyDBEntiyTrackingInfoAndEntityVersionToModelObject(final DBEntity dbEntity,final PersistableModelObject<?> modelObject) {
		// do not forget!
		ModelObjectTracking trackingInfo = new ModelObjectTracking();

		// Create data
		if (dbEntity instanceof HasCreationData dbEntityHasCreationData) {
			if (dbEntityHasCreationData.getCreatedAt() != null) {
				trackingInfo.setCreatedAt(dbEntityHasCreationData.getCreatedAt());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getCreatedAt() != null) {
				trackingInfo.setCreatedAt(modelObject.getTrackingInfo().getCreatedAt());
			}
			if (dbEntityHasCreationData.getCreatorSecurityPrincipalOid() != null) {
				trackingInfo.setCreatorSecurityPrincipalOid(dbEntityHasCreationData.getCreatorSecurityPrincipalOid());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getCreatorSecurityPrincipalOid() != null) {
				trackingInfo.setCreatorSecurityPrincipalOid(modelObject.getTrackingInfo().getCreatorSecurityPrincipalOid());
			}
			if (dbEntityHasCreationData.getCreatorSecurityPrincipalId() != null) {
				trackingInfo.setCreatorSecurityPrincipalId(dbEntityHasCreationData.getCreatorSecurityPrincipalId());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getCreatorSecurityPrincipalId() != null) {
				trackingInfo.setCreatorSecurityPrincipalId(modelObject.getTrackingInfo().getCreatorSecurityPrincipalId());
			}
		}
		// Update data
		if (dbEntity instanceof HasLastUpdateData dbEntityHasUpdateData) {
			if (dbEntityHasUpdateData.getLastUpdatedAt() != null) {
				trackingInfo.setLastUpdatedAt(dbEntityHasUpdateData.getLastUpdatedAt());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getLastUpdatedAt() != null) {
				trackingInfo.setLastUpdatedAt(modelObject.getTrackingInfo().getLastUpdatedAt());
			}
			if (dbEntityHasUpdateData.getLastUpdaterSecurityPrincipalOid() != null) {
				trackingInfo.setLastUpdaterSecurityPrincipalOid(dbEntityHasUpdateData.getLastUpdaterSecurityPrincipalOid());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getLastUpdaterSecurityPrincipalOid() != null) {
				trackingInfo.setLastUpdaterSecurityPrincipalOid(modelObject.getTrackingInfo().getLastUpdaterSecurityPrincipalOid());
			}
			if (dbEntityHasUpdateData.getLastUpdaterSecurityPrincipalId() != null) {
				trackingInfo.setLastUpdaterSecurityPrincipalId(dbEntityHasUpdateData.getLastUpdaterSecurityPrincipalId());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getLastUpdaterSecurityPrincipalId() != null) {
				trackingInfo.setLastUpdaterSecurityPrincipalId(modelObject.getTrackingInfo().getLastUpdaterSecurityPrincipalId());
			}
		}
		// impersonating security principal
		if (dbEntity instanceof HasImpersonatingSecurityPrincipal dbEntityHasImpersonatingSecurityPrincipal) {
			if (dbEntityHasImpersonatingSecurityPrincipal.getImpersonatingSecurityPrincipalOid() != null) {
				trackingInfo.setImpersonatingSecurityPrincipalOid(dbEntityHasImpersonatingSecurityPrincipal.getImpersonatingSecurityPrincipalOid());
			} else if (modelObject.getTrackingInfo() != null
					&& modelObject.getTrackingInfo().getImpersonatingSecurityPrincipalOid() != null) {
				trackingInfo.setImpersonatingSecurityPrincipalOid(modelObject.getTrackingInfo().getImpersonatingSecurityPrincipalOid());
			}
		}

		modelObject.setTrackingInfo(trackingInfo);

		// entity version
		if (dbEntity instanceof HasEntityVersion hasEntityVersion) {
			modelObject.setEntityVersion(hasEntityVersion.getEntityVersion());
		}
	}
}
