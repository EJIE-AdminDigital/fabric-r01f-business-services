package r01f.services.interfaces;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.model.PersistableModelObject;
import r01f.oids.PersistableObjectOID;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ServiceProviders {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Provides a {@link CRUDServicesForModelObject} instance given a {@link PersistableModelObject} type
	 */
	public interface CRUDServiceByModelObjectOIDTypeProvider {
		public <O extends PersistableObjectOID,M extends PersistableModelObject<O>> CRUDServicesForModelObject<O,M> getFor(final Class<? extends PersistableObjectOID> type);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Provides a {@link FindServicesForModelObject} instance given a {@link PersistableModelObject}
	 */
	public interface FindServiceByModelObjectTypeProvider {
		public <O extends PersistableObjectOID,M extends PersistableModelObject<O>> FindServicesForModelObject<O,M> getFor(final Class<?> type);
	}
}
