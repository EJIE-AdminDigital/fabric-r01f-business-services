package r01f.services.bootstrap.annotations.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;



/**
 * Use this way:
 *  @Bean @SecurityContextProviderForTest
 *	@Inject
 *	public SecurityContext _provideTestSecurityContext(final ServiceBootstrapSpringHandler servicesBootstrap) {
 *		SecurityContext outCtx = servicesBootstrap.getInjector()
 *												  .getProvider(Key.get(SecurityContext.class,
 *														  			   SecurityContextProviderForTest.class))
 *												  .get();
 *		return outCtx;
 *	}
 *	@Bean @SecurityContextProviderForTest
 *	@Inject
 *	public SecurityContext _provideTestSecurityContext(final ServiceBootstrapSpringHandler servicesBootstrap) {
 *		SecurityContext outCtx = servicesBootstrap.getInjector()
 *												  .getProvider(SecurityContext.class)
 *												  .get();
 *		return outCtx;
 *	}
 * </pre>
 */
@Qualifier //@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface SecurityContextProviderForTest {
	/* nothing to do */
}
