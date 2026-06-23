package r01f.services.bootstrap;

import java.util.Collection;
import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.patterns.Memoized;
import r01f.reflection.ReflectionUtils;
import r01f.services.bootstrap.config.IsCoreServiceImplOrProxyToCoreServiceImpl;
import r01f.services.core.IsCoreService;
import r01f.services.interfaces.IsProxyToCoreService;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Encapsulates a {@link ServiceInterface} match with a core impl or a proxy 
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public class ServiceInterfaceMatch
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Class<? extends ServiceInterface> _serviceInterfaceType;
	@Getter private final Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl> _coreImplOrProxyType;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final transient Memoized<Boolean> _isProxy = new Memoized<Boolean>() {
																	@Override
																	public Boolean supply() {
																		return _coreImplOrProxyType != null 
																					? ReflectionUtils.isImplementing(_coreImplOrProxyType,IsProxyToCoreService.class)
																					: false; 
																	}
														 };
	public boolean isProxy() {
		return _isProxy.get();
	}
	private final transient Memoized<Boolean> _isCoreImpl = new Memoized<Boolean>() {
																	@Override
																	public Boolean supply() {
																		return _coreImplOrProxyType != null 
																					? ReflectionUtils.isImplementing(_coreImplOrProxyType,IsCoreService.class)
																					: false;
																	}
														 	};
	public boolean isCoreImpl() {
		return _isCoreImpl.get();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isSameTypeAs(final ServiceInterfaceMatch other) {
		return this.isCoreImpl() && other.isCoreImpl()
			|| this.isProxy() && other.isProxy();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This match is for one of the given {@link ServiceInterface}s
	 * (ie the {@link ServiceInterface} part of this match is contained in the given collection of {@link ServiceInterface}s
	 * @param serviceInterfaceTypes
	 * @return
	 */
	public boolean serviceInterfaceTypeIs(final Class<? extends ServiceInterface> serviceInterfaceType) {
		return _serviceInterfaceType == serviceInterfaceType;
	}
	/**
	 * This match is for one of the given {@link IsCoreService}s
	 * (ie the [proxy or impl matching type] implements one of the given collection of {@link IsCoreService}s
	 * @param coreServiceBaseType
	 * @return
	 */
	public boolean coreImplOrProxyMatchingTypeIsAssignableFromOneOfTheseCoreServiceImplBaseTypes(final Collection<Class<? extends IsCoreService>> coreServiceBaseTypes) {
		if (!this.isCoreImpl() || CollectionUtils.isNullOrEmpty(coreServiceBaseTypes)) return false;
		boolean out = false;
		for (Class<? extends IsCoreService> coreServiceBaseType : coreServiceBaseTypes) {
			if (ReflectionUtils.isImplementing(_coreImplOrProxyType,coreServiceBaseType)) {
				out = true;
				break;
			}
		}
		return out;
	}
	/**
	 * This match is for one of the given {@link IsProxyToCoreService}s
	 * (ie the [proxy or impl matching type] implements one of the given collection of {@link IsProxyToCoreService}s
	 * @param serviceProxyBaseType
	 * @return
	 */
	public boolean coreImplOrProxyMatchingTypeIsAssignableFromOneOfTheseServiceProxyBaseTypes(final Collection<Class<? extends IsProxyToCoreService>> serviceProxyBaseTypes) {
		if (!this.isProxy() || CollectionUtils.isNullOrEmpty(serviceProxyBaseTypes)) return false;
		boolean out = false;
		for (Class<? extends IsProxyToCoreService> serviceProxyBaseType : serviceProxyBaseTypes) {
			if (ReflectionUtils.isImplementing(_coreImplOrProxyType,serviceProxyBaseType)) {
				out = true;
				break;
			}
		}
		return out;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EQUALS & HASH CODE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ServiceInterfaceMatch)) return false;
		
		ServiceInterfaceMatch other = (ServiceInterfaceMatch)obj;
		return this.getServiceInterfaceType() == other.getServiceInterfaceType()
			&& this.getCoreImplOrProxyType() == other.getCoreImplOrProxyType();
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getServiceInterfaceType(),this.getCoreImplOrProxyType());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("{} > {} match with {}",
								  _serviceInterfaceType,
								  this.isProxy() ? "PROXY" : "CORE IMPL",
								  _coreImplOrProxyType);
	}	
}
