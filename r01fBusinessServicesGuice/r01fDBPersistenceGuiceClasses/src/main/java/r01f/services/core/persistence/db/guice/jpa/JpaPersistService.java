package r01f.services.core.persistence.db.guice.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import r01f.services.core.persistence.db.guice.PersistService;
import r01f.services.core.persistence.db.guice.UnitOfWork;

@Singleton
    class JpaPersistService 
implements Provider<EntityManager>, 
		   UnitOfWork, 
		   PersistService {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
  	private final String _persistenceUnitName;
  	private final JpaPersistOptions _options;
  	private final Map<?,?> _persistenceProperties;
  	
  	private volatile EntityManagerFactory _entityManagerFactory;

  	private final ThreadLocal<EntityManager> _entityManager = new ThreadLocal<>();
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
  	@Inject
  	public JpaPersistService(@Jpa final String persistenceUnitName,
  							 @Jpa final JpaPersistOptions options,
      					   	 @Nullable @Jpa final Map<?,?> persistenceProperties) {
    	_persistenceUnitName = persistenceUnitName;
    	_options = options;
    	_persistenceProperties = persistenceProperties;
  	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
  	@Override
  	public EntityManager get() {
    	if (_options.getAutoBeginWorkOnEntityManagerCreation() 
    	 && !this.isWorking()) {
      		this.begin();
    	}

    	EntityManager outEntityManager = _entityManager.get();
    	Preconditions.checkState(outEntityManager != null,
        						 "Requested EntityManager outside work unit. " + 
    							 "As of Guice 6.0, Guice Persist doesn't automatically begin the unit of work when provisioning an EntityManager. " + 
        					     "To preserve the legacy behavior, construct the `JpaPersistModule` with a " +
    							 	"`JpaPersistOptions.builder()" +
        					     				      ".setAutoBeginWorkOnEntityManagerCreation(true)" +
    							 					  ".build()`. " +
            					 "Alternately, try calling UnitOfWork.begin() first, or use a PersistFilter if you are inside a servlet environment.");
    	return outEntityManager;
  	}

  	public boolean isWorking() {
    	return _entityManager.get() != null;
  	}
  	@Override
  	public void begin() {
    	Preconditions.checkState(_entityManager.get() == null,
        						 "Work already begun on this thread. Looks like you have called UnitOfWork.begin() twice " +
            					 "without a balancing call to end() in between.");

    	_entityManager.set(_entityManagerFactory.createEntityManager());
  	}
  	@Override
  	public void end() {
    	EntityManager em = _entityManager.get();

    	// Let's not penalize users for calling end() multiple times.
    	if (null == em) return;

	    try {
    		em.close();
    	} finally {
      		_entityManager.remove();
    	}
  	}
  	@VisibleForTesting
  	synchronized void start(final EntityManagerFactory emFactory) {
    	_entityManagerFactory = emFactory;
  	}
  	@Override
  	public synchronized void start() {
    	if (null != _entityManagerFactory) return;

    	if (null != _persistenceProperties) {
     		_entityManagerFactory = Persistence.createEntityManagerFactory(_persistenceUnitName,
     																	   _persistenceProperties);
    	} else {
      		_entityManagerFactory = Persistence.createEntityManagerFactory(_persistenceUnitName);
    	}
  	}
  	@Override
  	public synchronized void stop() {
    	if (null != _entityManagerFactory && _entityManagerFactory.isOpen()) {
      		_entityManagerFactory.close();
    	}
  	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////  	
  	@Singleton
  	public static class EntityManagerFactoryProvider 
  			 implements Provider<EntityManagerFactory> {
    	private final JpaPersistService _entityManagerProvider;

	    @Inject
	    public EntityManagerFactoryProvider(final JpaPersistService emProvider) {
	      _entityManagerProvider = emProvider;
	    }
	
	    @Override
	    public EntityManagerFactory get() {
	      assert _entityManagerProvider._entityManagerFactory != null;
	      return _entityManagerProvider._entityManagerFactory;
	    }
  	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
  	@Documented
  	@Retention(RetentionPolicy.RUNTIME)
  	@Target(ElementType.PARAMETER)
  	private @interface Nullable {
  		// nothing
  	}
}
