package r01f.services.core.persistence.db.guice;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * Apply this filter to enable the HTTP Request unit of work and to have
 * guice-persist manage the lifecycle of active units of work. The filter
 * automatically starts and stops the relevant {@link PersistService} upon
 * {@link jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)} and
 * {@link jakarta.servlet.Filter#destroy()} respectively.
 *
 * <p>
 * To be able to use the open session-in-view pattern (i.e. work per request),
 * register this filter <b>once</b> in your Guice {@code ServletModule}. It is
 * important that you register this filter before any other filter.
 *
 * <p>
 * For multiple providers, you should register this filter once per provider,
 * inside a private module for each persist module installed (this must be the
 * same private module where the specific persist module is itself installed).
 *
 * <p>
 * Example configuration:
 *
 * <pre>{@code
 * public class MyModule extends ServletModule {
 * 	public void configureServlets() {
 * 		filter("/*").through(PersistFilter.class);
 *
 * 		serve("/index.html").with(MyHtmlServlet.class);
 * 		// Etc.
 * 	}
 * }
 * }</pre>
 *
 * <p>
 * This filter is thread safe and allows you to create injectors concurrently
 * and deploy multiple guice-persist modules within the same injector, or even
 * multiple injectors with persist modules withing the same JVM or web app.
 *
 * <p>
 * This filter requires the Guice Servlet extension.
 */
@Singleton
public final class PersistFilter 
		implements Filter {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private final UnitOfWork _unitOfWork;
	private final PersistService _persistService;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public PersistFilter(final UnitOfWork unitOfWork,final PersistService persistService) {
		_unitOfWork = unitOfWork;
		_persistService = persistService;
	}
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		_persistService.start();
	}
	@Override
	public void destroy() {
		_persistService.stop();
	}
	@Override
	public void doFilter(final ServletRequest servletRequest,final ServletResponse servletResponse,
						 final FilterChain filterChain) throws IOException, ServletException {

		_unitOfWork.begin();
		try {
			filterChain.doFilter(servletRequest,servletResponse);
		} finally {
			_unitOfWork.end();
		}
	}
}
