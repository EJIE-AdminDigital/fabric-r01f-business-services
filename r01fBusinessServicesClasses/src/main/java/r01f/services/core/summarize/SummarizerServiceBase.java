package r01f.services.core.summarize;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.locale.Language;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizableObjectType;
import r01f.model.SummarizedPersistableObject;
import r01f.oids.PersistableObjectOID;
import r01f.patterns.FactoryFrom;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
public abstract class SummarizerServiceBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
										    S extends SummarizedPersistableObject<O,M>> 
		   implements SummarizerService<O,M,S> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final boolean _enabled = true;
	/**
	 * Get the summarizable object type from the oid 
	 */
	private final FactoryFrom<O,SummarizableObjectType<?>> _summarizableObjectTypeFactory;
	/**
	 * Cache
	 */
	private final Cache<CachedSummarizedObjectKey,S> _cache;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected SummarizerServiceBase(final FactoryFrom<O,SummarizableObjectType<?>> summarizableObjectTypeFactory) {
		_summarizableObjectTypeFactory = summarizableObjectTypeFactory;
		_cache = CacheBuilder.newBuilder()
								.maximumSize(100)
							 .build();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void invalidate(final O objOid) {
		if (!_enabled) return;
		
		log.debug("[summarized objects cache]: invalidating cached summarized object of type {} with oid={}",
				  _summarizableObjectTypeFactory.from(objOid),objOid);
		
		// Find all keys (a single obj might be cached in multiple languages)
		Collection<CachedSummarizedObjectKey> keysToInvalidate = _keysFor(objOid);
		if (CollectionUtils.hasData(keysToInvalidate)) {
			_cache.invalidateAll(keysToInvalidate);
		}
	}
	public S getFromCacheOrNull(final O objOid,final Language lang) {
		if (!_enabled) return null;
		
		CachedSummarizedObjectKey cacheKey = new CachedSummarizedObjectKey(objOid,lang);
		S outSummary = _cache.getIfPresent(cacheKey);
		return outSummary;
	}
	public S getFromCacheOrLoad(final O objOid,final Language lang,
					  			final Callable<S> loader) {
		CachedSummarizedObjectKey cacheKey = new CachedSummarizedObjectKey(objOid,lang);
		S outSummary = null;
		try {
			if (!_enabled) {
				outSummary = loader.call();
			} else {
				outSummary = _cache.get(cacheKey,
										loader);
			}
		} catch(Exception ex) {
			log.error("Error while creating a summary: {}. EXCEPCTION: {}",
					  objOid,
					  ex.getMessage(),
					  ex);
		}
		return outSummary;
	}	
	public void cache(final O objOid,final Language lang,
					  final S summary) {
		if (!_enabled) return;
		
		final CachedSummarizedObjectKey cacheKey = new CachedSummarizedObjectKey(objOid,lang);
		
		log.debug("[summarized objects cache]: caching summarized object of type {} with oid={} in lang={}",
				  _summarizableObjectTypeFactory.from(objOid),objOid,lang);
		
		_cache.put(cacheKey,
				   summary);
	}
	@Override
	public boolean isCached(final O objOid) {
		if (!_enabled) return false;
		
		// Find all keys (a single obj might be cached in multiple languages)
		Collection<CachedSummarizedObjectKey> keysForOid = _keysFor(objOid);
		return CollectionUtils.hasData(keysForOid);
	}
	private Collection<CachedSummarizedObjectKey> _keysFor(final O objOid) {
		Collection<CachedSummarizedObjectKey> keysForOid = _cache.asMap()
																 .keySet()
																 .stream()					
																 .filter(key -> key.getOid().is(objOid))
																 .toList();
		return keysForOid;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CACHE KEY
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	private static record CachedSummarizedObjectKey(PersistableObjectOID oid,
													Language lang)  
	   		  implements Serializable {
		public PersistableObjectOID getOid() {
			return oid;
		}
		public Language getLang() {
			return lang;
		}
	}
}
