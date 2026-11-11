package r01f.services.core.persistence.db;

import r01f.model.ModelObject;
import r01f.model.PersistableObject;
import r01f.model.security.context.SecurityContext;
import r01f.oids.OID;
import r01f.patterns.Provider;

/**
 * Transfers the {@link PersistableObject}'s state to a given {@link DBEntity}
 * (used when creating or updating a {@link DBEntity} from a {@link ModelObject})
 * @param <M>
 * @param <DB>
 */
public interface TransfersModelObjectStateToDBEntity<M extends ModelObject,
													 DB extends DBEntity> {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public void setDBEntityFieldsFromModelObject(final SecurityContext securityContext,
												 final M modelObj,final DB dbEntity);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID> String oidAsStringOrNull(final Provider<O> oidProvider) {
		O oid = oidProvider.provideValue();
		return oid != null ? oid.asString() : null;
	}
}
