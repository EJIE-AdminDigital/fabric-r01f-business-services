package r01f.services.bootstrap.core.web.guice;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import jakarta.servlet.ServletContextEvent;
import lombok.extern.slf4j.Slf4j;
import r01f.services.bootstrap.guice.ServicesBootstrapGuiceUtil;

@Slf4j
public abstract class GuiceServletContextListenerBase
 			  extends GuiceServletContextListener {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Guice Injector
	 * Can be accessed by any servlet, filter or listener by calling <code>GuiceServletContextListenerBase.getInjector()</code>
	 * <code>
	 * 		Injector injector = (Injector)req.getServletContext()
        									 .getAttribute(Injector.class.getName());
       </code>
	 */
	protected Injector _injector;
/////////////////////////////////////////////////////////////////////////////////////////
//  Overridden methods of GuiceServletContextListener
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Injector getInjector() {
		if (_injector == null) {
			// [1] - Create guice modules
			Iterable<Module> guiceModules = _createGuiceModules();
			
			// [2] - Bootstrap
			Stopwatch sw = Stopwatch.createStarted();
			log.warn("\n\n\n\n");
			log.warn(".............................................................................");
			log.warn("[START] - BOOTSTRAPPING GUICE MODULES");
			log.warn(".............................................................................");
			
			_injector = Guice.createInjector(guiceModules);

			log.warn(".............................................................................");
			log.warn("[END] ({} millis) - BOOTSTRAPING GUICE MODULES",sw.elapsed(TimeUnit.MILLISECONDS));
			log.warn(".............................................................................");
		} else {
			log.warn("The Guice Injector is already created!!!");
		}
		return _injector;
	}
	/**
	 * Creates the guice modules to be used by the application
	 * @return
	 */
	protected abstract Iterable<Module> _createGuiceModules();
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		log.warn("=============================================");
		log.warn("Loading {} Servlet Context with {}...",
				 servletContextEvent.getServletContext().getContextPath(),
				 this.getClass().getSimpleName());
		log.warn("=============================================");

		super.contextInitialized(servletContextEvent);

		// Init JPA's Persistence Service, Lucene indexes and everything that has to be started
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesBootstrapGuiceUtil.startServices(_injector);
	}
	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		log.warn("=============================================");
		log.warn("DESTROYING {} Servlet Context with {} > closing search engine indexes if they are in use, release background jobs threads and so on...",
				 servletContextEvent.getServletContext().getContextPath(),
				 this.getClass().getSimpleName());
		log.warn("=============================================");

		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesBootstrapGuiceUtil.stopServices(_injector);

		// finalize
		super.contextDestroyed(servletContextEvent);

		log.warn("=============================================");
		log.warn("{} Servlet Context DESTROYED!!...",
				 servletContextEvent.getServletContext().getContextPath());
		log.warn("=============================================");
	}
}
