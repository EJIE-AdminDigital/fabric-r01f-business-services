package r01f.services.core.persistence.db;

import r01f.model.PersistableModelObject;
import r01f.model.persistence.PersistencePerformedOperation;
import r01f.securitycontext.SecurityContext;
import r01f.services.core.persistence.db.entities.DBEntityForModelObject;

public interface ListensToDBEntityPersistenceEvents<M extends PersistableModelObject<?>,DB extends DBEntityForModelObject<?>> {
	/**
	 * Event launched BEFORE a DB entity persistence operation is performed
	 * @param securityContext
	 * @param op
	 * @param modelObj
	 * @param dbEntity
	 */
	public void onBeforDBEntityPersistenceOperation(final SecurityContext securityContext,
													final PersistencePerformedOperation op,
									  				final M modelObj,final DB dbEntity);
	/**
	 * Event launched AFTER a DB entity persistence operation is performed
	 * @param securityContext
	 * @param op
	 * @param dbEntity
	 * @param modelObj
	 */
	public void onAfterDBEntityPersistenceOperation(final SecurityContext securityContext,
													final PersistencePerformedOperation op,
													final DB dbEntity,final M modelObj);
}
