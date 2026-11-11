package r01f.services.bootstrap.config;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.enums.EnumExtended;
import r01f.services.core.IsCoreService;
import r01f.services.interfaces.IsProxyToCoreService;
import r01f.util.types.collections.CollectionUtils;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public enum ServicesCoreImpl
 implements EnumExtended<ServicesCoreImpl> {
	IN_PROCESS(Set.of(IsCoreService.class)),		
	REST(Set.of(IsProxyToCoreService.class)),			
	SERVLET(Set.of(IsProxyToCoreService.class));		
	
	private final Set<Class<? extends IsCoreServiceImplOrProxyToCoreServiceImpl>> _allowedBaseTypes;
	
	public boolean isAllowedBaseType(final Class<?> type) {
		return CollectionUtils.hasData(_allowedBaseTypes)
					? _allowedBaseTypes.stream()
									   .anyMatch(baseType -> baseType.isAssignableFrom(type))
					: false;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServicesCoreImpl fromName(final String name) {
		return EnumExtended.fromName(name,ServicesCoreImpl.class)
						   .orElseThrow(() -> new IllegalArgumentException("no " + ServicesCoreImpl.class + " element with name=" + name));
	}
	public static ServicesCoreImpl fromNameOrNull(final String name) {
		return EnumExtended.fromName(name,ServicesCoreImpl.class)
						   .orElse(null);
	}
	public static Set<ServicesCoreImpl> fromNames(final String... names) {
		if (CollectionUtils.isNullOrEmpty(names)) return null;
		return Stream.of(names)
					 .map(name -> ServicesCoreImpl.fromNameOrNull(name))
					 .filter(Objects::nonNull)
					 .collect(Collectors.toUnmodifiableSet());
	}
	public static Set<ServicesCoreImpl> asSet() {
		return Sets.newHashSet(ServicesCoreImpl.values());
	}
}
