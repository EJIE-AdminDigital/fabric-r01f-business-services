package r01f.services.bootstrap.core.db.guice;

import java.util.Properties;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
import com.google.inject.name.Names;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import r01f.config.ContainsConfigData;
import r01f.service.ServiceHandler;
import r01f.services.bootstrap.guice.ServicesBootstrapGuiceUtil;
import r01f.services.core.ServicesCoreSubModuleConfig;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.guice.PersistService;
import r01f.services.core.persistence.db.guice.jpa.JpaPersistModule;
import r01f.services.core.persistence.db.guice.jpa.JpaPersistOptions;

/**
 * Base type for DB guice modules
 */
@Slf4j
public abstract class DBPersistenceGuiceModuleBase
     	   implements DBPersistenceGuiceModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Encapsulates the db config
	 */
	private final DBModuleConfig _dbConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBPersistenceGuiceModuleBase(final DBModuleConfig config) {
		_dbConfig = config;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	HasServiceCoreSubModuleConfig
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ServicesCoreSubModuleConfig<? extends ContainsConfigData> getSubModuleConfig() {
		return ServicesCoreSubModuleConfig.createForDBPersistenceSubModule(_dbConfig);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GUICE MODULE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		Binder theBinder = binder;

		// bind the config
		binder.bind(DBModuleConfig.class)
			  .toInstance(_dbConfig);

		// Load properties
		Properties props = _dbConfig.asProperties();

		// Create the module
		//// [ GUICE Persist6 ] JPA Persist Options https://github.com/google/guice/commit/e68fab652406b0d85bf294afe8cb9930ac9e6d42
		JpaPersistOptions jpaPersistOptions = JpaPersistOptions.builder()
															   .setAutoBeginWorkOnEntityManagerCreation(true)
															   .build();
		JpaPersistModule jpaModule = new JpaPersistModule(_dbConfig.getAppCode(),_dbConfig.getAppModule(),_dbConfig.getJPAPersistenceUnitName(),
														  jpaPersistOptions);	// for an alternative way see http://stackoverflow.com/questions/18101488/does-guice-persist-provide-transaction-scoped-or-application-managed-entitymanag

		jpaModule.properties(props);
		theBinder.install(jpaModule);


		// Service handler used to control (start/stop) the Persistence Service (see ServletContextListenerBase)
		// do NO forget!!
		ServicesBootstrapGuiceUtil.bindServiceHandler(theBinder,
												 	  JPAPersistenceServiceControl.class,_dbConfig.getJPAServiceName());
		// this is IMPORTANT (and cannot be moved to ServicesBootstrapUtil.bindServiceHandler)
		if (binder instanceof PrivateBinder privateBinder) {
			privateBinder.expose(Key.get(ServiceHandler.class,
										 Names.named(_dbConfig.getJPAServiceName())));	// expose the binding
		}

		log.warn("... binded jpa persistence unit {} whose entity manager is handled by ServiceHandler with name {}",
				 _dbConfig.getJPAPersistenceUnitName(),_dbConfig.getJPAServiceName());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PersistenceService control
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree
	 * The {@link ServiceHandler} interface is used to start & stop the JPA's PersistenceService
	 * at ServletContextListenerBase type
	 */
	static class JPAPersistenceServiceControl
	  implements ServiceHandler {
		private final PersistService _persistService;

		@Inject
		public JPAPersistenceServiceControl(final PersistService service) {
			_persistService = service;

		}
		@Override
		public void start() {
			if (_persistService == null) throw new IllegalStateException("NO persistence service available!");
			log.warn("######################################################################################");
			log.warn("Starting PersistService");
			log.warn("######################################################################################");
			try {
				_persistService.start();
			} catch (final Throwable th) {
				th.printStackTrace();
				throw new IllegalStateException(th.getLocalizedMessage());
			}
			log.warn("STARTED!!#############################################################################");
		}
		@Override
		public void stop() {
			if (_persistService == null) throw new IllegalStateException("NO persistence service available!");
			log.warn("######################################################################################");
			log.warn("Stopping PersistService");
			log.warn("######################################################################################");
			try {
				_persistService.stop();
			} catch (Throwable th) {/* just in the case where PersistenceService were NOT started */ }
			log.warn("STOPPED!!#############################################################################");
		}
	}
}
