package r01f.services.core.persistence.db;

import java.util.Set;

import r01f.model.PersistableModelObject;
import r01f.model.persistence.PersistenceException;
import r01f.model.security.context.SecurityContext;
import r01f.oids.PersistableObjectOID;

/**
 * Interface for types in charge of loading persistable model objects 
 * @param <O>
 * @param <M>
 */
public interface LoadsPersistableModelObject<O extends PersistableObjectOID,
					  						 M extends PersistableModelObject<O>> {
	/**
	 * Returns a entity from its identifier.
	 * @param securityContext the user auth data & context info
	 * @param oid the entity identifier
	 * @return the loaded record
	 * @throws PersistenceException
	 */
	public M load(final SecurityContext securityContext,
			   	  final O oid) throws PersistenceException;
	/**
	 * Returns all oids form the DB
	 * @param securityContext the user auth data & context info
	 * @return a {@link Set} with the loaded oids
	 * @throws PersistenceException
	 */
	public Set<O> loadAllOids(final SecurityContext securityContext) throws PersistenceException;
}
