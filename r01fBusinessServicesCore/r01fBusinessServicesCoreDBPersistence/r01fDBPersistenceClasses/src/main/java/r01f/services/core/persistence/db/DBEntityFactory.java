package r01f.services.core.persistence.db;

public interface DBEntityFactory<DB extends DBEntity> {
	/**
	 * @return a new {@link DBEntity} factory
	 */
	public DB create();
}
