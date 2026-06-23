package r01f.services.bootstrap.core.db.guice;

import com.google.inject.Module;

import r01f.services.core.HasServiceCoreSubModuleConfig;

/**
 * A marker interface for guice modules in charge of DB persistence bindings
 */
public interface DBPersistenceGuiceModule 
		 extends Module,
		 		 HasServiceCoreSubModuleConfig {
	// just a marker interface
}
