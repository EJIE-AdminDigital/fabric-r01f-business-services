package r01f.services.api;

import java.util.Map;

import jakarta.inject.Provider;
import lombok.experimental.Accessors;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.services.bootstrap.annotations.api.SecurityContextProviderForSystemUser;
import r01f.services.interfaces.ServiceInterface;
import r01f.types.AppVersion;



/**
 * Base type for every API implementation 
 * 
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
@Accessors(prefix="_")
public abstract class ClientAPIImplBase 
           implements ClientAPI {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR INJECTED
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The api version
	 */
	protected final AppVersion _version;
	/**
	 * Model objects marshaller
	 */
	protected final Marshaller _modelObjectsMarshaller;
	/**
	 * securityContext 
	 */
	protected final Provider<SecurityContext> _securityContextProvider;
	/**
	 * A guice's Map binder that provides a {@link ServiceInterface}'s core impl or proxy to the core impl
	 */
	@SuppressWarnings("rawtypes")
	protected final Map<Class,ServiceInterface> _srvcIfaceMappings;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("rawtypes") 
	public ClientAPIImplBase(final Provider<SecurityContext> securityContextProvider,
							 final Marshaller modelObjectsMarshaller,
							 final Map<Class,ServiceInterface> srvcIfaceMappings) {
		this(securityContextProvider,
			 modelObjectsMarshaller,
			 srvcIfaceMappings,
			 null);		// No version
	}
	@SuppressWarnings("rawtypes") 
	public ClientAPIImplBase(final Provider<SecurityContext> securityContextProvider,
							 final Marshaller modelObjectsMarshaller,
							 final Map<Class,ServiceInterface> srvcIfaceMappings,
							 final AppVersion version) {
		_securityContextProvider = securityContextProvider;
		_modelObjectsMarshaller = modelObjectsMarshaller;
		_srvcIfaceMappings = srvcIfaceMappings;
		_version = version != null ? version : AppVersion.from("0.0.0");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CAST
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <A extends ClientAPI> A as(final Class<A> type) {
		return (A)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MARSHALLER
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Marshaller getModelObjectsMarshaller() {
		return _modelObjectsMarshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	API VERSION                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	public AppVersion getApiVersion() {
		return _version;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SECURITY CONTEXT
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Provider<SecurityContext> getSecurityContextProvider() {
		return _securityContextProvider;
	}
	@Override @SuppressWarnings("unchecked")
	public <U extends SecurityContext> U getSecurityContext() {
		return (U)_securityContextProvider.get();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SERVICE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("rawtypes")
	public Map<Class,ServiceInterface> getServiceInterfaceMappings() {
		return _srvcIfaceMappings;
	}
	@Override @SuppressWarnings("unchecked")
	public <S extends ServiceInterface> S getServiceInterfaceCoreImplOrProxy(final Class<S> serviceInterfaceType) {
		S outSrvcIfaceCoreImplOrProxy = _srvcIfaceMappings != null ? (S)_srvcIfaceMappings.get(serviceInterfaceType)
										  						   : null;
		return outSrvcIfaceCoreImplOrProxy;
	}
}