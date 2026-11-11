package r01f.services.core.persistence;

 
import com.google.common.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.generics.TypeRef;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.services.core.IsCoreService;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.core.events.HasCOREEventBus;
import r01f.services.interfaces.ServiceInterface;

/**
 * Core service base
 */
@Accessors(prefix="_")
public abstract class CoreServicesBase
  		   implements IsCoreService,		// it's a core service
  		   			  HasCOREEventBus {		// it contains an event bus
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This properties are set at ServicesCoreBootstrapGuiceModuleBase type
	 */
	@Getter protected final ServicesCoreModuleConfig _coreConfig;
	/**
	 * Marshaller used to serialize / de-serialize java objects
	 */
	@Getter protected final Marshaller _modelObjectsMarshaller;
	/**
	 * EventBus
	 * IMPORTANT! The event listeners are subscribed at {@link BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase}
	 * 			  The subscription takes place when an event listener is configured at the guice moduel (see XXServicesBootstrapGuiceModule)
	 */
	@Getter protected final EventBus _eventBus;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param coreConfig	
	 * @param modelObjectsMarshaller annotated with @ModelObjectsMarshaller
	 * @param eventBus
	 */
	public CoreServicesBase(final ServicesCoreModuleConfig coreConfig,
							final Marshaller modelObjectsMarshaller,
							final EventBus eventBus) {
		_coreConfig = coreConfig;
		_modelObjectsMarshaller = modelObjectsMarshaller;
		_eventBus = eventBus;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DELEGATE PROVIDER
// 	A provider is used since typically a new persistence delegate is created at every
//	service impl method call to create a fresh new EntityManager
//	Note that a fresh new EntityManger is needed in every service impl method call
//	in order to avoid a single EntityManager that would cause transactional and
// 	concurrency issues
//	When at a delegate method services from another entity are needed (maybe to do some
//	validations), create a new delegate for the other entity reusing the current delegate
//	state (mainly the EntityManager), this way the transactional state is maintained:
//		public class CRUDServicesDelegateForX
//			 extends CRUDServicesForModelObjectDelegateBase<XOID,X> {
//			...
//			public CRUDResult<M> someMethod(..) {
//				....
//				CRUDServicesDelegateForY yDelegate = new CRUDServicesDelegateForY(this);	// reuse the transactional state
//				yDelegate.doSomething();
//				...
//
//		}
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract ServiceDelegateProvider<? extends ServiceInterface> getDelegateProvider();

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Override this method to check the SecurityContext
	 * @param securityContext
	 * @return
	 */
	public CoreServiceBaseCreateDelegateForSecurityContextStep forSecurityContext(final SecurityContext securityContext) {
		// [1] - check the security context
		// ... put any SecurityContext checking here
		
		// [2] - next step
		return new CoreServiceBaseCreateDelegateForSecurityContextStep(securityContext);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CoreServiceBaseCreateDelegateForSecurityContextStep {
		private final SecurityContext _securityContext;

		@SuppressWarnings({ "unchecked" })
		public <S extends ServiceInterface> S createDelegateAs(final Class<S> servicesType) {
			return (S)CoreServicesBase.this.getDelegateProvider().createDelegate(_securityContext);
		}
		@SuppressWarnings({ "unchecked" })
		public <S extends ServiceInterface> S createDelegateAs(final TypeToken<S> servicesType) {
			return (S)CoreServicesBase.this.getDelegateProvider().createDelegate(_securityContext);
		}
		@SuppressWarnings({ "unchecked" })
		public <S extends ServiceInterface> S createDelegateAs(final TypeRef<S> servicesType) {
			return (S)CoreServicesBase.this.getDelegateProvider().createDelegate(_securityContext);
		}
	}
}
