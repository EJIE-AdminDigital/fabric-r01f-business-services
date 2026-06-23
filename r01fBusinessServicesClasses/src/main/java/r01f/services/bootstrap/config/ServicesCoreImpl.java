package r01f.services.bootstrap.config;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import r01f.enums.EnumExtended;
import r01f.util.types.collections.CollectionUtils;

public enum ServicesCoreImpl
 implements EnumExtended<ServicesCoreImpl> {
	DEFAULT,		// Priority 0 (highest)
	IN_PROCESS,		// Priority 1
	REST,			// Priority 2
	EJB,			// Priority 3
	SERVLET,		// Priority 4 (it's NOT a full-fledged service since it's NOT consumed using a client api; it's called from a web browser so it has NO associated client-proxy)
	MOCK,			// Priority 5 (lower)
	NULL;			// used at ServicesCore annotation
	
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
