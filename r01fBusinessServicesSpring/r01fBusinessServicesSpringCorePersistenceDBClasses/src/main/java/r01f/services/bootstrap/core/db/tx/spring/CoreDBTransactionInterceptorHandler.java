package r01f.services.bootstrap.core.db.tx.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

public interface CoreDBTransactionInterceptorHandler {
	
	TransactionInterceptor createFrom(final PlatformTransactionManager tm, final String beanName);

}
