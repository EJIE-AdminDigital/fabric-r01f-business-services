package r01f.services.core.persistence.db.guice.jpa;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import r01f.services.core.persistence.db.guice.UnitOfWork;

     class JpaLocalTxnInterceptor 
implements MethodInterceptor {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
   	private JpaPersistService _emProvider = null;
	
	@Inject
	private UnitOfWork _unitOfWork = null;
	
	// Tracks if the unit of work was begun implicitly by this transaction.
	private final ThreadLocal<Boolean> _didWeStartWork = new ThreadLocal<>();
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public JpaLocalTxnInterceptor() {
		// default no args constructor 
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Object invoke(final MethodInvocation methodInvocation) throws Throwable {

		// Should we start a unit of work?
		if (!_emProvider.isWorking()) {
			_emProvider.begin();
			_didWeStartWork.set(true);
		}

		Transactional transactional = _readTransactionMetadata(methodInvocation);
		EntityManager em = _emProvider.get();

		// Allow 'joining' of transactions if there is an enclosing @Transactional
		// method.
		if (em.getTransaction().isActive()) {
			return methodInvocation.proceed();
		}

		final EntityTransaction txn = em.getTransaction();
		txn.begin();

		Object result;
		try {
			result = methodInvocation.proceed();
		} catch (Exception e) {
			// commit transaction only if rollback didnt occur
			if (_rollbackIfNecessary(transactional,e,txn)) {
				txn.commit();
			}

			// propagate whatever exception is thrown anyway
			throw e;
		} finally {
			// Close the em if necessary (guarded so this code doesn't run unless catch
			// fired).
			if (null != _didWeStartWork.get() && !txn.isActive()) {
				_didWeStartWork.remove();
				_unitOfWork.end();
			}
		}

		// everything was normal so commit the txn (do not move into try block above as
		// it
		// interferes with the advised method's throwing semantics)
		try {
			if (txn.isActive()) {
				if (txn.getRollbackOnly()) {
					txn.rollback();
				} else {
					txn.commit();
				}
			}
		} finally {
			// close the em if necessary
			if (null != _didWeStartWork.get()) {
				_didWeStartWork.remove();
				_unitOfWork.end();
			}
		}

		// or return result
		return result;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Transactional
	private static class InternalDummyTransactionalAnnotated {
		// nothing
	}
	// TODO(user): Cache this method's results.
	private Transactional _readTransactionMetadata(final MethodInvocation methodInvocation) {
		Transactional transactional;
		Method method = methodInvocation.getMethod();
		Class<?> targetClass = methodInvocation.getThis().getClass();

		transactional = method.getAnnotation(Transactional.class);
		if (null == transactional) {
			// If none on method, try the class.
			transactional = targetClass.getAnnotation(Transactional.class);
		}
		if (null == transactional) {
			// If there is no transactional annotation present, use the default
			transactional = InternalDummyTransactionalAnnotated.class.getAnnotation(Transactional.class);
		}
		return transactional;
	}
	/**
	 * Returns True if rollback DID NOT HAPPEN (i.e. if commit should continue).
	 *
	 * @param transactional The metadata annotation of the method
	 * @param e             The exception to test for rollback
	 * @param txn           A JPA Transaction to issue rollbacks on
	 */
	private boolean _rollbackIfNecessary(final Transactional transactional,final Exception e,final EntityTransaction txn) {
		// Changed to adapt code to jakarta.transaction.Transactional
		boolean doRollback = (e instanceof RuntimeException); // Default rule

		for (Class<? extends Exception> dontRollbackOn : transactional.dontRollbackOn()) {
			if (dontRollbackOn.isInstance(e)) {
				doRollback = false;
				break;
			}
		}

		for (Class<? extends Exception> rollbackOn : transactional.rollbackOn()) {
			if (rollbackOn.isInstance(e)) {
				doRollback = true;
				break;
			}
		}
		if (doRollback) {
			txn.rollback();
			return false;
		} else {
			return true;
		}
	}
}
