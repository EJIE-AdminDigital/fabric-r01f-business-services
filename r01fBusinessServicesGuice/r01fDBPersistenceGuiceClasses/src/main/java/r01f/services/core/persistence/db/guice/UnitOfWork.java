package r01f.services.core.persistence.db.guice;

/**
 * This interface is used to gain manual control over the unit of work. This is
 * mostly to do work in non-request, non-transactional threads. Or where more
 * fine-grained control over the unit of work is required. Starting and ending a
 * unit of work directly corresponds to opening and closing a {@code Session},
 * {@code EntityManager} or {@code ObjectContainer} respectively.
 *
 * <p>
 * The Unit of Work referred to by UnitOfWork will always be local to the
 * calling thread. Be careful to end() in a finally block. Neither JPA, nor
 * Hibernate supports threadsafe sessions (reasoning behind thread-locality of
 * Unit of Work semantics).
 *
 * <ul>
 * <li>Using UnitOfWork with the PersistFilter inside a request is not
 * recommended.
 * <li>Using UnitOfWork with session-per-txn strategy is not terribly clever
 * either.
 * <li>Using UnitOfWork with session-per-request strategy but *outside* a
 * request (i.e. in a background or bootstrap thread) is probably a good use
 * case.
 * </ul>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public interface UnitOfWork {
	/**
	 * Starts a Unit Of Work. Underneath, causes a session to the data layer to be
	 * opened. If there is already one open, the invocation will do nothing. In this
	 * way, you can define arbitrary units-of-work that nest within one another
	 * safely.
	 *
	 * <p>
	 * Transaction semantics are not affected.
	 */
	void begin();

	/**
	 * Declares an end to the current Unit of Work. Underneath, causes any open
	 * session to the data layer to close. If there is no Unit of work open, then
	 * the call returns silently. You can safely invoke end() repeatedly.
	 *
	 * <p>
	 * Transaction semantics are not affected.
	 */
	void end();
}
