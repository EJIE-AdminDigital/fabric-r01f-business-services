package r01f.services.core.persistence.db.config;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.TargetDatabase;

import jakarta.persistence.EntityManager;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.objectstreamer.annotations.MarshallType;
import r01f.services.core.persistence.db.config.DBSpecBase;
import r01f.services.core.persistence.db.config.DBVendor;
import r01f.services.core.persistence.db.config.DBPersistenceProviderVendorDBHints.DBHintKeyTranslation;
import r01f.services.core.persistence.db.config.DBPersistenceProviderVendorDBHints.DBHintVendorNeutralKey;
import r01f.util.types.Numbers;
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
@MarshallType(as="eclipseLinkDBSpec")
@Slf4j
@Accessors(prefix="_")
public class DBEclipseLinkSpec
     extends DBSpecBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBEclipseLinkSpec(final DBVendor vendor,final int majorVersion,final int minorVersion) {
		super(vendor,majorVersion,minorVersion,
			  // eclipselink hints
			  List.of(DBHintKeyTranslation.of(DBHintVendorNeutralKey.READ_ONLY,QueryHints.READ_ONLY)));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern SPEC_PATTERN = Pattern.compile("(" + TargetDatabase.MySQL + "|" + TargetDatabase.PostgreSQL + "|" + TargetDatabase.Oracle + ")\\s([0-9]+)\\.([0-9]+)");
	/**
	 * Loads the {@link DBSpecBase} form a {@link String} like
	 * (mysql|Oracle) {majorVersion}.{minorVersion}
	 * @param code
	 * @return
	 */
	public static DBSpecBase valueOf(final String code) {
		DBSpecBase outSpec = null;

		Matcher m = SPEC_PATTERN.matcher(code);
		if (m.find()) {
			DBVendor vendor = DBVendor.fromCode(m.group(1));
			int majorVersion = Integer.parseInt(m.group(2));
			int minorVersion = Integer.parseInt(m.group(3));
			outSpec = new DBEclipseLinkSpec(vendor,majorVersion,minorVersion);
		} else {
			throw new IllegalArgumentException(Throwables.message("The database spect DOES NOT have a valid format: {}. It MUST match {}",
																  code,SPEC_PATTERN.toString()));
		}
		return outSpec;
	}
	/**
	 * Loads the {@link DBSpecBase} from the {@link EntityManager} properties
	 * @param em
	 * @return
	 */
	public static DBSpecBase usedAt(final EntityManager em) {
		// All these properties should have been set at DBGuiceModuleBase
		// using the db properties xml file
		Map<String,Object> emProps = em.getProperties();
		Object dbVendorProp = emProps.get(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME);
		Object majorVersionProp = emProps.get(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION);
		Object minorVersionProp = emProps.get(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION);

		// some checks
		if (dbVendorProp == null) throw new IllegalArgumentException(Strings.customized("The {} entity manager property was NOT set using the db xml properties file",
																						PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME));
		if (majorVersionProp == null) throw new IllegalArgumentException(Strings.customized("The {} entity manager property was NOT set using the db xml properties file",
																						     PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION));
		if (minorVersionProp == null) throw new IllegalArgumentException(Strings.customized("The {} entity manager property was NOT set using the db xml properties file",
																						    PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION));
		if (!(dbVendorProp instanceof String) && !DBVendor.canBeFromCode((String)dbVendorProp)) throw new IllegalArgumentException(Strings.customized("The DBVendor name={} is NOT valid (the valid ones are {})",
																																   					  dbVendorProp,DBVendor.values()));
		if (!Numbers.isInteger(majorVersionProp.toString())) throw new IllegalArgumentException(Strings.customized("The db major version={} is NOT a valid int number",
																													majorVersionProp));
		if (!Numbers.isInteger(minorVersionProp.toString())) throw new IllegalArgumentException(Strings.customized("The db mino version={} is NOT a valid int number",
																													majorVersionProp));


		DBVendor dbVendor = DBVendor.fromCode(((String)dbVendorProp));
		int majorVersion = Numbers.toInt(majorVersionProp.toString());
		int minorVersion = Numbers.toInt(minorVersionProp.toString());
		DBSpecBase outSpec = new DBEclipseLinkSpec(dbVendor,
												   majorVersion,minorVersion);
		log.debug("DBSpec from entity manager properties:  {}",
				  outSpec.debugInfo());
		return outSpec;
	}
}
