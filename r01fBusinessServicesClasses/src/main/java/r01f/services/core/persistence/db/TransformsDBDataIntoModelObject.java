package r01f.services.core.persistence.db;

import r01f.model.ModelObject;
import r01f.model.security.context.SecurityContext;

/**
 * Interface for types that transforms DB data into a {@link ModelObject}
 * (used when loading a {@link ModelObject} from DB data directly)
 * @param <M>
 */
public interface TransformsDBDataIntoModelObject<M extends ModelObject> {
	/**
	 * Builds a {@link ModelObject} from DB data
	 * @param securityContext
	 * @param dbData
	 * @return a model object
	 */
	public abstract M dbDataToModelObject(final SecurityContext securityContext,
										  final Object[] dbData);
}
