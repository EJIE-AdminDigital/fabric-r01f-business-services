package r01f.services.bootstrap.core.db.tx.spring;

import org.springframework.aop.Advisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

public interface CoreDBTransactionAdvisorHandler {
	
	public Advisor createFor(final TransactionInterceptor interceptor, 
                             final Class<?>... basePackageClasses);
}
