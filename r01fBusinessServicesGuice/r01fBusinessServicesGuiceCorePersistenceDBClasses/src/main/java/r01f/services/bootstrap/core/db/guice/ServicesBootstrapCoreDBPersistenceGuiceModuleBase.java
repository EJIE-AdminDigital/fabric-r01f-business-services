package r01f.services.bootstrap.core.db.guice;

import java.util.Arrays;
import java.util.Collection;

import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import r01f.services.bootstrap.core.guice.ServicesBootstrapCoreGuiceModuleBase;
import r01f.services.core.events.crud.CRUDErrorEventListener;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;

/**
 * Mappings internal to services core implementation
 */
@Slf4j
@EqualsAndHashCode(callSuper=true)				// This is important for guice modules
public abstract class ServicesBootstrapCoreDBPersistenceGuiceModuleBase
              extends ServicesBootstrapCoreGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final Module... otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBPersistenceGuiceModule dbGuiceModule) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  dbGuiceModule,
			  null);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBPersistenceGuiceModule dbGuiceModule,
														     final Module... otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  dbGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBPersistenceGuiceModule dbGuiceModule,
														     final Collection<Module> otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  dbGuiceModule,
			  otherModules);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBPersistenceGuiceModule dbGuiceModule,
														     final DBSearchPersistenceGuiceModule searchGuiceModule,
														     final Collection<Module> otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  dbGuiceModule,
			  searchGuiceModule,
			  otherModules);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBPersistenceGuiceModule dbGuiceModule,
														     final DBSearchPersistenceGuiceModule searchGuiceModule,
														     final Module... otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  dbGuiceModule,
			  searchGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBSearchPersistenceGuiceModule searchGuiceModule,
														     final Module... otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  searchGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public ServicesBootstrapCoreDBPersistenceGuiceModuleBase(final CoreAppCode coreAppCode,final CoreModule coreModule,
														     final DBSearchPersistenceGuiceModule searchGuiceModule,
														     final Collection<Module> otherModules) {
		super(coreAppCode,coreModule,
			  true,	// isolated,
			  searchGuiceModule,
			  otherModules);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		super.configure(binder);	// this is where all sub-modules are installed!!

		Binder theBinder = binder;
		
		// bind the [event listeners]
		_bindEventListeners(theBinder);
		
		// bind more [event listeners]
		theBinder.bind(CRUDErrorEventListener.class)
			  	 .toInstance(new CRUDErrorEventListener());		// CRUDOperationNOKEvent for EVERY model object
	}
}
