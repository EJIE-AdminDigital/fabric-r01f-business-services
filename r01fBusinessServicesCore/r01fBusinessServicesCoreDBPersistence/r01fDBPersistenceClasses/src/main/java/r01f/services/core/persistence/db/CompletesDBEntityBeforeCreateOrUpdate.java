package r01f.services.core.persistence.db;

import r01f.model.PersistableModelObject;
import r01f.model.persistence.PersistencePerformedOperation;
import r01f.model.security.context.SecurityContext;
import r01f.oids.OID;
import r01f.services.core.persistence.db.entities.DBEntityForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;

public interface CompletesDBEntityBeforeCreateOrUpdate<M extends PersistableModelObject<? extends OID>,DB extends DBEntityForModelObject<? extends DBPrimaryKeyForModelObject>> {
	/**
	 * Gives the CRUD layer the oportunity to complete the {@link DBEntity}... maybe setting dependent entities
	 * @param securityContext
	 * @param requestedOperation
	 * @param modelObj
	 * @param dbEntity
	 */
	public abstract void completeDBEntityBeforeCreateOrUpdate(final SecurityContext securityContext,
														      final PersistencePerformedOperation performedOp,
														      final M modelOjb,
														      final DB dbEntity);
}