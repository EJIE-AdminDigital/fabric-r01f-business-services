package r01f.services.core.persistence;

import r01f.model.security.context.SecurityContext;
import r01f.services.core.IsCoreService;
import r01f.services.interfaces.ServiceInterface;

/**
 * A provider os {@link ServiceInterface} delegates to be used at {@link IsCoreService}
 * @param <D>
 */
@FunctionalInterface
public interface ServiceDelegateProvider<D extends ServiceInterface> {
	/**
	 * Creates a delegate
	 * @param securityContext
	 * @return
	 */
	public D createDelegate(final SecurityContext securityContext);
}