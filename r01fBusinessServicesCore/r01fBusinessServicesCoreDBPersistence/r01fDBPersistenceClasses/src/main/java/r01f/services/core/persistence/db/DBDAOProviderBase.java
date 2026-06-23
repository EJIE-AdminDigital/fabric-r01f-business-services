package r01f.services.core.persistence.db;

import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import r01f.objectstreamer.Marshaller;
import r01f.services.core.ServicesCoreModuleConfig;

@RequiredArgsConstructor
public abstract class DBDAOProviderBase<D extends DBBase>
		   implements Provider<D> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final ServicesCoreModuleConfig _coreConfig;
	protected final EntityManager _entityManager;
	protected final Marshaller _modelObjectsMarshaller;
}
