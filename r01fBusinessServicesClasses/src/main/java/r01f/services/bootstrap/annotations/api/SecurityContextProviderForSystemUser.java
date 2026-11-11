package r01f.services.bootstrap.annotations.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;
import r01f.model.security.context.SecurityContext;



/**
 * Sometimes a [client API] is needed but in the context of the [system user] 
 * (e.g. to load config data at the CORE-side)
 * The [client API] gets injected the {@link SecurityContext} provider and that injection can come from:
 * 		- The "normal" {@link SecurityContext} provider: the one that provides the [logged user] context
 * 		  which is usually on the thread local storage 
 * 		- The "system user" {@link SecurityContext} provider: the one that provides the [system user] context
 * 		  which is an "special" internal only {@link SecurityContext} provider 
 * 
 * So there can be two different [client API] injections: 
 * 		- one for the [logged user] 
 *		- another for the [system user]
 *
 * The [system user] [client API] is created by a binding like:
 * <pre class='brush:java'>
 *    @Provides					// provides a client api
 *    @ClientAPIForSystemUser	// for MASTER system usage
 *    @Singleton				// BEWARE!!!
 *    private MyAPI _provideMasterClientAPI(@SecurityContextProviderForSystemUser final Provider<SecurityContext> securityContextProvider,
 *    										    	 	  @ModelObjectsMarshaller final Marshaller modelObjectsMarshaller,
 *    										               @Named("api-app-code") final Map<Class,ServiceInterface> srvcIfaceMappings) {
 *    	// This creates an ad-hoc [client api] that uses a legitimated SecurityContext provider
 *    	return new MyApi(securityContextProvider,
 *    					 modelObjectsMarshaller,
 *    					 srvcIfaceMappings);
 *    }
 * </pre> 
 * Note that the FIRST parameter is a {@link SecurityContext} provider annotated with {@link SecurityContextProviderForSystemUser} 
 * so the [client API] will use the [system user] context (and NOT the "normal" SecurityContext provider for the [logged user] context)
 * 
 * So there must also exist a binding for the {@link SecurityContext} provider annotated with {@link SecurityContextProviderForSystemUser}
 * like this sample using Guice:
 * <pre class='brush:java'>
 *    public class MyCommonBootstrapGuiceModule
 *      implements Module {
 *      
 *       private final Provider<SecurityContext> _securityContextProvider;
 *       private final Provider<SecurityContext> _systemSecurityContextProvider;
 *      
 *	    public MyCommonBootstrapGuiceModule(final Provider<SecurityContext> securityContextProvider) {
 *		    super();
 *		    if (securityContextProvider == null) throw new IllegalArgumentException("The [security context] provider cannot be null!!");
 *		    _securityContextProvider = securityContextProvider;
 *		    _systemSecurityContextProvider = new DN00SecurityContextProviderForSystemUser();
 *	    }
 *      
 *       // This provides the [logged user] SecurityContext provider (usually from thread local storage)
 *       @Provides
 *       private SecurityContext _provideSecurityContext() {
 *       	return _securityContextProvider.get();
 *       }
 *       // This provides the [system user] SecurityContext provider (usually a special internal only SecurityContext provider)
 *       @Provides @SecurityContextProviderForSystemUser
 *       private SecurityContext _provideSystemSecurityContext() {
 *       	return _systemSecurityContextProvider.get();
 *       }
 *    }
 * </pre>
 */
@Qualifier //@BindingAnnotation			
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface SecurityContextProviderForSystemUser {
	/* nothing to do */
}
 