package r01f.services.core.persistence;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.model.catalogable.geo.CatalogableGeoPicks;
import r01f.objectstreamer.Marshaller;
import r01f.patterns.Factory;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForCatalogableGeoPickBase;
import r01f.services.core.persistence.internal.interfaces.CoreCRUDServicesForCatalogableGeoPicks;

@Singleton
@Accessors(prefix="_")
public abstract class CoreCRUDServiceForCatalogableGeoPicksBase<DB extends DBEntityForCatalogableGeoPickBase>
     		  extends CoreCRUDServiceForCatalogablePicksBase<CatalogableGeoPicks,DB>
     	   implements CoreCRUDServicesForCatalogableGeoPicks {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CoreCRUDServiceForCatalogableGeoPicksBase(final ServicesCoreModuleConfig cfg,
										  			 final Class<DB> dbEntityType,final Factory<DB> dbEntityFactory,
										  			 final Marshaller modelObjectsMarshaller,
										  			 final EventBus eventBus,
										  			 final Provider<EntityManager> entityManagerProvider) {
		super(cfg,
			  dbEntityType,dbEntityFactory,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
	}
}
