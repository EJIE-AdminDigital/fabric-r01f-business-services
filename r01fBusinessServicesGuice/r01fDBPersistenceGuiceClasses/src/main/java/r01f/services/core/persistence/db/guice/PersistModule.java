package r01f.services.core.persistence.db.guice;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.AbstractModule;
import com.google.inject.internal.InternalFlags;
import com.google.inject.matcher.Matcher;

import jakarta.transaction.Transactional;

/**
 * Install this module to add guice-persist library support for JPA persistence providers.
 */
public abstract class PersistModule 
			  extends AbstractModule {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
  	private static final Matcher<Method> NOT_OBJECT_METHOD = new Matcher<Method>() {
												        			@Override
												        			public boolean matches(final Method m) {
												          				return !Object.class.equals(m.getDeclaringClass());
												        		}
  																};
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected final void configure() {
      	_configurePersistence();
    
      	requireBinding(PersistService.class);
      	requireBinding(UnitOfWork.class);
      	if (InternalFlags.isBytecodeGenEnabled()) {
        	// class-level @Transacational
        	this.bindInterceptor(annotatedWith(Transactional.class),NOT_OBJECT_METHOD,_getTransactionInterceptor());
	        // method-level @Transacational
        	this.bindInterceptor(any(),annotatedWith(Transactional.class),_getTransactionInterceptor());
      	}
    }

  	protected abstract void _configurePersistence();

  	protected abstract MethodInterceptor _getTransactionInterceptor();
}
