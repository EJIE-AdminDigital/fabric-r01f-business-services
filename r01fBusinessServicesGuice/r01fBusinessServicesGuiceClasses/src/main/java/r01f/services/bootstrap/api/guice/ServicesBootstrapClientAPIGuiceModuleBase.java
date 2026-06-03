package r01f.services.bootstrap.api.guice;

import com.google.inject.Binder;
import com.google.inject.Module;

import jakarta.inject.Singleton;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.inject.HasMoreBindings;
import r01f.services.api.ClientAPI;
import r01f.services.ids.ServiceIDs.ClientApiAppCode;

/**
 * This GUICE module is where the client-api bindings takes place
 *
 * At this module some client-side bindings are done:
 * <ol>
 * 		<li>Client APIs: types that aggregates the services access</li>
 * 		<li>Model object extensions</li>
 * 		<li>Server services proxies (ie: REST, bean, ejb)</li>
 * </ol>
 *
 * The execution flow is something like:
 * <pre>
 * ClientAPI
 *    |----> ServicesClientProxy
 * 						|---------------[ Proxy between client and server services ]
 * 														  |
 * 														  |----- [ HTTP / RMI / Direct Bean access ]-------->[REAL server / core side Services implementation]
 * </pre>
 *
 * The API simply offers access to service methods to the client and frees it from the buzz of knowing how to deal with different
 * service implementations (REST, EJB, Bean...).
 * All the logic related to transforming client method-calls to core services method calls is done at the PROXIES.
 * There's one proxy per core service implementation (REST, EJB, Bean...)
 *
 * <b>See file services-architecture.txt :: there is an schema of the app high level architecture</b>
 * </pre>
 */
@Slf4j
@Accessors(prefix="_")
@EqualsAndHashCode				// This is important for guice modules
public abstract class ServicesBootstrapClientAPIGuiceModuleBase
		   implements Module {	// this is a client guice bindings module
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS (all are set at bootstraping time at {@link ServicesBootstrap})
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final ClientApiAppCode _clientApiAppCode;
	@Getter protected final Class<? extends ClientAPI> _clientApiType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected ServicesBootstrapClientAPIGuiceModuleBase(final ClientApiAppCode clientApiAppCode,final Class<? extends ClientAPI> clientApiType) {
		_clientApiAppCode = clientApiAppCode;
		_clientApiType = clientApiType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GUICE MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		// [1] - Bind the client api as a Singleton
		if (_clientApiType != null) { 
			binder.bind(_clientApiType)
				  .in(Singleton.class);
		}
		
		// [99] - Other module-specific bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(binder);
		}
	}
}
