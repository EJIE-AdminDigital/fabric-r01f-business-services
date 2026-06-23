package r01f.services.bootstrap;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import r01f.reflection.ReflectionUtils;
import r01f.reflection.scanner.ClassgraphScanHandler;
import r01f.reflection.scanner.ScannerForExtendingOrImplementingTypes;
import r01f.reflection.scanner.ScannerForImplementingTypes;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.interfaces.ExposedServiceInterface;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

class ServicesClassPathScanner {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final ClassgraphScanHandler _classPathScanner;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	ServicesClassPathScanner(final ClassgraphScanHandler classPathScanner) {
		_classPathScanner = classPathScanner;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SERVICE INTERFACE FIND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds {@link ServiceInterface}-implementing interfaces
	 * @param serviceInterfacesPckg
	 * @return
	 */
	public Set<Class<? extends ServiceInterface>> findServiceInterfaceTypes(final Collection<Class<? extends ServiceInterface>> serviceInterfacesBaseTypes) {
		Set<Class<? extends ServiceInterface>> serviceInterfaceTypes = Sets.newLinkedHashSet();

		// iterate all over the base types
		for (Class<? extends ServiceInterface> serviceInterfaceBaseType : serviceInterfacesBaseTypes) {
			Set<Class<? extends ServiceInterface>> thisServiceInterfaceTypes = this.findServiceInterfaceTypes(serviceInterfaceBaseType);
			if (CollectionUtils.hasData(thisServiceInterfaceTypes)) serviceInterfaceTypes.addAll(thisServiceInterfaceTypes);
		}
		return serviceInterfaceTypes;
	}
	/**
	 * Finds types implementing {@link ServiceInterface}
	 * @param serviceInterfacesPckg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Class<? extends ServiceInterface>> findServiceInterfaceTypes(final Class<? extends ServiceInterface> serviceInterfacesBaseType) {
		// [1] - do find ServiceInterface-implementing types annotated with @ExposedServiceInterface
		//		 ... also remove interface types NOT annotated with @ExposedServiceInterface (this is because sometimes there are BASE service interfaces
		//			 that we do NOT want to be detected as REAL -final- service interfaces; these BASE service interfaces are NOT annotated
		//			 with @ExposedServiceInterface)
		ScannerForImplementingTypes implementingTypesScanner = new ScannerForImplementingTypes(_classPathScanner);
		Set<Class<? extends ServiceInterface>> srvcIfaces = implementingTypesScanner.findTypesImplementing((Class<ServiceInterface>)serviceInterfacesBaseType,
																										   ExposedServiceInterface.class);	// ServiceInterface.class annotated with @ExposedServiceInterface
		// [2] - checkings
		if (CollectionUtils.isNullOrEmpty(srvcIfaces)) throw new IllegalStateException("Could NOT find any interface extending " + serviceInterfacesBaseType + " and annotated with @" + ExposedServiceInterface.class.getSimpleName());

		for (Class<? extends ServiceInterface> serviceInterfaceType : srvcIfaces) {
			if (!ReflectionUtils.isInterface(serviceInterfaceType)) throw new IllegalStateException(String.format("%s is NOT a valid %s: it MUST be a java interface, NOT a 'normal' class",
																												  serviceInterfaceType,ServiceInterface.class.getSimpleName()));
		}
		// [3] - Return
		return srvcIfaces;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PROXY OR CORE SERVICE IMPL FIND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds types implementing {@link ServiceInterface}
	 * @param serviceInterfacesPckg
	 * @return
	 */
	public Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> findServiceInterfaceProxyOrCoreImplTypes(final Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> coreImplOrProxyType) {
		// [1] - do find type implementing types

		ScannerForExtendingOrImplementingTypes extendingOrImplementinTypesScanner = new ScannerForExtendingOrImplementingTypes(_classPathScanner);
		Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> coreImplOrProxyTypes = (Set) extendingOrImplementinTypesScanner.findTypesExtendingOrImplementing(coreImplOrProxyType);

		// [2] - Filter abstract or interface types
		coreImplOrProxyTypes = coreImplOrProxyTypes.stream()
									 			   .filter(ReflectionUtils::isInstanciable)
									 			   .collect(Collectors.toSet());
		// [3] - Checkings
    	if (CollectionUtils.isNullOrEmpty(coreImplOrProxyTypes)) {
    		throw new IllegalStateException(Strings.customized("Could NOT find any type extending or implementing '{} ' at packages '{}'",
    														   coreImplOrProxyType,
    														   _classPathScanner.getAcceptedJavaPackages()
    														   				    .stream()
    														   				    .map(p -> p.asString())
    														   				    .collect(Collectors.joining(","))));
    	}
    	// [4] - Return
    	return coreImplOrProxyTypes;
	}
}
