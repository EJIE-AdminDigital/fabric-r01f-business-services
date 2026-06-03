package r01f.services.interfaces;

import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;

/**
 * base interface for proxy flavors:
 * <ul>
 * 		<li>{@link ProxyForRESTImplementedService}</li>
 * 		<li>{@link ProxyForEJBImplementedService}</li>
 * 		<li>etc</li>
 * </ul>
 */
public interface IsProxyToCoreService 
		 extends IsCoreServiceImplOrProxyToCoreServiceImpl {
	// just a marker interface
}
