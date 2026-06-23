package r01f.services.core.persistence.db.config;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.EntityManager;
import r01f.guids.OID;
import r01f.objectstreamer.annotations.MarshallType;
import r01f.services.core.persistence.db.config.DBPersistenceProviderVendorDBHints.DBHintKeyTranslation;
import r01f.util.types.collections.CollectionUtils;

/**
 * DB hints names are vendor-specific (the ones of Hibernate are different from the ones of EclipseLink)
 * For Example:
 *	Feature	    Hibernate Key	                EclipseLink Key
 *  ===========================================================================
 *	Read Only	org.hibernate.readOnly	        eclipselink.read-only
 *	Fetch Size	jakarta.persistence.fetchSize	eclipselink.jdbc.fetch-size
 *	Cacheable	org.hibernate.cacheable	        eclipselink.query-results-cache
 *	Batch Size	org.hibernate.batch_size	    eclipselink.batch
 *
 * This class maps vendor-agnostic keys to vendor-specific keys
 */
@MarshallType(as="vendorDBHints")
public class DBPersistenceProviderVendorDBHints
	 extends ArrayList<DBHintKeyTranslation> {

	private static final long serialVersionUID = -3410650167486557987L;

	////////// CONSTRUCTOR
	public DBPersistenceProviderVendorDBHints() {
		this((Collection<DBHintKeyTranslation>)null);
	}
	public DBPersistenceProviderVendorDBHints(final Collection<DBHintKeyTranslation> vendorSpecificHints) {
		// add the vendor-specific hints
		if (CollectionUtils.hasData(vendorSpecificHints)) this.addAll(vendorSpecificHints);

		// add the vendor-neutral hints
		_addVendorNeutralHints();
	}
	public DBPersistenceProviderVendorDBHints(final EntityManager em) {
		// get the vendor
		DBPersistenceProvider vendor = DBPersistenceProvider.fromEntityManager(em);
		
		// hardcode db hints based on the vendor
		Collection<DBHintKeyTranslation> vendorSpecificHints = new ArrayList<>();
        if (vendor == DBPersistenceProvider.HIBERNATE) {
        	// hibernate specific hints
        	vendorSpecificHints.add(DBHintKeyTranslation.of(DBHintVendorNeutralKey.READ_ONLY,"org.hibernate.readOnly"));	// HibernateHints.HINT_READ_ONLY
        }
        else if (vendor == DBPersistenceProvider.ECLIPSELINK) {
        	// eclipselink specific hints
			vendorSpecificHints.add(DBHintKeyTranslation.of(DBHintVendorNeutralKey.READ_ONLY,"eclipselink.read-only"));		// QueryHints.READ_ONLY
        }
        else {
        	throw new IllegalStateException(vendor + " is NOT a supported ORM supported vendor; just " + DBPersistenceProvider.HIBERNATE + " and " + DBPersistenceProvider.ECLIPSELINK + " are supported");
        }

		// add the vendor-specific hints
		if (CollectionUtils.hasData(vendorSpecificHints)) this.addAll(vendorSpecificHints);

		// add the vendor-neutral hints
		_addVendorNeutralHints();
	}
	private void _addVendorNeutralHints() {
		// add the vendor-neutral hints (the ones defined in SpecHints in the case of Hibernate)
		this.add(DBHintKeyTranslation.of(DBHintVendorNeutralKey.LOCK_TIMEOUT,"jakarta.persistence.lock.timeout"));
		this.add(DBHintKeyTranslation.of(DBHintVendorNeutralKey.QUERY_TIMEOUT,"jakarta.persistence.query.timeout"));
		this.add(DBHintKeyTranslation.of(DBHintVendorNeutralKey.CACHE_STORE_MODE,"jakarta.persistence.cache.storeMode"));
		this.add(DBHintKeyTranslation.of(DBHintVendorNeutralKey.CACHE_RETRIEVE_MODE,"jakarta.persistence.cache.retrieveMode"));
	}
	
	public static DBPersistenceProviderVendorDBHints fromEntityManager(final EntityManager em) {
		return new DBPersistenceProviderVendorDBHints(em);
	}

	////////// Retrievers
	public String getReadOnlyHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.READ_ONLY);
	}
	public String getFetchSizeHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.FETCH_SIZE);
	}
	public String getCacheableHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.CACHEABLE);
	}
	public String getLockTimeoutHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.LOCK_TIMEOUT);
	}
	public String getQueryTimeoutHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.QUERY_TIMEOUT);
	}
	public String getCacheStoreModeHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.CACHE_STORE_MODE);
	}
	public String getCacheRetrieveModeHintKey() {
		return this.getHintKeyTranslated(DBHintVendorNeutralKey.CACHE_RETRIEVE_MODE);
	}
	////////// Generic Retriever
	public String getHintKeyTranslated(final DBHintVendorNeutralKey key) {
		return this.stream()
				   .filter(kv -> kv.vendorNeutralKey().equals(key))
				   .findFirst()
				   .map(kv -> kv.vendorSpecificKey())
				   .orElse(null);
	}
	public boolean hasHintKey(final DBHintVendorNeutralKey key) {
		return this.stream()
				   .filter(kv -> kv.vendorNeutralKey().equals(key))
				   .count() > 0;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	////////// DBHint Key
	public record DBHintKeyTranslation(DBHintVendorNeutralKey vendorNeutralKey,String vendorSpecificKey) {
		public static DBHintKeyTranslation of(final DBHintVendorNeutralKey key,final String vendorSpecificKey) {
			return new DBHintKeyTranslation(key,vendorSpecificKey);
		}
	}
	public record DBHintVendorNeutralKey(String id)
	   implements OID {

		// vendor-specific hints
		public static final DBHintVendorNeutralKey READ_ONLY = DBHintVendorNeutralKey.of("READ_ONLY");
		public static final DBHintVendorNeutralKey FETCH_SIZE = DBHintVendorNeutralKey.of("FETCH_SIZE");
		public static final DBHintVendorNeutralKey CACHEABLE = DBHintVendorNeutralKey.of("CACHEABLE");

		// vendor-neutral hints (the ones defined in SpecHints in the case of Hibernate)
		public static final DBHintVendorNeutralKey LOCK_TIMEOUT = DBHintVendorNeutralKey.of("LOCK_TIMEOUT");					// Sets the wait time for a pessimistic lock
		public static final DBHintVendorNeutralKey QUERY_TIMEOUT = DBHintVendorNeutralKey.of("QUERY_TIMEOUT");				// Sets the timeout for a query
		public static final DBHintVendorNeutralKey CACHE_STORE_MODE = DBHintVendorNeutralKey.of("CACHE_STORE_MODE");			// Sets how the cache is used for the query
		public static final DBHintVendorNeutralKey CACHE_RETRIEVE_MODE = DBHintVendorNeutralKey.of("CACHE_RETRIEVE_MODE");	// Sets how the cache is used for the query

		public static DBHintVendorNeutralKey of(final String key) {
			return new DBHintVendorNeutralKey(key);
		}

		@Override
		public String asString() {
			return this.id;
		}
	}
}

