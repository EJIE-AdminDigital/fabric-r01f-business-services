package r01f.services.bootstrap.core.db.spring;

import java.lang.annotation.Annotation;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import r01f.services.core.persistence.db.DBEntity;
import r01f.services.core.persistence.db.annotations.DataSourceFor;
import r01f.services.core.persistence.db.annotations.EntityManagerFactoryFor;
import r01f.services.core.persistence.db.annotations.EntityManagerFor;
import r01f.services.core.persistence.db.annotations.TransactionInterceptorFor;
import r01f.services.core.persistence.db.annotations.TransactionManagerFor;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
public class CoreSpringBootstrapPersistenceModulePostProcessor 
	 extends CoreSpringBootstrapPersistenceConfigurationBase
  implements BeanDefinitionRegistryPostProcessor, EnvironmentAware,BeanFactoryAware {
/////////////////////////////////////////////////////////////////////////////////////////
// MEMBERS
/////////////////////////////////////////////////////////////////////////////////////////	
	protected ConfigurableEnvironment _env;
	protected BeanFactory _beanFactory;	
	protected Class<?>[] _servicesBase;
	protected String _fqn;
/////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////		
	protected CoreSpringBootstrapPersistenceModulePostProcessor(final CoreAppCode coreCode, final CoreModule coreModule,
																final Class<? extends DBEntity> entityBase,
																final Class<?>... servicesBase ) {
		super(coreModule, entityBase);	
		_servicesBase = servicesBase;
		_fqn = coreCode.asString() + "." + coreModule.asString();
	}
	
	protected CoreSpringBootstrapPersistenceModulePostProcessor(final CoreAppCode coreCode, final CoreModule coreModule,
																final Class<? extends DBEntity>[] entitiesBase,
																final Class<?>... servicesBase ) {
		super(coreModule, entitiesBase);	
		_servicesBase = servicesBase;
		_fqn  = coreCode.asString() + "." + coreModule.asString();
	}
	
	protected CoreSpringBootstrapPersistenceModulePostProcessor(final CoreAppCode coreCode, final CoreModule coreModule,
																final Class<?>... servicesBase ) {
		super(coreModule);	
		_servicesBase = servicesBase;
		_fqn  = coreCode.asString() + "." + coreModule.asString();
	}
///////////////////////////////////////////////////////////////////////////
/// PUBLIC METHODS TO IMPLEMENT
///////////////////////////////////////////////////////////////////////////   
	 @Override
	 public void setEnvironment(final Environment environment) {
	    _env = (ConfigurableEnvironment) environment;
	 }
	 
	 @Override
	 public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
	    _beanFactory = beanFactory;
	} 	    
    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
       // do nothing
    }    
    
    @Override
	public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {    	
    	if ( CollectionUtils.hasData(super.getEntitiesBase())) {
    		 // Common case: Requires to define datasource, entity, etc....    	
    		_postProcessBeanDefinitionRegistry(registry);
    	} else {    		
    		_postProcessBeanDefinitionRegistryWithoutInnerPersistence(registry);
    	}
    }
    
///////////////////////////////////////////////////////////////////////////
/// PROCESSOR [ Six Steps based definition Registry]
///////////////////////////////////////////////////////////////////////////
    protected void _postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
    	_postProcessDatasourceBeanDefinitionRegistry(registry);
    	_postProcessEntityManagerFactoryBeanDefinitionRegistry(registry);
    	_postProcessTransactionManagerFactoryBeanDefinitionRegistry(registry,false);
    	_postProcessEntityManagerBeanDefinitionRegistry(registry);
    	_postProcessTransactionInterceptorBeanDefinitionRegistry(registry);
    	_postProcessTransactionAdvisorBeanDefinitionRegistry(registry);
    }
///////////////////////////////////////////////////////////////////////////
/// PROCESSOR [ Three Steps based definition Registry] ( used for root or global transaction services without inner-persistence)
///////////////////////////////////////////////////////////////////////////  
    protected void _postProcessBeanDefinitionRegistryWithoutInnerPersistence(final BeanDefinitionRegistry registry) throws BeansException {   
    	_postProcessTransactionManagerFactoryBeanDefinitionRegistry(registry,true);    	
    	_postProcessTransactionInterceptorBeanDefinitionRegistry(registry);
    	_postProcessTransactionAdvisorBeanDefinitionRegistry(registry);
    }
    /**
     * [PROCESS STEP 1]: Datasource    
     * ---------------------------------
     */
    protected void _postProcessDatasourceBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        // --- [1.] Register Datasource
        BeanDefinition dsDef = BeanDefinitionBuilder.genericBeanDefinition(DataSource.class,
                                                                           () -> super.dataSource(_env))
        											.getBeanDefinition();
        _addQualifier(dsDef, DataSourceFor.class, _fqn);
        registry.registerBeanDefinition(super.getBeanNames().getDataSourceBeanName(), dsDef);
    }
    /**
     * [PROCESS STEP 2]:  Entity Manager Factory    
     * ------------------------------------------
     */    
    protected void _postProcessEntityManagerFactoryBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
    	 // --[2.] Register Entity Manager Factory
        BeanDefinition emfDef = BeanDefinitionBuilder.genericBeanDefinition(LocalContainerEntityManagerFactoryBean.class,
															                () -> {							               
															                    DataSource ds = _beanFactory.getBean(super.getBeanNames().getDataSourceBeanName(), DataSource.class);
															                    return super.entityManagerFactory(ds, _env);
															                })
        											 .getBeanDefinition();
        _addQualifier(emfDef, EntityManagerFactoryFor.class, _fqn);
        registry.registerBeanDefinition(super.getBeanNames().geEntityManagerFactoryBeanName(), emfDef);
    }
    /**
     * [PROCESS STEP 3]:  Transaction Manager    
     * ------------------------------------------
     */   
    protected void _postProcessTransactionManagerFactoryBeanDefinitionRegistry(final BeanDefinitionRegistry registry,final boolean isVirtual) throws BeansException {
    	  BeanDefinition tmDef;
    	  if ( !isVirtual ) {
    		  tmDef = BeanDefinitionBuilder.genericBeanDefinition(PlatformTransactionManager.class,
													              () -> {
													                    EntityManagerFactory emf = _beanFactory.getBean(super.getBeanNames().geEntityManagerFactoryBeanName(), EntityManagerFactory.class);
													                    return super.transactionManager(emf);
													              })
    				  					   .getBeanDefinition();
    	  } else {
    		  tmDef = BeanDefinitionBuilder.genericBeanDefinition(PlatformTransactionManager.class,
											                      () -> new org.springframework.transaction.support.AbstractPlatformTransactionManager() {
												                              private static final long serialVersionUID = 1L;
												                              @Override 
												                              protected Object doGetTransaction() { 
												                            	  return new Object(); 
												                              }
												                              @Override 
												                              protected void doBegin(final Object tx,final TransactionDefinition def) { 
												                            	  log.info(" > [{}] Virtual TX Started",_fqn); 
												                              }
												                              @Override 
												                              protected void doCommit(final org.springframework.transaction.support.DefaultTransactionStatus status) { 
												                            	  log.info(" > [{}] Virtual TX Committed",_fqn); 
												                              }
												                              @Override 
												                              protected void doRollback(final org.springframework.transaction.support.DefaultTransactionStatus status) { 
												                            	  log.info(" > [{}] Virtual TX Rolled Back",_fqn); 
												                              }
											                      		})
    				  						.getBeanDefinition();
    	  }    	  
		_addQualifier(tmDef,TransactionManagerFor.class,_fqn);
		registry.registerBeanDefinition(super.getBeanNames().geEntityTransactionManagerBeanName(),tmDef);
    }
    /**
     * [PROCESS STEP 4]:  Entity Manager    
     * ------------------------------------------
     */   
    protected void _postProcessEntityManagerBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
    	// -- [4] Entity Manager 							     	  
        BeanDefinition emDef = BeanDefinitionBuilder.genericBeanDefinition(EntityManager.class,
															               () -> {
															                    EntityManagerFactory emf = _beanFactory.getBean(super.getBeanNames().geEntityManagerFactoryBeanName(), EntityManagerFactory.class);								        	   
															                    return super.entityManager(emf); 
															               })
        											.getBeanDefinition();
		_addQualifier(emDef,EntityManagerFor.class,_fqn);
        registry.registerBeanDefinition(super.getBeanNames().geEntityManagerBeanName(),emDef);
    
    }
    /**
     * [PROCESS STEP 5]:  Transaction Interceptor    
     * ------------------------------------------
     */   
    protected void _postProcessTransactionInterceptorBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition interceptorDef = BeanDefinitionBuilder.genericBeanDefinition(TransactionInterceptor.class, 
																	                () -> {
																	                    PlatformTransactionManager tm = _beanFactory.getBean(super.getBeanNames().geEntityTransactionManagerBeanName(), PlatformTransactionManager.class);
																	                    return super.transactionsInterceptor(tm,
																	                    									 super.getBeanNames()
																	                    									 	  .geEntityTransactionManagerBeanName());
																	                })
        													 .getBeanDefinition();
		_addQualifier(interceptorDef, TransactionInterceptorFor.class, _fqn);
		registry.registerBeanDefinition(super.getBeanNames().geEntityTransactionManagerInterceptorBeanName(), interceptorDef);
    }
    /**
     * [PROCESS STEP 6]:  Advisor
     * ------------------------------------------
     */   
    protected void _postProcessTransactionAdvisorBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
    	  registry.registerBeanDefinition(super.getBeanNames()
    			  							   .geEntityTransactionManagerAdvisorBeanName(), 
						                  BeanDefinitionBuilder.genericBeanDefinition(Advisor.class,
						                  () -> {
						                	  TransactionInterceptor interceptor = _beanFactory.getBean(super.getBeanNames()
						                			  														 .geEntityTransactionManagerInterceptorBeanName(), 
						                			  													TransactionInterceptor.class);
						                	  return super.transactionsAdvisor(interceptor,
						                			  						   _servicesBase);
						                  })
				   .getBeanDefinition());
    }
///////////////////////////////////////////////////////////////////////////
/// PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////   
    @SuppressWarnings({ "static-method" })
	private void _addQualifier(final BeanDefinition bd,
							   final Class<? extends Annotation> annotationType,final String value) {
        ((AbstractBeanDefinition)bd).addQualifier(new AutowireCandidateQualifier(annotationType,value));
    }
    @SuppressWarnings({ "static-method", "unused" })
	private void _logBean(final BeanDefinitionRegistry registry, 
						  final String beanName,final String qualifierAlias) {
        BeanDefinition bd = registry.getBeanDefinition(beanName);
        String fqn = bd.getBeanClassName();       
        log.debug(String.format(" > BEAN ID: %-40s | QUALIFIER: @%-25s | TYPE: %s", 
                                beanName, qualifierAlias, fqn != null ? fqn : "DynamicProxy/Lambda"));
    }
}