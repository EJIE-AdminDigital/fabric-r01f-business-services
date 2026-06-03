package r01f.services.core.persistence.db.config;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.HibernateHints;

import jakarta.persistence.EntityManager;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.objectstreamer.annotations.MarshallType;
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
@MarshallType(as="hibernateDBSpec")
@Slf4j
@Accessors(prefix="_")
public class DBHibernateSpec
     extends DBSpecBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBHibernateSpec(final DBVendor vendor,final int majorVersion,final int minorVersion) {
		super(vendor,majorVersion,minorVersion,
			  // eclipselink hints
			  List.of(DBHintKeyTranslation.of(DBHintVendorNeutralKey.READ_ONLY,HibernateHints.HINT_READ_ONLY)));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern SPEC_PATTERN = Pattern.compile("(" + DBVendor.MySQL.getCode() + "|" + DBVendor.PostgreSQL.getCode() + "|" + DBVendor.ORACLE.getCode() + ")\\s([0-9]+)\\.([0-9]+)");
	/**
	 * Loads the {@link DBHibernateSpec} form a {@link String} like
	 * (mysql|Oracle) {majorVersion}.{minorVersion}
	 * @param code
	 * @return
	 */
	public static DBHibernateSpec valueOf(final String code) {
		DBHibernateSpec outSpec = null;

		Matcher m = SPEC_PATTERN.matcher(code);
		if (m.find()) {
			DBVendor vendor = DBVendor.fromCode(m.group(1));
			int majorVersion = Integer.parseInt(m.group(2));
			int minorVersion = Integer.parseInt(m.group(3));
			outSpec = new DBHibernateSpec(vendor,majorVersion,minorVersion);
		} else {
			throw new IllegalArgumentException(Throwables.message("The database spect DOES NOT have a valid format: {}. It MUST match {}",
																  code,SPEC_PATTERN.toString()));
		}
		return outSpec;
	}
	/**
	 * Loads the {@link DBHibernateSpec} from the {@link EntityManager} properties
	 * @param em
	 * @return
	 */
	public static DBHibernateSpec usedAt(final EntityManager em) {
		// All these properties should have been set at DBGuiceModuleBase
		// using the db properties xml file
		Object dbVendorProp = null;
		Object majorVersionProp = null;
		Object minorVersionProp = null;
		try {
			// In EclipseLink, the values were loaded into an em property,
			// 		e.g., "emProps.get("jakarta.persistence.database-product-name")"
			// Now we must access JDBC directly to retrieve the values.
            var sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactoryImplementor.class);
            var connectionProvider = sessionFactory.getServiceRegistry().getService(ConnectionProvider.class);
            try (var connection = connectionProvider.getConnection()) {
                var metaData = connection.getMetaData();
                dbVendorProp = metaData.getDatabaseProductName();
                majorVersionProp = metaData.getDatabaseMajorVersion();
                minorVersionProp = metaData.getDatabaseMinorVersion();
            }
        } catch (SQLException e) {
			e.printStackTrace();
		}
		// some checks
		if (dbVendorProp == null) throw new IllegalArgumentException(Strings.customized("The {} entity manager property was NOT set using the db xml properties file",
																						"jakarta.persistence.database-product-name"));
		if (majorVersionProp == null) throw new IllegalArgumentException(Strings.customized("The {} entity manager property was NOT set using the db xml properties file",
																						     "jakarta.persistence.database-major-version"));
		if (minorVersionProp == null) throw new IllegalArgumentException(Strings.customized("The {} entity manager property was NOT set using the db xml properties file",
																						    "jakarta.persistence.database-minor-version"));
		if (!(dbVendorProp instanceof String) && !DBVendor.canBeFromCode((String)dbVendorProp)) throw new IllegalArgumentException(Strings.customized("The DBVendor name={} is NOT valid (the valid ones are {})",
																																   					  dbVendorProp,DBVendor.values()));
		if (!Numbers.isInteger(majorVersionProp.toString())) throw new IllegalArgumentException(Strings.customized("The db major version={} is NOT a valid int number",
																													majorVersionProp));
		if (!Numbers.isInteger(minorVersionProp.toString())) throw new IllegalArgumentException(Strings.customized("The db mino version={} is NOT a valid int number",
																													majorVersionProp));


		DBVendor dbVendor = DBVendor.fromCode(((String)dbVendorProp));
		int majorVersion = Numbers.toInt(majorVersionProp.toString());
		int minorVersion = Numbers.toInt(minorVersionProp.toString());
		DBHibernateSpec outSpec = new DBHibernateSpec(dbVendor,
													  majorVersion,minorVersion);
		log.debug("DBSpec from entity manager properties:  {}",
				  outSpec.debugInfo());
		return outSpec;
	}
}
