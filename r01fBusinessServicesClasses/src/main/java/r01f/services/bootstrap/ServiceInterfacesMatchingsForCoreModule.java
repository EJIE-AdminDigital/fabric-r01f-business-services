package r01f.services.bootstrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


@Accessors(prefix="_")
@RequiredArgsConstructor
public class ServiceInterfacesMatchingsForCoreModule
  implements Iterable<ServiceInterfaceMatch>,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The core app code
	 */
	@Getter private final CoreAppCode _coreAppCode;
	/**
	 * The Core module
	 */
	@Getter private final CoreModule _coreModule;
	/**
	 * The matchings between the service interfaces and the [core impl] or [proxies to core impl]
	 */
	@Getter private final Collection<ServiceInterfaceMatch> _serviceInterfacesMatchings = new LinkedList<>();
/////////////////////////////////////////////////////////////////////////////////////////
//  ADD
/////////////////////////////////////////////////////////////////////////////////////////
	public ServiceInterfacesMatchingsForCoreModule addMatching(final ServiceInterfaceMatch match) {
		// store the BEST match
		ServiceInterfaceMatch existing = this.getServiceInterfaceMatchingFor(match.getServiceInterfaceType());
		if (existing != null) {
			if (match.isCoreImpl() && existing.isProxy()) {
				// the new match is better than the existing... replace it!
				_serviceInterfacesMatchings.remove(existing);
				_serviceInterfacesMatchings.add(match);
			} if (match.isSameTypeAs(existing)) {
				throw new IllegalStateException(String.format("There exist TWO matchings of the SAME TYPE for [service interface]=%s: %s AND %s (there MUST exist a SINGLE ONE",
															  match.getServiceInterfaceType(),
															  match.getCoreImplOrProxyType(),existing.getCoreImplOrProxyType()));
			} else {
				// there exists a CORE impl matching that is BETTER than the new match
			}
		} else {
			_serviceInterfacesMatchings.add(match);
		}
		return this;
	}
	public ServiceInterfacesMatchingsForCoreModule addMatching(final Class<? extends ServiceInterface> serviceInterfaceType,
											 	  			   final Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> coreImplOrProxyMatch) {
		ServiceInterfaceMatch match = new ServiceInterfaceMatch(serviceInterfaceType,
															    coreImplOrProxyMatch);
		return this.addMatching(match);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a map with the [service interface] to [core impl] or [proxy] matchings
	 * @return
	 */
	public Map<Class<? extends ServiceInterface>,
			   Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> getServiceInterfaceToCoreImplOrProxyMatchingsAsMap() {
		return CollectionUtils.hasData(_serviceInterfacesMatchings) 
					? _serviceInterfacesMatchings.stream()
											     .collect(Collectors.toMap(match -> match.getServiceInterfaceType(),
																	       match -> match.getCoreImplOrProxyType()))
					: Collections.emptyMap();
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean hasData() {
		return CollectionUtils.hasData(_serviceInterfacesMatchings);
	}
	public boolean isEmpty() {
		return CollectionUtils.isNullOrEmpty(_serviceInterfacesMatchings);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FILTERING
/////////////////////////////////////////////////////////////////////////////////////////
	public ServiceInterfaceMatch getServiceInterfaceMatchingFor(final Class<? extends ServiceInterface> serviceInterfaceType) {
		return _serviceInterfacesMatchings.stream()
										  .filter(match -> match.serviceInterfaceTypeIs(serviceInterfaceType))	// service interface is the given one
										  .findFirst()
										  .orElse(null);
	}
	public boolean existsServiceInterfaceMatchingFor(final Class<? extends ServiceInterface> serviceInterfaceType) {
		return this.getServiceInterfaceMatchingFor(serviceInterfaceType) != null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ITERABLE
/////////////////////////////////////////////////////////////////////////////////////////
////////// Base
	@Override
	public Iterator<ServiceInterfaceMatch> iterator() {
		return _serviceInterfacesMatchings.iterator();
	}
	public Iterator<ServiceInterfaceMatch> iteratorFor(final Predicate<ServiceInterfaceMatch> pred) {
		return StreamSupport.stream(this.spliterator(),
									false)	// not parallel	 
							.filter(pred::test)
							.iterator();
	}
	public Iterable<ServiceInterfaceMatch> iterableFor(final Predicate<ServiceInterfaceMatch> pred) {
		return CollectionUtils.iterableFrom(this.iteratorFor(pred));
	}
////////// Core Impl
	public Iterable<ServiceInterfaceMatch> coreImplMatchingsIterable() {
		return CollectionUtils.iterableFrom(this.coreImplMatchingsIterator());
	}
	public Iterator<ServiceInterfaceMatch> coreImplMatchingsIterator() {
		return this.iteratorFor(ServiceInterfaceMatch::isCoreImpl);
	}
////////// Proxy
	public Iterable<ServiceInterfaceMatch> proxyMathingsIterable() {
		return CollectionUtils.iterableFrom(this.proxyMathingsIterator());
	}
	public Iterator<ServiceInterfaceMatch> proxyMathingsIterator() {
		return this.iteratorFor(ServiceInterfaceMatch::isProxy);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("* [CORE {}.{}] service interfaces matchings:\n" + 
								  "{}",
				  				  _coreModule,_coreAppCode,
				  				  CollectionUtils.hasData(_serviceInterfacesMatchings) 
				  				  		? _serviceInterfacesMatchings.stream()
				  				  								     .map(match -> "\t- " + match.debugInfo())
				  								   .collect(Collectors.joining("\n"))
				  				  		: "\tNO service interface matchings found!");
	}
}
