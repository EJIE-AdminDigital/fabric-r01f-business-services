package r01f.services.bootstrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
import r01f.services.ids.ServiceIDs.ClientApiAppCode;
import r01f.services.ids.ServiceIDs.CoreAppCode;
import r01f.services.ids.ServiceIDs.CoreModule;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Encapsulates a collection of {@link ServiceInterfaceMatch} object
 * that matches a {@link ServiceInterface} with a CORE or PROXY impl
 */
@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public class ServiceInterfacesMatchings 
  implements Iterable<ServiceInterfacesMatchingsForCoreModule>,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final ClientApiAppCode _clientApiAppCode;
	@Getter private final Collection<ServiceInterfacesMatchingsForCoreModule> _serviceInterfacesMatchingsByCoreModule;

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean hasData() {
		return CollectionUtils.hasData(_serviceInterfacesMatchingsByCoreModule);
	}
	public boolean isEmpty() {
		return CollectionUtils.isNullOrEmpty(_serviceInterfacesMatchingsByCoreModule);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	ITERABLE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Iterator<ServiceInterfacesMatchingsForCoreModule> iterator() {
		return _serviceInterfacesMatchingsByCoreModule.iterator();
	}
	public Iterator<ServiceInterfacesMatchingsForCoreModule> iteratorFor(final Predicate<ServiceInterfacesMatchingsForCoreModule> pred) {
		return StreamSupport.stream(this.spliterator(),
									false)	// not parallel	 
							.filter(pred::test)
							.iterator();
	}
	public Iterable<ServiceInterfacesMatchingsForCoreModule> iterableFor(final Predicate<ServiceInterfacesMatchingsForCoreModule> pred) {
		return CollectionUtils.iterableFrom(this.iteratorFor(pred));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the matchings between [service interface] and [core impl] or [proxy to core impl]
	 * @param coreAppCode
	 * @param coreModule
	 * @return
	 */
	public ServiceInterfacesMatchingsForCoreModule getServiceInterfaceMatchingsFor(final CoreAppCode coreAppCode,final CoreModule coreModule) {
		return CollectionUtils.hasData(_serviceInterfacesMatchingsByCoreModule)
					? _serviceInterfacesMatchingsByCoreModule.stream()
															 .filter(matchingsForModule -> matchingsForModule.getCoreAppCode() == coreAppCode
																					    && matchingsForModule.getCoreModule() == coreModule)
															 .findFirst()
															 .orElse(null)
					: null;
	}
	/**
	 * Returns a collection with the matchings between [service interface] and [core impl] or [proxy to core impl]
	 * for all the core modules defined
	 * @return
	 */
	public Collection<ServiceInterfaceMatch> getAllServiceInterfaceToCoreImplOrProxyMatchings() {
		return CollectionUtils.hasData(_serviceInterfacesMatchingsByCoreModule)
					? _serviceInterfacesMatchingsByCoreModule.stream()
															 .flatMap(matchingsForModule -> matchingsForModule.getServiceInterfacesMatchings()
																	 										  .stream())
															 .collect(Collectors.toList())
					: Collections.emptyList();
	}
	/**
	 * Returns a map with the matchings between [service interface] and [core impl] or [proxy to core impl]
	 * for all the core modules defined
	 * @return
	 */
	public Map<Class<? extends ServiceInterface>,
			   Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> getAllServiceInterfaceToCoreImplOrProxyMatchingsAsMap() {
		return CollectionUtils.hasData(_serviceInterfacesMatchingsByCoreModule)
					? _serviceInterfacesMatchingsByCoreModule.stream()
														      .flatMap(matchingsForModule -> matchingsForModule.getServiceInterfaceToCoreImplOrProxyMatchingsAsMap()
														    		  										   .entrySet()
														    		  										   .stream())
														  	  .collect(Collectors.toMap(m -> m.getKey(),
																					    m -> m.getValue()))
					: Collections.emptyMap();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("API {} service interface matchings\n" +
								   "------------------------------\n" +
								  "{}",
								  _clientApiAppCode,
								  CollectionUtils.hasData(_serviceInterfacesMatchingsByCoreModule) 
								  		? _serviceInterfacesMatchingsByCoreModule.stream()
																	 .map(cfg -> cfg.debugInfo())
										  							 .collect(Collectors.joining("\n"))
										: "NO core modules defined!");
	}
}
