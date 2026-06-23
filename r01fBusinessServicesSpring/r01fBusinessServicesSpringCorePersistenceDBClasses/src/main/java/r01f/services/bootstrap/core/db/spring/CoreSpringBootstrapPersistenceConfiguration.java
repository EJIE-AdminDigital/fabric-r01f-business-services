package r01f.services.bootstrap.core.db.spring;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import r01f.spring.IsSpringConfiguration;

public interface CoreSpringBootstrapPersistenceConfiguration 
	extends  IsSpringConfiguration {	
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
    public DataSource dataSource(final ConfigurableEnvironment env);
    
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource ds, final ConfigurableEnvironment env);
    
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf);
    
    public EntityManager entityManager(final EntityManagerFactory emf);    
 
    public TransactionInterceptor transactionsInterceptor( final PlatformTransactionManager tm, final String beanName); 
	   
    public Advisor transactionsAdvisor( final TransactionInterceptor interceptor,  final Class<?>... basePackageClasses);
}