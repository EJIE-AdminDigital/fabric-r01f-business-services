package r01f.services.core.persistence.db.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;
import r01f.enums.EnumWithCode;

@Accessors(prefix="_")
@RequiredArgsConstructor
public enum DBDialect
 implements EnumWithCode<String,DBDialect> {
	ORACLE("org.hibernate.dialect.OracleDialect"),
	MySQL("org.hibernate.dialect.MySQLDialect"),
	PostgreSQL("org.hibernate.dialect.MySQLDialect");

	@Getter private final Class<String> _codeType = String.class;
	@Getter private final String _code;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static DBDialect fromCode(final String code) {
		return EnumWithCode.fromCode(code,DBDialect.class)
						   .orElseThrow(() -> new IllegalArgumentException("no " + DBDialect.class + " element with code=" + code));
	}
	public static DBDialect fromName(final String name) {
		return EnumExtended.fromName(name,DBDialect.class)
						   .orElseThrow(() -> new IllegalArgumentException("no " + DBDialect.class + " element with name=" + name));
	}
	public static boolean canBeFromCode(final String code) {
		return EnumWithCode.canBeFromCode(code,DBDialect.class);
	}
	public static boolean canBe(final String name) {
		return EnumExtended.canBeFromName(name,DBDialect.class);
	}
}
