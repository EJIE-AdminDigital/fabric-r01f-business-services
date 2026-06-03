package r01f.services.core.persistence.db;

import jakarta.persistence.EntityManager;

/**
 * Interface for the types that holds an {@link EntityManager}
 */
public interface HasEntityManager {
	/**
	 * @return an {@link EntityManager}
	 */
	public EntityManager getEntityManager();
}
