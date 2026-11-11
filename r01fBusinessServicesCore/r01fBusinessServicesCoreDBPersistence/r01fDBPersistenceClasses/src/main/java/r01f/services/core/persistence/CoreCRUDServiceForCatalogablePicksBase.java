package r01f.services.core.persistence;

 
import com.google.common.reflect.TypeToken;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.model.catalogable.CatalogableIDs.CatalogableObjectTypeID;
import r01f.model.catalogable.CatalogableOIDs.CatalogableObjectOID;
import r01f.model.catalogable.CatalogableObjectPicksBase;
import r01f.model.persistence.CRUDResult;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.patterns.Factory;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForCatalogablePickBase;
import r01f.services.core.persistence.internal.interfaces.CoreCRUDServicesForCatalogablePicksBase;

@Singleton
@Accessors(prefix="_")
public abstract class CoreCRUDServiceForCatalogablePicksBase<P extends CatalogableObjectPicksBase<?,?,P>,
															 DB extends DBEntityForCatalogablePickBase>
     		  extends CorePersistenceServicesBase
     	   implements CoreCRUDServicesForCatalogablePicksBase<P> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Class<DB> _dbEntityType;
	protected final Factory<DB> _dbEntityFactory;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CoreCRUDServiceForCatalogablePicksBase(final ServicesCoreModuleConfig cfg,
										  		  final Class<DB> dbEntityType,final Factory<DB> dbEntityFactory,
										  		  final Marshaller modelObjectsMarshaller,
										  		  final EventBus eventBus,
										  		  final Provider<EntityManager> entityManagerProvider) {
		super(cfg,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
		_dbEntityType = dbEntityType;
		_dbEntityFactory = dbEntityFactory;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@Transactional
	@Override @SuppressWarnings("serial")
	public CRUDResult<P> load(final SecurityContext securityContext,
						      final CatalogableObjectOID catalogableObjectOid,final CatalogableObjectTypeID catalogableObjectTypeId) {
		if (catalogableObjectOid == null || catalogableObjectTypeId == null) throw new IllegalArgumentException("Catalogable object oid and type are mandatory!");
		return this.forSecurityContext(securityContext)
						.createDelegateAs(new TypeToken<CoreCRUDServicesForCatalogablePicksBase<P>>() { /* just ignore */ })
							.load(securityContext,
								  catalogableObjectOid,catalogableObjectTypeId);
	}
	@Transactional
	@Override @SuppressWarnings("serial")
	public CRUDResult<P> save(final SecurityContext securityContext,
							  final P picks) {
		if (picks == null) throw new IllegalArgumentException("Label picks object cannot be null!!");
		if (picks.getCatalogableObjectOid() == null || picks.getCatalogableObjectType() == null) throw new IllegalArgumentException("Catalogable object oid and type are mandatory in picks object!");
		return this.forSecurityContext(securityContext)
						.createDelegateAs(new TypeToken<CoreCRUDServicesForCatalogablePicksBase<P>>() { /* just ignore */ })
							.save(securityContext,
								  picks);
	}
	@Transactional
	@Override @SuppressWarnings("serial")
	public CRUDResult<P> delete(final SecurityContext securityContext,
							    final CatalogableObjectOID catalogableObjectOid,final CatalogableObjectTypeID catalogableObjectTypeId) {
		if (catalogableObjectOid == null || catalogableObjectTypeId == null) throw new IllegalArgumentException("Catalogable object oid and type are mandatory!");
		return this.forSecurityContext(securityContext)
						.createDelegateAs(new TypeToken<CoreCRUDServicesForCatalogablePicksBase<P>>() { /* just ignore */ })
							.delete(securityContext,
									catalogableObjectOid,catalogableObjectTypeId);
	}
}
