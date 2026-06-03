package r01f.services.core.persistence.search.db;


import java.util.function.Function;

import r01f.locale.Language;
import r01f.model.search.SearchResultItem;
import r01f.securitycontext.SecurityContext;
import r01f.services.core.persistence.db.DBEntity;

/**
 * Base impl of {@link TransformsDBEntityToSearchResultItem}
 * @param <DB>
 * @param <I>
 */
public abstract class TransformsDBEntityToSearchResultItemBase<DB extends DBEntity,I extends SearchResultItem> 
		   implements TransformsDBEntityToSearchResultItem<DB,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns this transformer as a {@link Function}
	 * @param securityContext
	 * @return
	 */
	public Function<DB,I> asTransformFuncion(final SecurityContext securityContext,
											 final Language lang) {
		return dbEntity -> TransformsDBEntityToSearchResultItemBase.this.dbEntityToSearchResultItem(securityContext,
													  												dbEntity,
													  												lang);
	}
}
