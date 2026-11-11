package r01f.services.core.delegates;

import jakarta.inject.Provider;
import r01f.model.security.context.SecurityContext;
import r01f.services.interfaces.ServiceInterface;

public abstract class ServicesDelegateProvider<D extends ServiceInterface> 
	       implements Provider<D> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public abstract D get(final SecurityContext securityContext);
	
	@Override
	public D get() {
		return this.get(null);	// no user context
	}
}
