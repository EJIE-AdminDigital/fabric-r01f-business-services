package r01f.services.bootstrap.core.db.tx.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public  class CoreDBTransactionInterceptorHandlerDefaultLegacy 
		implements CoreDBTransactionInterceptorHandler {
///////////////////////////////////////////////////////////////////////
/// CREATE
///////////////////////////////////////////////////////////////////////
	@Override
	public TransactionInterceptor createFrom(final PlatformTransactionManager tm, final String transactionTMbeanName) {
	  
	    DefaultTransactionAttribute txAttr = new DefaultTransactionAttribute();
	    txAttr.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	    
	     //[0] Set the TM Bean Name !!!
	    txAttr.setQualifier(transactionTMbeanName); 

	    //[1] Set transactiona methods
	    NameMatchTransactionAttributeSource txSource = new NameMatchTransactionAttributeSource();
	    txSource.addTransactionalMethod("create*", txAttr);
	    txSource.addTransactionalMethod("update*", txAttr);
	    txSource.addTransactionalMethod("delete*", txAttr);
	    txSource.addTransactionalMethod("save*", txAttr);
	    txSource.addTransactionalMethod("touch*", txAttr);
	    
	    txSource.addTransactionalMethod("*", txAttr); // test*/

	    //[2] Build Interceptor
	    TransactionInterceptor interceptor = new TransactionInterceptor();
	    interceptor.setTransactionManager(tm);
	    interceptor.setTransactionAttributeSource(txSource);
	    interceptor.setTransactionManagerBeanName(transactionTMbeanName); // Transaction manager!	    
	   
	    log.warn( "########################################################################################################");
	    log.warn( "##### [ (•‿•) Warning! ] Transaction interceptor for  Transaction Manager named '{}'" , transactionTMbeanName);
	    log.warn( "#########################################################################################################");
	    return interceptor;
	}

	 
	
}