package r01f.services.core.persistence.db.config;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;

@Accessors(prefix="_")
@RequiredArgsConstructor
public enum PersistenceUnitType
 implements EnumExtended<PersistenceUnitType> {
	DRIVER_MANAGER,
	DATASOURCE;

/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static PersistenceUnitType fromName(final String name) {
		return EnumExtended.fromName(name,PersistenceUnitType.class)
						   .orElseThrow(() -> new IllegalArgumentException("no " + PersistenceUnitType.class + " element with name=" + name));
	}
}
