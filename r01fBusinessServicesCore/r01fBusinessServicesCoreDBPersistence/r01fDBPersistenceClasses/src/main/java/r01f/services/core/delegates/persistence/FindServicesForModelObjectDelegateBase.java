package r01f.services.core.delegates.persistence;


import java.time.Instant;
import java.util.stream.Stream;

import r01f.eventbus.EventBus;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.FindOIDsResultBuilder;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectOID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;

/**
 * Service layer delegated type for CRUD find operations
 */
public abstract class FindServicesForModelObjectDelegateBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
			  extends PersistenceServicesDelegateForModelObjectBase<O,M>
		   implements FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public FindServicesForModelObjectDelegateBase(final ServicesCoreModuleConfig coreCfg,
												  final Class<M> modelObjectType,
												  final FindServicesForModelObject<O,M> findServices) {
		this(coreCfg,
			 modelObjectType,
			 findServices,
			 null);
	}
	public FindServicesForModelObjectDelegateBase(final ServicesCoreModuleConfig coreCfg,
												  final Class<M> modelObjectType,
												  final FindServicesForModelObject<O,M> findServices,
												  final EventBus eventBus) {
		super(coreCfg,
			  modelObjectType,
			  findServices,
			  eventBus);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
////////// --- FIND ALL
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findAll(final SecurityContext securityContext) {
		FindOIDsResult<O> outResults = this.getServiceImplAs(FindServicesForModelObject.class)
										   .findAll(securityContext);
		return outResults;
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findAllAsStream(final SecurityContext securityContext) {
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findAllAsStream(securityContext);
	}
////////// --- FIND BY CREATE DATE	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreateDate(final SecurityContext securityContext,
										  	  final Range<Instant> createDate) {
		// [0] - check the date
		if (createDate == null) return FindOIDsResultBuilder.using(securityContext)
															.on(_modelObjectType)
															.errorFindingOids()
															.causedByClientBadRequest("The date range MUST NOT be null in order to find entities by create date");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByCreateDate(securityContext,
									 createDate);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByCreateDateAsStream(final SecurityContext securityContext,
										  	  final Range<Instant> createDate) {
		// [0] - check the date
		if (createDate == null) throw new IllegalArgumentException("The date range MUST NOT be null in order to find entities by create date");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByCreateDateAsStream(securityContext,
											 createDate);
	}
////////// --- FIND BY LAST UPDATE DATE
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdateDate(final SecurityContext securityContext,
											  	  final Range<Instant> lastUpdateDate) {
		// [0] - check the date
		if (lastUpdateDate == null) return FindOIDsResultBuilder.using(securityContext)
																.on(_modelObjectType)
																.errorFindingOids()
																.causedByClientBadRequest("The date range MUST NOT be null in order to find entities by last update date");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByLastUpdateDate(securityContext,
									     lastUpdateDate);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByLastUpdateDateAsStream(final SecurityContext securityContext,
											  	  final Range<Instant> lastUpdateDate) {
		// [0] - check the date
		if (lastUpdateDate == null) throw new IllegalArgumentException("The date range MUST NOT be null in order to find entities by last update date");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByLastUpdateDateAsStream(securityContext,
												 lastUpdateDate);
	}
////////// --- FIND BY CREATOR OID	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
									   	   final SecurityPrincipalOID creatorUserOid) {
		// [0] - check the date
		if (creatorUserOid == null) return FindOIDsResultBuilder.using(securityContext)
																.on(_modelObjectType)
																.errorFindingOids()
																.causedByClientBadRequest("The user oid MUST NOT be null in order to find entities creator");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByCreator(securityContext,
								  creatorUserOid);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByCreatorAsStream(final SecurityContext securityContext,
									   	   final SecurityPrincipalOID creatorUserOid) {
		// [0] - check the date
		if (creatorUserOid == null) throw new IllegalArgumentException("The user oid MUST NOT be null in order to find entities creator");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByCreatorAsStream(securityContext,
										  creatorUserOid);
	}
////////// --- FIND BY CREATOR ID
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
									   	   final SecurityPrincipalID creatorPrincipalId) {
		// [0] - check the date
		if (creatorPrincipalId == null) return FindOIDsResultBuilder.using(securityContext)
																	.on(_modelObjectType)
																	.errorFindingOids()
																	.causedByClientBadRequest("The user code MUST NOT be null in order to find entities creator");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByCreator(securityContext,
							      creatorPrincipalId);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByCreatorAsStream(final SecurityContext securityContext,
									   	   final SecurityPrincipalID creatorPrincipalId) {
		// [0] - check the date
		if (creatorPrincipalId == null) throw new IllegalArgumentException("The user code MUST NOT be null in order to find entities creator");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByCreatorAsStream(securityContext,
										  creatorPrincipalId);
	}
////////// --- FIND BY LAST UPDATER OID	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
										       final SecurityPrincipalOID updaterPrincipalId) {
		// [0] - check the date
		if (updaterPrincipalId == null) return FindOIDsResultBuilder.using(securityContext)
																	.on(_modelObjectType)
																	.errorFindingOids()
																	.causedByClientBadRequest("The user oid MUST NOT be null in order to find entities by last updator user code");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByLastUpdater(securityContext,
								      updaterPrincipalId);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByLastUpdaterAsStream(final SecurityContext securityContext,
										       final SecurityPrincipalOID updaterPrincipalId) {
		// [0] - check the date
		if (updaterPrincipalId == null) throw new IllegalArgumentException("The user oid MUST NOT be null in order to find entities by last updator user code");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByLastUpdaterAsStream(securityContext,
											  updaterPrincipalId);
	}
////////// --- FIND BY LAST UPDATER ID	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
										       final SecurityPrincipalID lastUpdatorUserCode) {
		// [0] - check the date
		if (lastUpdatorUserCode == null) return FindOIDsResultBuilder.using(securityContext)
																	 .on(_modelObjectType)
																	 .errorFindingOids()
																	 .causedByClientBadRequest("The user code MUST NOT be null in order to find entities by last updator user code");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByLastUpdater(securityContext,
									  lastUpdatorUserCode);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByLastUpdaterAsStream(final SecurityContext securityContext,
										       final SecurityPrincipalID lastUpdatorUserCode) {
		// [0] - check the date
		if (lastUpdatorUserCode == null) throw new IllegalArgumentException("The user code MUST NOT be null in order to find entities by last updator user code");
		// [1] - do the find
		return this.getServiceImplAs(FindServicesForModelObject.class)
				   .findByLastUpdaterAsStream(securityContext,
											  lastUpdatorUserCode);
	}
}
