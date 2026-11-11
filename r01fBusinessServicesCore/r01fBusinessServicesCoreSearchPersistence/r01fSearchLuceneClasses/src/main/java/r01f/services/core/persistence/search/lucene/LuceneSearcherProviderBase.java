package r01f.services.core.persistence.search.lucene;

import lombok.experimental.Accessors;
import r01f.model.PersistableObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.metadata.TypeMetaDataInspector;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.search.SearcherProviderBase;
import r01f.servcies.core.persistence.search.lucene.LuceneIndex;

@Accessors(prefix="_")
public abstract class LuceneSearcherProviderBase<F extends SearchFilter,I extends SearchResultItem>
  			  extends SearcherProviderBase<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final LuceneIndex _luceneIndex;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected LuceneSearcherProviderBase(final Marshaller marshaller,
										 final LuceneIndex luceneIndex) {
		super(marshaller);
		_luceneIndex = luceneIndex;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked","rawtypes" })
	protected IndexDocumentFieldConfigSet<? extends PersistableObject> getIndexableObjTypeFieldsConfigSet(final Class<? extends PersistableObject> indexableObjectType) {
		IndexDocumentFieldConfigSet<? extends PersistableObject> outFieldsConfigSet = _fieldsConfigSetByIndexableObjType.get(indexableObjectType);
		if (outFieldsConfigSet == null) {
			TypeMetaData<? extends PersistableObject> indexableObjTypeMetadata = TypeMetaDataInspector.singleton()
													  												  .getTypeMetaDataFor(indexableObjectType);
			outFieldsConfigSet = new IndexDocumentFieldConfigSet(indexableObjectType,
															     indexableObjTypeMetadata);
			_fieldsConfigSetByIndexableObjType.put(indexableObjectType,
												   outFieldsConfigSet);
		}
		return outFieldsConfigSet;
	}
}