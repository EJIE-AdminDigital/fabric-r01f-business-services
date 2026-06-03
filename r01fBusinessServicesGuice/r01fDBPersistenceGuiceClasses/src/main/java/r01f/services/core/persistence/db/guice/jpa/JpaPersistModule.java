package r01f.services.core.persistence.db.guice.jpa;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import r01f.services.core.persistence.db.annotations.EntityManagerFactoryForImpl;
import r01f.services.core.persistence.db.annotations.EntityManagerForImpl;
import r01f.services.core.persistence.db.guice.PersistModule;
import r01f.services.core.persistence.db.guice.PersistService;
import r01f.services.core.persistence.db.guice.UnitOfWork;
import r01f.services.core.persistence.db.guice.jpa.finder.DynamicFinder;
import r01f.services.core.persistence.db.guice.jpa.finder.Finder;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;

/**
 * JPA provider for guice persist.
 */
public final class JpaPersistModule 
		   extends PersistModule {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private final CoreAppCode _coreAppCode;
	private final CoreModule _coreModule;
	private final String _jpaUnit;
	private final JpaPersistOptions _options;
	
	private Map<?, ?> _properties;
	private MethodInterceptor _transactionInterceptor;
	private final List<Class<?>> _dynamicFinders = Lists.newArrayList();
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public JpaPersistModule(final CoreAppCode coreAppCode,final CoreModule coreModule,final String jpaUnit) {
		this(coreAppCode,coreModule,jpaUnit,
			 JpaPersistOptions.builder()
			 				  .build());
	}
	public JpaPersistModule(final CoreAppCode coreAppCode,final CoreModule coreModule,final String jpaUnit,
							final JpaPersistOptions options) {
		_coreAppCode = coreAppCode;
		_coreModule = coreModule;
		_jpaUnit = jpaUnit;
		
		_options = options;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void _configurePersistence() {
		String bindingId = String.format("%s.%s",
										 _coreAppCode,_coreModule);
		
		// constants injected in the JpaPersistService
		this.bindConstant()
			.annotatedWith(Jpa.class)
			.to(_jpaUnit);
		this.bind(JpaPersistOptions.class)
			.annotatedWith(Jpa.class)
			.toInstance(_options);

		// JpaPersisService is the main service that provides EntityManagers and transaction management
		this.bind(JpaPersistService.class)
			.in(Singleton.class);
		this.bind(PersistService.class)
			.to(JpaPersistService.class);
		this.bind(UnitOfWork.class)
			.to(JpaPersistService.class);
		
		// The EntityManagerFactory and EntityManager providers
		// a) new custom binding annotations
		this.bind(EntityManagerFactory.class)
			.annotatedWith(new EntityManagerFactoryForImpl(bindingId))
			.toProvider(JpaPersistService.EntityManagerFactoryProvider.class);
		this.bind(EntityManager.class)
			.annotatedWith(new EntityManagerForImpl(bindingId))
			.toProvider(JpaPersistService.class);
		
		// b) legacy bindings for backward compatibility 
		this.bind(EntityManagerFactory.class)
			.annotatedWith(Names.named(bindingId))
			.toProvider(JpaPersistService.EntityManagerFactoryProvider.class);
		this.bind(EntityManager.class)
			.annotatedWith(Names.named(bindingId))
			.toProvider(JpaPersistService.class);

		// The transaction interceptor
		_transactionInterceptor = new JpaLocalTxnInterceptor();
		this.requestInjection(_transactionInterceptor);

		// Bind dynamic finders.
		for (Class<?> finder : _dynamicFinders) {
			_bindFinder(finder);
		}
	}
	@Override
	protected MethodInterceptor _getTransactionInterceptor() {
		return _transactionInterceptor;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PROPERTIES
/////////////////////////////////////////////////////////////////////////////////////////	
	@Provides 
	@Jpa
	Map<?,?> _provideProperties() {
		return _properties;
	}
	/**
	 * Configures the JPA persistence provider with a set of properties.
	 * @param properties A set of name value pairs that configure a JPA persistence
	 *                   provider as per the specification.
	 */
	public JpaPersistModule properties(final Map<?,?> properties) {
		_properties = properties;
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	FINDERS
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Adds an interface to this module to use as a dynamic finder.
	 * @param iface Any interface type whose methods are all dynamic finders.
	 */
	public <T> JpaPersistModule addFinder(final Class<T> iface) {
		_dynamicFinders.add(iface);
		return this;
	}
	private <T> void _bindFinder(final Class<T> iface) {
		if (!_isDynamicFinderValid(iface)) return;
		InvocationHandler finderInvoker = new InvocationHandler() {
												@Inject
												JpaFinderProxy finderProxy;
									
												@Override
												public Object invoke(final Object thisObject,final Method method,final Object[] args) throws Throwable {
									
													// Don't intercept non-finder methods like equals and hashcode.
													if (!method.isAnnotationPresent(Finder.class)) {
														// NOTE(user): This is not ideal, we are using the invocation handler's equals
														// and hashcode as a proxy (!) for the proxy's equals and hashcode.
														return method.invoke(this,args);
													}
													return finderProxy.invoke(new MethodInvocation() {
																					@Override
																					public Method getMethod() {
																						return method;
																					}
																					@Override
																					public Object[] getArguments() {
																						return null == args ? new Object[0] : args;
																					}
																					@Override
																					public Object proceed() throws Throwable {
																						return method.invoke(thisObject,args);
																					}
																					@Override
																					public Object getThis() {
																						throw new UnsupportedOperationException("Bottomless proxies don't expose a this.");
																					}
																					@Override
																					public AccessibleObject getStaticPart() {
																						throw new UnsupportedOperationException();
																					}
																				});
												}
											};
		this.requestInjection(finderInvoker);

		@SuppressWarnings("unchecked") // Proxy must produce instance of type given.
		T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[] { iface },
											 finderInvoker);

		this.bind(iface)
			.toInstance(proxy);
	}
	private boolean _isDynamicFinderValid(final Class<?> iface) {
		boolean valid = true;
		if (!iface.isInterface()) {
			this.addError(iface + " is not an interface. Dynamic Finders must be interfaces.");
			valid = false;
		}
		for (Method method : iface.getMethods()) {
			DynamicFinder finder = DynamicFinder.from(method);
			if (null == finder) {
				addError("Dynamic Finder methods must be annotated with @Finder, but " + iface + "." + method.getName()
						+ " was not");
				valid = false;
			}
		}
		return valid;
	}
}
