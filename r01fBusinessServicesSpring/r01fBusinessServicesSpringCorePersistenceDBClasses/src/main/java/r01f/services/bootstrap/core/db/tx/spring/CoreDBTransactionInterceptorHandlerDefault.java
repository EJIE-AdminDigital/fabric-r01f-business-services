package r01f.services.bootstrap.core.db.tx.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j

/**
 * This class acts as a dynamic factory that builds the "transactional engine" for each module :
 *   [1] Allowing Spring to automatically detect @Transactional annotations (Jakarta or Spring) 
 *   [2] Execute them using the specific database manager (TransactionManager) assigned to that module.
 *   
 *  -> This class must be used with and Advisor 
 *         [1] The Advisor say when to apply
 *         [2] The Interceptor how to apply 
 */
public  class CoreDBTransactionInterceptorHandlerDefault 
		implements CoreDBTransactionInterceptorHandler {
///////////////////////////////////////////////////////////////////////
/// CREATE
///////////////////////////////////////////////////////////////////////
	@Override
	public TransactionInterceptor createFrom(final PlatformTransactionManager tm, final String transactionTMbeanName) {	  	
	    // [1] Allows to read  @Transactional [Jakarta or Spring]
	    AnnotationTransactionAttributeSource txSource = new AnnotationTransactionAttributeSource();
	    // 
	    TransactionInterceptor interceptor = new TransactionInterceptor();	    
	    // [2] Attach the to the module [person, org] transaction manger bean name  
	    interceptor.setTransactionManager(tm);	    
	    //[4].
	    interceptor.setTransactionAttributeSource(txSource);
	    
	    //[5] Attach the to the module [person, org] transaction manager bean name  
	    interceptor.setTransactionManagerBeanName(transactionTMbeanName);	    
	    log.warn("########################################################################################################");
	    log.warn("##### [ (•‿•) Success! ] Dynamic Transaction Interceptor linked to Manager: '{}'", transactionTMbeanName);
	    log.warn("########################################################################################################");	    
	    return interceptor;
	}

}