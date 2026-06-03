package r01f.services.core.persistence.db.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;
import r01f.enums.EnumWithCode;

@Accessors(prefix="_")
@RequiredArgsConstructor
public enum DBVendor
 implements EnumWithCode<String,DBVendor> {
	ORACLE("Oracle"),
	MySQL("MySQL"),
	PostgreSQL("PostgreSQL");

	@Getter private final Class<String> _codeType = String.class;
	@Getter private final String _code;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static DBVendor fromCode(final String code) {
		return EnumWithCode.fromCode(code,DBVendor.class)
						   .orElseThrow(() -> new IllegalArgumentException("no " + DBVendor.class + " element with code=" + code));
	}
	public static DBVendor fromName(final String name) {
		return EnumExtended.fromName(name,DBVendor.class)
						   .orElseThrow(() -> new IllegalArgumentException("no " + DBVendor.class + " element with name=" + name));
	}
	public static boolean canBeFromCode(final String code) {
		return EnumWithCode.canBeFromCode(code,DBVendor.class);
	}
	public static boolean canBe(final String name) {
		return EnumExtended.canBeFromName(name,DBVendor.class);
	}
}
