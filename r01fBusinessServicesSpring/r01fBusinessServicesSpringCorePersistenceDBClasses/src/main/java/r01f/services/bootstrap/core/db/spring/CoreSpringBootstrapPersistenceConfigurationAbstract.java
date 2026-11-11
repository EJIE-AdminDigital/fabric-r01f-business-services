package r01f.services.bootstrap.core.db.spring;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.reflection.ReflectionUtils;
import r01f.services.core.persistence.db.DBCoreBaseConstants;
import r01f.services.core.persistence.db.DBEntity;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.util.types.Strings;


/**
 * Based on  this yaml sample..
 * db:
  	persona:
	    datasource:
	      url: jdbc:mysql://localhost:33306/dena?useUnicode=true&serverTimezone=UTC
	      username: xxx
	      password: xxx
	      driver-class-name: com.mysql.cj.jdbc.Driver
	    jpa:
	      show-sql: true    
	      properties:
	        hibernate:
	          dialect: org.hibernate.dialect.MySQLDialect
	          format_sql: true
	          highlight_sql: true
	          hbm2ddl.auto: update
 */

@Slf4j
@Accessors(prefix="_")
public abstract class CoreSpringBootstrapPersistenceConfigurationAbstract 
		implements CoreSpringBootstrapPersistenceConfiguration {	
/////////////////////////////////////////////////////////////////////////////////////////
//  MEMBERS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final CoreModule _coreModule;	
	@Getter protected final Class<? extends DBEntity>[] _entitiesBase;
	@Getter protected final CoreSpringDBComponentesNames _beanNames;
   
    final String _dbPrefix; // f.e  db + "persona" (CoreModule)    
/////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORs
/////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	protected CoreSpringBootstrapPersistenceConfigurationAbstract(final CoreModule coreModule,
    													          final  Class<? extends DBEntity> ... entitiesBase) {
		_coreModule = coreModule;
		_entitiesBase = entitiesBase; //(Class<? extends DBEntity>[]) new Class<?>[] { entityBase };
	    _dbPrefix =   Strings.customized("{}.{}",
	    										DBCoreBaseConstants.DB_PROPS_PREFIX,coreModule.asString());
	    _beanNames = new CoreSpringDBComponentesNames(_coreModule);
	            
	    
	}
/////////////////////////////////////////////////////////////////////////////////////////
// METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Create JpaProperties based on JpaProperties bindable properties
     */
    protected JpaProperties createJpaProperties(final ConfigurableEnvironment env) {    
        JpaProperties props = new JpaProperties();
        Binder.get(env)
        	  .bind(_dbPrefix + ".jpa", Bindable.ofInstance(props));
        
     // 2. [V4 FORCE] If 'database-platform' is defined in YAML, we force 'hibernate.dialect' 
     //    to match it exactly. This prevents Spring Boot 4's auto-configuration [¿? ] ]from 
     //    injecting the default 'MySQLDialect' and ensures our custom Fabric dialect   //    takes precedence.
        if (props.getDatabasePlatform() != null) {
            props.getProperties().put("hibernate.dialect", props.getDatabasePlatform());
            log.warn("[Spring V4] Fabric Dialect Overwrite: Using {} for Hibernate", props.getDatabasePlatform());
        }       
      
        return props;
    }    
  
    /**
     * Create DataSource based on HikariConfig bindable properties.
     */
    protected DataSource createDataSource(final ConfigurableEnvironment env) {      
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();      
        
        //[] Attempt to bind properties to the config object
        Binder.get(env).bind(_dbPrefix + ".datasource", Bindable.ofInstance(config));
     
        // [Validation ]: Ensure the JDBC URL is defined
        if (config.getJdbcUrl() == null) {
            String url = env.getProperty(_dbPrefix + ".datasource.url");            
            if (url == null 
            		|| url.trim().isEmpty()) {
            	log.error("\n #########################################################################\""
            			+ "\n[ DB Configuration Error !!!]"
            			+ "\n #########################################################################"
            			+ "\n  Not defined db-componentes for CoreModule '{}' , just review : , "
            			+ "  \n [1] Include to import db-section or component at application.yml  fe. - classpath:config/default/{}-database.yml"
            			+ "  \n [2] DB component  section at file db:\r\n"
            			+ "		 {}:\r\n"
            			+ "    datasource:", _coreModule, _coreModule,_coreModule,_coreModule);
            			
                throw new IllegalStateException(
                			Strings.customized("DataSource configuration error: Property '{}.datasource.url' is missing or undefined", _dbPrefix)
                );
            }            
            config.setJdbcUrl(url);
        }        
        return new com.zaxxer.hikari.HikariDataSource(config);
    }
 
    /**
     * Create EntyManageFactory Builder. This is compatible with Spring 4
     * [ Previous version : 
	     * protected EntityManagerFactoryBuilder createEntityManagerFactoryBuilder() {
	        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    	}
     * 
     */
    protected EntityManagerFactoryBuilder createEntityManagerFactoryBuilder() {    
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter() {
            @Override
            public Map<String, Object> getJpaPropertyMap(jakarta.persistence.spi.PersistenceUnitInfo pui) {
                return super.getJpaPropertyMap(pui);
            }
        };        
        return new EntityManagerFactoryBuilder(vendorAdapter, (ds) -> new HashMap<>(), null);
    }

    /**
     * Create LocalContainerEntityManagerFactoryBean based on entityPackageClass
     */
   /* @SuppressWarnings("static-method")
	protected LocalContainerEntityManagerFactoryBean createFactory( final DataSource ds, 
														            final JpaProperties props, 
														            final EntityManagerFactoryBuilder builder, 
														            final Class<?> entityPackageClass, 
														            final String puName) {
    
        return builder
                .dataSource(ds)
                .packages(entityPackageClass)
                .persistenceUnit(puName)
                .properties(props.getProperties())
                .mappingResources(new String[0]) // Evita el error de persistence.xml
                .build();
    }*/
    
    
    /**
     * Create LocalContainerEntityManagerFactoryBean based on entityPackageClass array
     */
   /* @SuppressWarnings("static-method")
	protected LocalContainerEntityManagerFactoryBean createFactory( final DataSource ds, 
														            final JpaProperties props, 
														            final EntityManagerFactoryBuilder builder, 
														            final String puName,
														            final String... entityPackageClass
														            ) {
    	log.debug("#############################################################################");
    	log.debug(" createFactory, properties: {}", props.getProperties().values());
    	log.debug("#############################################################################");
		return builder
					.dataSource(ds)
						.packages(entityPackageClass)
						.persistenceUnit(puName)
						.properties(props.getProperties())
						.mappingResources(new String[0]) 
					.build();
	}*/
    
    /**
     * Create LocalContainerEntityManagerFactoryBean based on constructor passed entityPackageClass
     */
    protected LocalContainerEntityManagerFactoryBean createEntityManagerFactory( final DataSource ds, 
																	             final JpaProperties jpaProps, 
																	             final EntityManagerFactoryBuilder builder) {
    	log.warn("#############################################################################");
    	log.warn(" createFactory, properties: {}", jpaProps.getProperties().values());
    	log.warn("#############################################################################");
    	Map<String, Object> jpaProperties = new HashMap<>(jpaProps.getProperties());
    	// 1. Set persistence unit name 	
		String  persitenceUnitName = Strings.customized("{}.{}",_coreModule.asString(),"persistenceUnit"); 
		
		// 2. Dynamically instantiate the PhysicalNamingStrategy if configured
	    Object customNamingStrategy = _createPhysicalNamingStrategyIfRequired(jpaProperties);
	    if (customNamingStrategy != null) {
	        jpaProperties.put("hibernate.physical_naming_strategy", customNamingStrategy);	      
	    }
		return builder
					.dataSource(ds)
						.packages(_entitiesBase)
						.persistenceUnit(persitenceUnitName)
						.properties(jpaProperties)
						.mappingResources( new String[0]) 
					.build();
		}
  
/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////////////// 
    /**
     * Evaluates JPA properties and, if a physical naming strategy is defined as a String (FQDN),
     * attempts to dynamically instantiate it. If the target class provides a constructor accepting
     * a String argument, the 'db.tables.prefix' property value will be injected into it.
     *
     * @param jpaProperties Map of properties returned by JpaProperties
     * @return The instantiated PhysicalNamingStrategy object, or null if not required or instantiation failed
     */
    /**
     * Evaluates JPA properties and, if a physical naming strategy is defined as a String (FQDN),
     * attempts to dynamically instantiate it. If the target class provides a constructor accepting
     * a String argument, the 'db.tables.prefix' property value will be injected into it.
     *
     * @param jpaProperties Map of properties returned by JpaProperties
     * @return The instantiated PhysicalNamingStrategy object, or null if not required or instantiation failed
     */
    @SuppressWarnings("static-method")
	private Object _createPhysicalNamingStrategyIfRequired(Map<String, Object> jpaProperties ) {
    	
        if (jpaProperties == null || jpaProperties.isEmpty()) {
            return null;
        }

        // 1. Extract the physical naming strategy class name (supporting both property keys)
        String strategyClassName = (String) jpaProperties.get("hibernate.physical_naming_strategy");
        if (strategyClassName == null) {
            strategyClassName = (String) jpaProperties.get("physical_naming_strategy");
        }

        // Return null immediately if no strategy FQDN is defined or if it's blank
        if (strategyClassName == null
        		|| strategyClassName.trim().isEmpty()) {
            return null;
        }

        // 2. Extract the table prefix property (supporting both property keys)
        String prefix = (String) jpaProperties.get("db.tables.prefix");
        if (prefix == null) {
            prefix = (String) jpaProperties.get("hibernate.db.tables.prefix");
        }
        String safePrefix = (prefix != null) ? prefix : "";
        // 3. Dynamically load the class and instantiate it via ReflectionUtils
        try {
            Class<?> clazz = Class.forName(strategyClassName.trim());
            try {
                // Attempt to instantiate using the String-arg constructor (prefix)
                Class<?>[] paramTypes = new Class<?>[] { String.class };
                Object[] args = new Object[] { safePrefix };

                Object instance = ReflectionUtils.createInstanceOf(clazz, paramTypes, args);
                log.warn("PhysicalNamingStrategy instantiated dynamically with prefix '{}': {}", safePrefix, strategyClassName);
                return instance;

            } catch (final Throwable e) {
                // Fallback: If no String constructor exists, use the default no-arg constructor
                Object instance = ReflectionUtils.createInstanceOf(clazz, new Class<?>[]{}, new Object[]{});
                log.warn("PhysicalNamingStrategy instantiated dynamically using default constructor: {}", strategyClassName);
                return instance;
            }

        } catch (Throwable e) {
            log.error("Failed to dynamically instantiate PhysicalNamingStrategy: {}", strategyClassName, e);
            return null;
        }
    }
    @SuppressWarnings("unused")
	private void _debugShowAllProperties(final ConfigurableEnvironment env) {
        log.warn("#############################################################################");
        log.warn("[DEBUG] INSPECTING PROPERTIES FOR PREFIX: {}", _dbPrefix);
        
       
        for (PropertySource<?> source : env.getPropertySources()) {
            if (source instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerableSource = (EnumerablePropertySource<?>) source;
                for (String name : enumerableSource.getPropertyNames()) {
                    if (name.startsWith(_dbPrefix)) {
                        log.warn("[PROPERTY] {} = {}", name, env.getProperty(name));
                    }
                }
            }
        }
        log.warn("#############################################################################");
    }
    
}
