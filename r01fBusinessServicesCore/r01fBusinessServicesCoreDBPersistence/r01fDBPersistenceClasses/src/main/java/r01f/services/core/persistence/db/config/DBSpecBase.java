package r01f.services.core.persistence.db.config;

import java.util.Collection;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.objectstreamer.annotations.MarshallField;
import r01f.objectstreamer.annotations.MarshallField.MarshallFieldAsXml;
import r01f.objectstreamer.annotations.MarshallPolymorphicTypeInfo;
import r01f.services.core.persistence.db.config.DBPersistenceProviderVendorDBHints.DBHintKeyTranslation;
import r01f.util.types.Strings;

/**
 * Encapsulates the database specification needed when defining the
 * schema generation at persistence.xml
 * For MySql:
 * <pre class='brush:xml'>
 *			<property name="jakarta.persistence.database-product-name" value="mysql"/>
 *			<property name="jakarta.persistence.database-major-version" value="5"/>
 *			<property name="jakarta.persistence.database-minor-version" value="6"/>
 * </pre>
 * For Oracle:
 * <pre class='brush:xml'>
 *			<property name="jakarta.persistence.database-product-name" value="Oracle"/>
 *			<property name="jakarta.persistence.database-major-version" value="11"/>
 *			<property name="jakarta.persistence.database-minor-version" value="2"/>
 * </pre>
 */
@MarshallPolymorphicTypeInfo
@Accessors(prefix="_")
public abstract class DBSpecBase
  	       implements DBSpec {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public final static String DEFAULT = "MySQL 5.7";
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@MarshallField(as="vendor",
				   whenXml=@MarshallFieldAsXml(attr=true))
	@Getter protected final DBVendor _vendor;

	@MarshallField(as="majorVersion",
				   whenXml=@MarshallFieldAsXml(attr=true))
	@Getter protected final int _majorVersion;

	@MarshallField(as="minorVersion",
				   whenXml=@MarshallFieldAsXml(attr=true))
	@Getter protected final int _minorVersion;

	@MarshallField(as="dbHints")
	@Getter protected final DBPersistenceProviderVendorDBHints _vendorDBHints;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBSpecBase(final DBVendor vendor,final int majorVersion,final int minorVersion,
					  final DBPersistenceProviderVendorDBHints vendorDBHints) {
		_vendor = vendor;
		_majorVersion = majorVersion;
		_minorVersion = minorVersion;
		_vendorDBHints = vendorDBHints;
	}
	public DBSpecBase(final DBVendor vendor,final int majorVersion,final int minorVersion,
					  final Collection<DBHintKeyTranslation> vendorSpecificHints) {
		this(vendor,majorVersion,minorVersion,
			 new DBPersistenceProviderVendorDBHints(vendorSpecificHints));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("{} {}.{}",_vendor,_majorVersion,_minorVersion);
	}
}
