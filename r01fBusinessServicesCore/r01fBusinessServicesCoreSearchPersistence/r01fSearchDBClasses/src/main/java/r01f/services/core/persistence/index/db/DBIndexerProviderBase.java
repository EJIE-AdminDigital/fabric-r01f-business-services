package r01f.services.core.persistence.index.db;

import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import lombok.experimental.Accessors;
import r01f.model.PersistableObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.metadata.TypeMetaDataInspector;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.IndexerProviderBase;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.services.core.persistence.db.config.DBModuleConfig;

@Accessors(prefix="_")
public abstract class DBIndexerProviderBase<M extends PersistableObject>
  			  extends IndexerProviderBase<M> {

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final DBModuleConfig _dbModuleConfig;
	protected final Provider<EntityManager> _entityManagerProvider;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected DBIndexerProviderBase(final Class<M> indexableObjectType,
									final Marshaller marshaller,
									final DBModuleConfig dbModuleConfig,
									final Provider<EntityManager> entityManagerProvider) {
		super(indexableObjectType,
			  marshaller);
		_dbModuleConfig = dbModuleConfig;
		_entityManagerProvider = entityManagerProvider;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("unchecked")
	protected IndexDocumentFieldConfigSet<M> getIndexableObjTypeFieldsConfigSet() {
		IndexDocumentFieldConfigSet<M> outFieldsConfigSet = (IndexDocumentFieldConfigSet<M>)_fieldsConfigSetByIndexableObjType.get(_indexableObjectType);
		if (outFieldsConfigSet == null) {
			TypeMetaData<M> indexableObjTypeMetadata = TypeMetaDataInspector.singleton()
													  			.getTypeMetaDataFor(_indexableObjectType);
			outFieldsConfigSet = new IndexDocumentFieldConfigSet<>(_indexableObjectType,
															       indexableObjTypeMetadata);
			_fieldsConfigSetByIndexableObjType.put(_indexableObjectType,
												   outFieldsConfigSet);
		}
		return outFieldsConfigSet;
	}
}