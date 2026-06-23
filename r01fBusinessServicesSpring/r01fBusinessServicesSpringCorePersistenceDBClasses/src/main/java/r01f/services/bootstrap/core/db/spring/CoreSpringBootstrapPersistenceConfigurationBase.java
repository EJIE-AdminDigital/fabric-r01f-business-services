package r01f.services.bootstrap.core.db.spring;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import r01f.services.bootstrap.core.db.tx.spring.CoreDBTransactionAdvisorHandler;
import r01f.services.bootstrap.core.db.tx.spring.CoreDBTransactionAdvisorHandlerDefault;
import r01f.services.bootstrap.core.db.tx.spring.CoreDBTransactionInterceptorHandler;
import r01f.services.bootstrap.core.db.tx.spring.CoreDBTransactionInterceptorHandlerDefault;
import r01f.services.core.persistence.db.DBEntity;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.spring.IsSpringConfiguration;

/**
 *  Based on this class there a two options :
 * 
 *  Opttion  [1]: The "Dynamic Grafting" [ BeanPostProcessor]
    ###########################################################
    
    
     
		This approach is a surgical intervention in the Spring lifecycle. You are essentially letting Spring discover the beans first, and then you "hijack" their metadata before they are fully born (instantiated).
		
		How it works: Your CoreSpringBootstrapPersistenceModulePostProcessor scans all registered BeanDefinitions. When it finds a service (e.g., DN01PersonCoreServiceImpl), it programmatically adds a Qualifier to its metadata pointing to the correct TransactionManager.
		
		The Pros: * Zero-Touch Services: Your business logic remains "pure." You don't need to add @Transactional("personaTM") to every method.
		
		Scalability: If you add 10 new services to the "Persona" module, the PostProcessor automatically "shields" them without extra configuration.
		
		The Cons: * Hidden Magic: It makes the application harder to debug because the transactional behavior isn't visible in the source code; it happens "under the hood."
		
		Ordering Issues: You must ensure the PostProcessor runs at the exact right moment in the Spring Lifecycle.
		
    
      [ Just extend  CoreSpringBootstrapPersistenceModulePostProcessor ]
      
      					@Component
						public class DN01PersonSpringBootstrapPersistenceConfigurationForPersona 
								extends CoreSpringBootstrapPersistenceModulePostProcessor {
						/////////////////////////////////////////////////////////////////////////////////////////
						//
						/////////////////////////////////////////////////////////////////////////////////////////
							protected DN01PersonSpringBootstrapPersistenceConfigurationForOrgConfigGenerarator() {
								super(CoreModule.forId("persona"), 
									  DN01DBEntityPersonImpl.class ,
									  DN01PersonCoreServiceImpl.class);		
							}

					}


    Option  [2]: The "Explicit Wiring" [ Manual @Configuration ]
   ##############################################################

	This is the Architectural Fortress approach. You are not leaving anything to Spring's "best guess." You explicitly build the infrastructure for each module.
	
	How it works: 1.  You define unique Beans for
	                   [1] DataSource, EntityManagerFactory, and PlatformTransactionManager.
	2.  The "Kill Switch" for Ambiguity: By creating a specific TransactionInterceptor and calling interceptor.setTransactionManager(tm), you bypass Spring's default search mechanism.
	3.  You bind that Interceptor to a specific Advisor that only targets your module's service classes. 
   [ Just extend  CoreSpringBootstrapPersistenceModulePostProcessor ]
			 *  @Override	
			    @CoreDatasource(DN01CoreDBPersonaConstants.DS)
			    public DataSource dataSource(final ConfigurableEnvironment env) {
			        return createDataSource(env); 
			    }
			
			    @Override
				@CoreEntityManagerFactory(DN01CoreDBPersonaConstants.EMF)
			    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final  @CoreDatasource(DN01CoreDBPersonaConstants.DS) DataSource ds, 
			                                                                                                                   ConfigurableEnvironment env) {
			        
			        return createFactory(ds,
			        		             createJpaProperties(env), 
			        		             createEntityManagerFactoryBuilder(), 
			                             DN01DBEntityPersonImpl.class, "PersonaPU");
			    }
			
			 * 
 */

public abstract class CoreSpringBootstrapPersistenceConfigurationBase 
		extends CoreSpringBootstrapPersistenceConfigurationAbstract 
	implements CoreSpringBootstrapPersistenceConfiguration,
			   IsSpringConfiguration {		
/////////////////////////////////////////////////////////////////////////////////////////
// MEMEBERS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final CoreDBTransactionAdvisorHandler     _transationAdvisorHandler;
	protected final CoreDBTransactionInterceptorHandler _transactionInterceptorHandler;
/////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CoreSpringBootstrapPersistenceConfigurationBase(final CoreModule configPrefix, 
			                                               final Class<? extends DBEntity> entityBase,	
			                                               final CoreDBTransactionAdvisorHandler transationAdvisorHandler, final CoreDBTransactionInterceptorHandler transactionInterceptorHandler) {
		super(configPrefix,entityBase);
		_transationAdvisorHandler  = transationAdvisorHandler;
		_transactionInterceptorHandler  = transactionInterceptorHandler;
	}
	protected CoreSpringBootstrapPersistenceConfigurationBase(final CoreModule configPrefix, 
                                                              final Class<? extends DBEntity> entityBase) {
	  this(configPrefix,
		   entityBase,
		   new CoreDBTransactionAdvisorHandlerDefault(), new CoreDBTransactionInterceptorHandlerDefault() );
	}
/////////////////////////////////////////////////////////////////////////////////////////
// METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	protected JpaProperties _getProps(final ConfigurableEnvironment env) {
		return createJpaProperties(env);
	}
	protected EntityManagerFactoryBuilder _getBuilder() {
		return createEntityManagerFactoryBuilder();
	}
/////////////////////////////////////////////////////////////////////////////////////////
// METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public DataSource dataSource(final ConfigurableEnvironment env) {
		return createDataSource(env); 
	}

	@Override
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource ds, final ConfigurableEnvironment env) {
		return super.createEntityManagerFactory(ds,
								                createJpaProperties(env), 
								                createEntityManagerFactoryBuilder());
	}
	@Override
	public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {		
		 return new JpaTransactionManager(emf);
	}

	@Override
	public EntityManager entityManager( final EntityManagerFactory emf) {
    	/**
    	 * The SharedEntityManagerCreator provides a thread-safe proxy that dynamically delegates operations ..
    	 * ... to the specific EntityManager associated with each module's unique PersistenceUnit. 
    	 *  This architecture ensures that each module maintains strict isolation of its data sources while allowing Spring to automatically synchronize the persistence context with the correct TransactionManager for every request.
    	 */
    	return SharedEntityManagerCreator.createSharedEntityManager(emf);
    }
	
	@Override 
    public TransactionInterceptor transactionsInterceptor( final PlatformTransactionManager tm, final String beanName) {     
	    return _transactionInterceptorHandler.createFrom(tm,  beanName);
	}

	   
    @Override
    public Advisor transactionsAdvisor( final TransactionInterceptor interceptor,  final Class<?>... baseServicePackageClasses) {        
        return _transationAdvisorHandler.createFor(interceptor,baseServicePackageClasses );        		
    } 
	
}