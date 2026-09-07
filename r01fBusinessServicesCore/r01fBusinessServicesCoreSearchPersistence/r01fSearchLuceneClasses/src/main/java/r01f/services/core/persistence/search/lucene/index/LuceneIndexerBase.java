package r01f.services.core.persistence.search.lucene.index;

import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

import jakarta.inject.Provider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.enums.EnumWithCode;
import r01f.exceptions.Throwables;
import r01f.facets.HasOID;
import r01f.facets.util.Facetables;
import r01f.guids.OID;
import r01f.guids.OIDBase;
import r01f.guids.OIDForVersionableModelObject;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.model.IndexableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.model.metadata.FieldID;
import r01f.model.metadata.FieldMetaData;
import r01f.model.metadata.HasMetaDataForPersistableModelObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.persistence.index.IndexableFieldValuesExtractor;
import r01f.persistence.index.IndexerBase;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.index.document.IndexDocumentFieldID;
import r01f.persistence.index.document.IndexDocumentFieldValue;
import r01f.persistence.index.document.IndexDocumentFieldValueSet;
import r01f.persistence.index.document.IndexDocumentStandardFieldType;
import r01f.reflection.ReflectionUtils;
import r01f.securitycontext.SecurityContext;
import r01f.servcies.core.persistence.search.lucene.LuceneIndex;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.IsPath;
import r01f.types.Range;
import r01f.types.summary.LangDependentSummary;
import r01f.types.summary.LangIndependentSummary;
import r01f.types.summary.Summary;
import r01f.types.url.Url;
import r01f.util.types.StringConverter;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.datetime.DateTimeMarshaller;
import r01f.util.types.locale.Languages;

/**
 * Base Lucene indexer that encapsulates the logic described at {@link LuceneDocumentFactoryForIndexableObject} to create a lucene's {@link Document}
 * from a model object
 * @param <P>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class LuceneIndexerBase<P extends IndexableModelObject>
		      extends IndexerBase<P> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FINAL STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Fields config
	 */
	@Getter(AccessLevel.PROTECTED) private final IndexDocumentFieldConfigSet<P> _fieldsConfigSet;
	/**
	 * Lucene index to search against
	 */
	@Getter(AccessLevel.PROTECTED) private final LuceneIndex _luceneIndex;

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param modelObjectType
	 * @pram modelObjectTypeMetaData
	 * @param fieldsConfigSet
	 * @param luceneIndex
	 * @param indexableFieldValuesExtractorProvider the IndexableFieldValuesExtractor is NOT thread safe BUT this indexer is a singleton
	 * 											    ... so a new instance of the extractor MUST be created for every indexing operation
	 * 												... so a Provider is provided
	 */
	public LuceneIndexerBase(final Class<P> modelObjectType,final TypeMetaData<P> modelObjectTypeMetaData,
							 final IndexDocumentFieldConfigSet<P> fieldsConfigSet,
						 	 final LuceneIndex luceneIndex,
							 final Provider<IndexableFieldValuesExtractor<P>> indexableFieldValuesExtractorProvider) {	// IndexableFieldValuesExtractor are NOT thread safe!
		super(modelObjectType,modelObjectTypeMetaData,
			  indexableFieldValuesExtractorProvider);
		_fieldsConfigSet = fieldsConfigSet;
		_luceneIndex = luceneIndex;																						// indexes documents with fields
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	INDEX METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void index(final SecurityContext securityContext,
					  final P modelObject) {
		// Get the document from the record
		Document doc = _createLuceneDocumentFor(securityContext,
												modelObject,
												PersistenceRequestedOperation.CREATE);	// new record
		// Index in Lucene
		_luceneIndex.index(doc);
	}
	@Override
	public void updateIndex(final SecurityContext securityContext,
							final P modelObject) {
		// Get the document from the record
		Document doc = _createLuceneDocumentFor(securityContext,
												modelObject,
												PersistenceRequestedOperation.UPDATE);	// update an existing record

		// update lucene index (it deletes the reg and inserts it again)
		FieldID docIdFieldId = FieldID.from(HasMetaDataForPersistableModelObject.SEARCHABLE_METADATA.DOCID);
		_luceneIndex.reIndex(new Term(docIdFieldId.asString(),
								  	  _luceneDOCIDFieldValueFrom(modelObject)),
							 doc);
	}
	@Override
	public void removeFromIndex(final SecurityContext securityContext,
								final OID oid) {
		if (this.getModelObjectMetaData().hasFacet(HasVersionableFacet.class)) {
			if (!(oid instanceof OIDForVersionableModelObject)) throw new UnsupportedOperationException(Throwables.message("The model object {} is a versionable object, BUT it's oid type does NOT implements {}",
																		   												   this.getModelObjectType(),OIDForVersionableModelObject.class));
			OIDForVersionableModelObject versionableOid = (OIDForVersionableModelObject)oid;
			_removeFromIndex(securityContext,
							 versionableOid.getVersionIndependentOid(),versionableOid.getVersion());
		} else {
			_removeFromIndex(securityContext,
							 oid);
		}
	}
	@SuppressWarnings("unused")
	private void _removeFromIndex(final SecurityContext securityContext,
								  final OID oid) {
		FieldID docIdFieldId = FieldID.from(HasMetaDataForPersistableModelObject.SEARCHABLE_METADATA.DOCID);
		Term idTerm = new Term(docIdFieldId.asString(),
							   _idFor(oid));
		// UN-index
		_luceneIndex.unIndex(idTerm);
	}
	private void _removeFromIndex(final SecurityContext securityContext,
								  final VersionIndependentOID oid,final VersionOID version) {
		if (!this.getModelObjectMetaData().hasFacet(HasVersionableFacet.class)) throw new UnsupportedOperationException(Throwables.message("The model object {} is NOT a versionable object, so removeFromIndex(oid) MUST be used",
																																		  this.getModelObjectType()));
		FieldID docIdFieldId = FieldID.from(HasMetaDataForPersistableModelObject.SEARCHABLE_METADATA.DOCID);
		Term idTerm = new Term(docIdFieldId.asString(),
							   _idFor(oid,version));
		// UN-index
		_luceneIndex.unIndex(idTerm);
	}
	private Document _createLuceneDocumentFor(final SecurityContext securityContext,
											  final P modelObj,
											  final PersistenceRequestedOperation reqOp) {
		// [1] - Extract the field values from the model object
		IndexDocumentFieldValueSet fieldsValues = _extractIndexableFields(securityContext,
																	  	  modelObj,
																	  	  reqOp);
		// [2] - Add the id field (every document has an id field)
		FieldMetaData docIdField = this.getModelObjectMetaData()
											.findFieldByIdOrThrow(HasMetaDataForPersistableModelObject.SEARCHABLE_METADATA.DOCID)
												.asFieldMetaData();
		fieldsValues.add(IndexDocumentFieldValue.forMetaData(docIdField)
									  			.andValue(// anonymous oid type to avoid creating a new oid type just for this
									  					  new OIDBase<String>() {
									  							private static final long serialVersionUID = -5184629874103543060L;
																@Override
									  							public String getId() {
									  								return _luceneDOCIDFieldValueFrom(modelObj);
									  							}
																@Override
																public String asString() {
																	return this.getId();
																}
									  					  }));

		// [3] - Create a fields factory that uses the fields config to create the lucene fields
		LuceneDocumentFactoryForIndexableObject docFactory = LuceneDocumentFactoryForIndexableObject.of(_fieldsConfigSet);	// creates fields using the fieldsConfigSet

		// [4] - Create the fields using the factory
		if (fieldsValues.hasData()) {
			log.debug("Creating a Lucene document for {} with fields: {}",
					  fieldsValues.getFieldIdsAsString(),_fieldsConfigSet.getClass().getSimpleName());

			for (IndexDocumentFieldValue<?> fieldValue : fieldsValues) {
				// If a field's creation fails... continue: create the document with the other fields
				try {
					log.debug("- Document field {}.{} of type {}",
							  _fieldsConfigSet.getClass().getSimpleName(),fieldValue.getIndexableFieldId(),
							  fieldValue.getValue() != null ? fieldValue.getValue().getClass() : "[NO VALUE]");
					_createField(docFactory,
								 fieldValue);
				} catch (Throwable th) {
					log.error("Error {} while creating the field {} > {} of type {}",
							  th.getMessage(),
							  _fieldsConfigSet.getClass().getSimpleName(),fieldValue.getIndexableFieldId(),
							  fieldValue.getValue() != null ? fieldValue.getValue().getClass() : "[NO VALUE]",
							  th);
				}
			}
		}
		// [5] - Create the Document using a factory that uses a
		//		 LuceneFieldConfig template
		return docFactory.createDocument();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ID FIELD VALUE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Every lucene document has an id field whose value is obtained in this method
	 * Usually, the id field value is the model object's oid field value, but this is
	 * NOT always the situation: for example when the model object is a versionable
	 * model object, the id field is the composition of the model object's oid and version
	 * @param modelObj
	 * @return
	 */
	private String _luceneDOCIDFieldValueFrom(final P modelObj) {
		OID oid = Facetables.asFacet(modelObj,HasOID.class)
							.getOid();

		String luceneIdStr = null;
		if (oid instanceof OIDForVersionableModelObject) {
			OIDForVersionableModelObject vOid = (OIDForVersionableModelObject)oid;
			VersionIndependentOID versionIndependentOid = vOid.getVersionIndependentOid();
			VersionOID versionOid = vOid.getVersion();
			if (versionOid != null) {
				luceneIdStr = _idFor(versionIndependentOid,versionOid);
			} else {
				throw new IllegalStateException(Throwables.message("NO version info in an oid instance of type {}",oid.getClass()));
			}
		} else {
			luceneIdStr = _idFor(oid);
		}
		return luceneIdStr;
	}
	private static String _idFor(final OID oid) {
		return oid.asString();
	}
	private static String _idFor(final VersionIndependentOID oid,final VersionOID versionOid) {
		return Strings.customized("{}_{}",
					  			  oid,versionOid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELD CREATION
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	private <T> void _createField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  final IndexDocumentFieldValue<T> indexableFieldValue) {
		FieldMetaData fieldMetaDataConfig = indexableFieldValue.getMetaDataConfig();
		FieldID fieldId = indexableFieldValue.getIndexableFieldId();
		T fieldValue = indexableFieldValue.getValue();

		if (fieldValue == null) return;	// do not waste time

		///////// OID
		if (fieldValue instanceof OID) {
			log.debug("\t-OID field: {}={}",fieldId,fieldValue);
			_createOidField(fieldsFactory,
							(IndexDocumentFieldValue<OID>)indexableFieldValue);
		}
		////////// Java Class
		else if (fieldValue instanceof Class) {
			log.debug("\t-java type field: {}={}",fieldId,fieldValue);
			_createJavaTypeField(fieldsFactory,
								 (IndexDocumentFieldValue<Class<?>>)indexableFieldValue);
		}
		//////// String
		else if (fieldValue instanceof String) {
			log.debug("\t-String field: {}={}",fieldId,fieldValue);
			// Sometimes a String field is indexed as FULL-TEXT...
			if (!indexableFieldValue.dependsOnDynamicDimension()
			 && _fieldsConfigSet.getConfigOrThrowFor(IndexDocumentFieldID.fieldIdOf(fieldId)).getType() == IndexDocumentStandardFieldType.Text) {
				// Full-text field
				_createReaderField(fieldsFactory,
								   IndexDocumentFieldID.fieldIdOf(fieldId),
								   StringConverter.asReader((String)fieldValue));
			} else {
				// Normal String field
				_createStringField(fieldsFactory,
								   (IndexDocumentFieldValue<String>)indexableFieldValue);
			}
		}
		////////// Boolean
		else if (fieldValue instanceof Boolean) {
			log.debug("\t-boolean field: {}={}",fieldId,fieldValue);
			_createBooleanField(fieldsFactory,
								(IndexDocumentFieldValue<Boolean>)indexableFieldValue);
		}
		////////// Numeric
		else if (fieldValue instanceof Integer) {
			log.debug("\t-int field: {}={}",fieldId,fieldValue);
			_createNumberField(fieldsFactory,
							   (IndexDocumentFieldValue<Integer>)indexableFieldValue);
		}
		else if (fieldValue instanceof Long) {
			log.debug("\t-long field: {}={}",fieldId,fieldValue);
			_createNumberField(fieldsFactory,
							   (IndexDocumentFieldValue<Long>)indexableFieldValue);
		}
		else if (fieldValue instanceof Float) {
			log.debug("\t-float field: {}={}",fieldId,fieldValue);
			_createNumberField(fieldsFactory,
							   (IndexDocumentFieldValue<Float>)indexableFieldValue);
		}
		else if (fieldValue instanceof Double) {
			log.debug("\t-double field: {}={}",fieldId,fieldValue);
			_createNumberField(fieldsFactory,
							  (IndexDocumentFieldValue<Double>)indexableFieldValue);
		}
		else if (fieldValue instanceof Short) {
			log.debug("\t-short field: {}={}",fieldId,fieldValue);
			_createNumberField(fieldsFactory,
							   (IndexDocumentFieldValue<Short>)indexableFieldValue);
		}
		else if (fieldValue instanceof Byte) {
			log.debug("\t-byte field: {}={}",fieldId,fieldValue);
			_createNumberField(fieldsFactory,
							   (IndexDocumentFieldValue<Byte>)indexableFieldValue);
		}
		////////// Date Time
		else if (fieldValue instanceof Instant) {
			log.debug("\t-Instant field: {}={}",fieldId,fieldValue);
			_createDateTimeField(fieldsFactory,
								 (IndexDocumentFieldValue<Instant>)indexableFieldValue);
		}
		else if (fieldValue instanceof LocalDate) {
			log.debug("\t-LocalDate field: {}={}",fieldId,fieldValue);
			_createDateTimeField(fieldsFactory,
							 	 (IndexDocumentFieldValue<LocalDate>)indexableFieldValue);
		}
		else if (fieldValue instanceof LocalDateTime) {
			log.debug("\t-LocalDate field: {}={}",fieldId,fieldValue);
			_createDateTimeField(fieldsFactory,
							 	 (IndexDocumentFieldValue<LocalDate>)indexableFieldValue);
		}
		else if (fieldValue instanceof LocalTime) {
			log.debug("\t-LocalTime field: {}={}",fieldId,fieldValue);
			_createDateTimeField(fieldsFactory,
							 	 (IndexDocumentFieldValue<LocalTime>)indexableFieldValue);
		}
		////////// Enum
		else if (fieldValue instanceof Enum) {
			log.debug("\t-Enum field: {}={}",fieldId,((Enum<?>)fieldValue).name());
			_createEnumField(fieldsFactory,
							 (IndexDocumentFieldValue<Enum<? extends Enum<?>>>)indexableFieldValue);
		}
		else if (fieldValue instanceof Range) {
			log.debug("\t-Range field: {}={}",fieldId,((Range<? extends Comparable<?>>)fieldValue).asString());
			_createRangeField(fieldsFactory,
							 (IndexDocumentFieldValue<Range<? extends Comparable<?>>>)indexableFieldValue);
		}
		else if (fieldValue instanceof LanguageTexts) {
			log.debug("\t-lang texts field: {}",fieldId);
			_createLanguageTextsFields(fieldsFactory,
									   (IndexDocumentFieldValue<LanguageTexts>)indexableFieldValue);
		}
		else if (fieldValue instanceof Summary) {
			log.debug("\t-summary field: {}",fieldId);
			_createSummaryField(fieldsFactory,
								(IndexDocumentFieldValue<Summary>)indexableFieldValue);
		}
		else if (fieldValue instanceof IsPath) {
			log.debug("\t-path field: {}",fieldId);
			_createPathField(fieldsFactory,
							 (IndexDocumentFieldValue<IsPath>)indexableFieldValue);
		}
		else if (fieldValue instanceof Url) {
			log.debug("\t-serializedURL: {}",fieldId);
			_createSerializedUrlField(fieldsFactory,
									  (IndexDocumentFieldValue<Url>)indexableFieldValue);
		}
		else if (fieldValue instanceof Collection) {
			log.debug("\t-Multi valued field: {}",fieldId);
			// Transform in multiple _createField(...) calls
			Collection<?> col = (Collection<?>)indexableFieldValue.getValue();
			for (Object colVal : col) {
				if (colVal == null) continue;
				// A recursive call with the same metadata id (multiple value)
				// BUT do not check the type when creating the field value since it's NOT a collection
				_createField(fieldsFactory,
							 IndexDocumentFieldValue.forMetaDataNotCheckingType(fieldMetaDataConfig)
							 						.andValue(colVal));		// recursive call
			}
		}
		else if (fieldValue instanceof CanBeRepresentedAsString) {		// DO NOT MOVE!!!!
			log.debug("\t-CanBeRepresentedAsString: {}",fieldId);
			_createCanBeRepresentedAsStringField(fieldsFactory,
												 (IndexDocumentFieldValue<CanBeRepresentedAsString>)indexableFieldValue);
		}
		else if (fieldValue instanceof Map) {
			log.debug("\t-Dimension dependent field: {}",fieldId);
			// Transform in multiple _createField(...) calls
			Map<?,?> fieldValues = (Map<?,?>)indexableFieldValue.getValue();
			for (Map.Entry<?,?> me : fieldValues.entrySet()) {
				IndexDocumentFieldID luceneFieldId = IndexDocumentFieldID.fieldIdOf(indexableFieldValue.getIndexableFieldId(),
																   					me.getKey());		// the point
				Object pointValue = me.getValue();
				_createField(fieldsFactory,
							 luceneFieldId,
							 pointValue);
			}
		}
		else {
			log.error("The field {} of type {} is NOT indexable in Lucene " +
					 "(beware that if it's a date/time field, ALL dates are stored at the UTC timezone so OffsetDateTime and ZonedDateTime are NOT supported)",
					  fieldId,fieldValue.getClass());
		}
	}
	@SuppressWarnings("unchecked")
	private static <T> void _createField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 final IndexDocumentFieldID luceneFieldId,
								  		 final T value) {
		///////// String
		if (value instanceof String) {
			_createStringField(fieldsFactory,luceneFieldId,
							   (String)value);
		}
		///////// OID
		else if (value instanceof OID oidVal) {
			_createOidField(fieldsFactory,luceneFieldId,
						    oidVal);
		}
		////////// Boolean
		else if (value instanceof Boolean boolVal) {
			_createBooleanField(fieldsFactory,luceneFieldId,
								boolVal);
		}
		////////// Numeric
		else if (value instanceof Integer intVal) {
			_createNumberField(fieldsFactory,luceneFieldId,
						       intVal);
		} else if (value instanceof Long longVal) {
			_createNumberField(fieldsFactory,luceneFieldId,
						       longVal);
		} else if (value instanceof Double doubleVal) {
			_createNumberField(fieldsFactory,luceneFieldId,
						      doubleVal);
		} else if (value instanceof Float floatVal) {
			_createNumberField(fieldsFactory,luceneFieldId,
						       floatVal);
		}
		////////// DateTime
		else if (value instanceof Instant instantVal) {
			_createDateTimeField(fieldsFactory,luceneFieldId,
						    	 instantVal);
		} else if (value instanceof LocalDateTime ldtVal) {
			_createDateTimeField(fieldsFactory,luceneFieldId,
						    	 ldtVal);
		} else if (value instanceof LocalDate ldVal) {
			_createDateTimeField(fieldsFactory,luceneFieldId,
						    	 ldVal);
		} else if (value instanceof LocalTime ltVal) {
			_createDateTimeField(fieldsFactory,luceneFieldId,
						    	 ltVal);
		}
		////////// Enum
		else if (value instanceof Enum) {
			_createEnumField(fieldsFactory,luceneFieldId,
						     (Enum<?>)value);
		}
		////////// Range
		else if (value instanceof Range) {
			_createRangeField(fieldsFactory,luceneFieldId,
							  (Range<? extends Comparable<?>>)value);
		}
		////////// Path
		else if (value instanceof IsPath) {
			_createPathField(fieldsFactory,luceneFieldId,
							 (IsPath)value);
		}
		////////// URL
		else if (value instanceof Url) {
			_createSerializedUrlField(fieldsFactory,luceneFieldId,
									  (Url)value);
		}
		////////// LanguageTexts
		else if (value instanceof LanguageTexts langTextVal) {
			_createLanguageTextsField(fieldsFactory,luceneFieldId,
									  langTextVal);
		}
		////////// Summary
		else if (value instanceof Summary summaryVal) {
			_createSummaryField(fieldsFactory,luceneFieldId,
								summaryVal);
		}
		////////// CanBeRepresentedAsString
		else if (value instanceof CanBeRepresentedAsString canBeRepresentedAsStringVal) {		// DO NOT MOVE!!!
			_createCanBeRepresentedAsStringField(fieldsFactory,luceneFieldId,
												 canBeRepresentedAsStringVal);
		} else if (value instanceof Collection) {
			// Transform in multiple _createField(...) calls
			Collection<?> col = (Collection<?>)value;
			for (final Object colVal : col) {
				_createField(fieldsFactory,
							 luceneFieldId,
							 colVal);		// recursive call
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CREATE
/////////////////////////////////////////////////////////////////////////////////////////
	////////// OID
	private static void _createOidField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								 		final IndexDocumentFieldValue<OID> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createOidField(fieldsFactory,
													      luceneFieldId,
													      value));
	}
	private static void _createOidField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
									    final IndexDocumentFieldID luceneFieldId,
									    final OID oid) {
		fieldsFactory.createField(luceneFieldId)
					 .setStringValue(oid.asString());
	}
	////////// String
	private static void _createJavaTypeField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										   	 final IndexDocumentFieldValue<Class<?>> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createJavaTypeField(fieldsFactory,
															   luceneFieldId,
															   value));
	}
	private static void _createJavaTypeField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										  	 final IndexDocumentFieldID luceneFieldId,
										   	 final Class<?> javaType) {
		fieldsFactory.createField(luceneFieldId)
					 .setStringValue(javaType.getName());
	}
	////////// String
	private static void _createStringField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										   final IndexDocumentFieldValue<String> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createStringField(fieldsFactory,
														     luceneFieldId,
														     value));
	}
	private static void _createStringField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										   final IndexDocumentFieldID luceneFieldId,
										   final String value) {
		fieldsFactory.createField(luceneFieldId)
					 .setStringValue(value);
	}
	////////// CanBeRepresentedAsString
	private static void _createCanBeRepresentedAsStringField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 		  			 final IndexDocumentFieldValue<CanBeRepresentedAsString> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createCanBeRepresentedAsStringField(fieldsFactory,
								 						   	   				   luceneFieldId,
								 						   	   				   value));
	}
	private static void _createCanBeRepresentedAsStringField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 		  			 final IndexDocumentFieldID luceneFieldId,
								  		 		  			 final CanBeRepresentedAsString value) {
		fieldsFactory.createField(luceneFieldId)
					 .setStringValue(value.asString());
	}
	////////// Reader
	@SuppressWarnings("unused")
	private static void _createReaderField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										   final IndexDocumentFieldValue<Reader> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createReaderField(fieldsFactory,
								 				   			 luceneFieldId,
								 				   			 value));
	}
	private static void _createReaderField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										   final IndexDocumentFieldID luceneFieldId,
										   final Reader value) {
		fieldsFactory.createField(luceneFieldId)
					 .setReaderValue(value);
	}
	////////// Boolean
	private static void _createBooleanField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
									 		final IndexDocumentFieldValue<Boolean> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createBooleanField(fieldsFactory,
														 	  luceneFieldId,
														 	  value));
	}
	private static void _createBooleanField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										    final IndexDocumentFieldID luceneFieldId,
										    final Boolean boolValue) {
		int value = boolValue ? 1 : 0;
		fieldsFactory.createField(luceneFieldId)
					 .setIntValue(value);
	}
	////////// NUMERIC
	private static <N extends Number> void _createNumberField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
									 						  final IndexDocumentFieldValue<N> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createNumberField(fieldsFactory,
															 luceneFieldId,
															 value));
	}
	private static <N extends Number> void _createNumberField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										    				  final IndexDocumentFieldID luceneFieldId,
										    				  final N number) {
		if (number instanceof Integer intVal) {
			fieldsFactory.createField(luceneFieldId)
					     .setIntValue(intVal);
		} else if (number instanceof Long longVal) {
			fieldsFactory.createField(luceneFieldId)
					     .setLongValue(longVal);
		} else if (number instanceof Double doubleVal) {
			fieldsFactory.createField(luceneFieldId)
					     .setDoubleValue(doubleVal);
		} else if (number instanceof Float floatVal) {
			fieldsFactory.createField(luceneFieldId)
					     .setFloatValue(floatVal);
		} else if (number instanceof Short shortVal) {
			fieldsFactory.createField(luceneFieldId)
					     .setShortValue(shortVal);
		} else if (number instanceof Byte byteVal) {
			fieldsFactory.createField(luceneFieldId)
					     .setByteValue(byteVal);
		} else {
			throw new IllegalArgumentException("Number type " + number.getClass() + " is NOT supported in lucene");
		}
	}
	////////// Date Time
	private static <T extends Temporal> void _createDateTimeField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 					  	  final IndexDocumentFieldValue<T> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createDateTimeField(fieldsFactory,
								 						   	   luceneFieldId,
								 						   	   value));
	}
	private static <T extends Temporal> void _createDateTimeField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										 						  final IndexDocumentFieldID luceneFieldId,
										 						  final T value) {
		if (value instanceof Instant instantVal) {
			fieldsFactory.createField(luceneFieldId)
						 .setLongValue(DateTimeMarshaller.forMarshalling(instantVal)
								 						 .asEPOHMillis());
		} else if (value instanceof LocalDate ldVal) {
			fieldsFactory.createField(luceneFieldId)
						 .setLongValue(DateTimeMarshaller.forMarshalling(ldVal)
								 						 .asEPOHDays());
		} else if (value instanceof LocalDateTime ldtVal) {
			fieldsFactory.createField(luceneFieldId)
						 .setLongValue(DateTimeMarshaller.forMarshalling(ldtVal)
								 						 .asEPOHMillis());
		} else if (value instanceof LocalTime ltVal) {
			fieldsFactory.createField(luceneFieldId)
						 .setLongValue(DateTimeMarshaller.forMarshalling(ltVal)
								 						 .asNanoOfDay());
		} else {
			throw new IllegalArgumentException("Temporal type " + value.getClass() + " is NOT supported in lucene " +
											   "(beware that ALL dates are stored at the UTC timezone so OffsetDateTime and ZonedDateTime are NOT supported)");
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RANGE
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _createRangeField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		  final IndexDocumentFieldValue<Range<? extends Comparable<?>>> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createRangeField(fieldsFactory,
								  							luceneFieldId,
								  							value));
	}
	private static void _createRangeField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										  final IndexDocumentFieldID luceneFieldId,
										  final Range<?> range) {

		/*
		 *   [javac] /softbase_ejie/aplic/r01fb/tmp/compileLib/r01fbPersistenceClasses/src/r01f/persistence/index/lucene/LuceneIndexerBase.java:606: inconvertible types
		 *   [javac] found   : r01f.types.Range<T>
		 *   [javac] required: r01f.types.Range<java.util.Date>
		 *   [javac] 			Range<Date> typedRange = (Range<Date>)range;
		 *   [javac] 			                                      ^
		 */
		if (ReflectionUtils.isImplementing(range.getDataType(),Temporal.class)) {
			if (range.getDataType() == Instant.class) {
				_createDateTimeRange(fieldsFactory,
									 luceneFieldId,
									 (Range<Instant>)range);
			} else if (range.getDataType() == LocalDateTime.class) {
				_createDateTimeRange(fieldsFactory,
									 luceneFieldId,
									 (Range<LocalDateTime>)range);
			} else if (range.getDataType() == LocalDate.class) {
				_createDateTimeRange(fieldsFactory,
									 luceneFieldId,
									 (Range<LocalDate>)range);
			} else if (range.getDataType() == LocalTime.class) {
				_createDateTimeRange(fieldsFactory,
									 luceneFieldId,
									 (Range<LocalTime>)range);
			} else {
				throw new IllegalArgumentException("Temporal type " + range.getDataType() + " is NOT supported in lucene " +
												   "(beware that ALL dates are stored at the UTC timezone so OffsetDateTime and ZonedDateTime are NOT supported)");
			}
		} else if (ReflectionUtils.isImplementing(range.getDataType(),Number.class)) {
			if (range.getDataType() == Integer.class) {
				_createNumberRange(fieldsFactory,
								   luceneFieldId,
								   (Range<Integer>)range);
			} else if (range.getDataType() == Long.class) {
				_createNumberRange(fieldsFactory,
								   luceneFieldId,
								   (Range<Long>)range);
			} else if (range.getDataType() == Double.class) {
				_createNumberRange(fieldsFactory,
								   luceneFieldId,
								   (Range<Double>)range);
			} else if (range.getDataType() == Float.class) {
				_createNumberRange(fieldsFactory,
								   luceneFieldId,
								   (Range<Float>)range);
			} else if (range.getDataType() == Short.class) {
				_createNumberRange(fieldsFactory,
								   luceneFieldId,
								   (Range<Short>)range);
			} else if (range.getDataType() == Byte.class) {
				_createNumberRange(fieldsFactory,
								   luceneFieldId,
								   (Range<Byte>)range);
			} else {
				throw new IllegalArgumentException("Number type " + range.getDataType() + " is NOT supported in lucene");
			}
		} else {
			throw new IllegalArgumentException("Type " + range.getDataType() + " is NOT supported in lucene");
		}
	}
	private static <T extends Temporal & Comparable<? super T>> void _createDateTimeRange(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										  						  						  final IndexDocumentFieldID luceneFieldId,
										  						  						  final Range<T> range) {
		if (range.getLowerBound() != null) _createDateTimeField(fieldsFactory,
												 	 			IndexDocumentFieldID.forId(luceneFieldId.asString() + ".lower"),
												 	 			range.getLowerBound());
		if (range.getUpperBound() != null) _createDateTimeField(fieldsFactory,
												 	 			IndexDocumentFieldID.forId(luceneFieldId.asString() + ".upper"),
												 	 			range.getUpperBound());
	}
	private static <N extends Number & Comparable<? super N>> void _createNumberRange(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										  					  						  final IndexDocumentFieldID luceneFieldId,
										  					  						  final Range<N> range) {
		if (range.getLowerBound() != null) _createNumberField(fieldsFactory,
												 	 			IndexDocumentFieldID.forId(luceneFieldId.asString() + ".lower"),
												 	 			range.getLowerBound());
		if (range.getUpperBound() != null) _createNumberField(fieldsFactory,
												 	 			IndexDocumentFieldID.forId(luceneFieldId.asString() + ".upper"),
												 	 			range.getUpperBound());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Path
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _createPathField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 final IndexDocumentFieldValue<IsPath> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createPathField(fieldsFactory,
								 						   luceneFieldId,
								 						   value));
	}
	private static void _createPathField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										 final IndexDocumentFieldID luceneFieldId,
										 final IsPath path) {
		fieldsFactory.createField(luceneFieldId)
					 .setStringValue(path.asAbsoluteString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Path
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _createSerializedUrlField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 		  final IndexDocumentFieldValue<Url> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createSerializedUrlField(fieldsFactory,
								 		  							luceneFieldId,
								 		  							value));
	}
	private static void _createSerializedUrlField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
										 		  final IndexDocumentFieldID luceneFieldId,
										 		  final Url url) {
		fieldsFactory.createField(luceneFieldId)
					 .setStringValue(url.asString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Enum
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _createEnumField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 final IndexDocumentFieldValue<Enum<? extends Enum<?>>> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createEnumField(fieldsFactory,
								 						   luceneFieldId,
								 						   value));
	}
	private static void _createEnumField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
								  		 final IndexDocumentFieldID luceneFieldId,
								  		 final Enum<? extends Enum<?>> en) {
		if (en instanceof Language) {
			_createStringField(fieldsFactory,
							   luceneFieldId,en.name());
		} else if (en instanceof EnumWithCode) {
			Object code = ((EnumWithCode<?,?>)en).getCode();
			if (code instanceof Number numVal) {
				_createNumberField(fieldsFactory,
								   luceneFieldId,
								   numVal);
			}
			else if (code instanceof Character charVal) {
				_createStringField(fieldsFactory,
								   luceneFieldId,
								   Character.toString((Character)code));
			}
			else if (code instanceof String strVal) {
				_createStringField(fieldsFactory,
								   luceneFieldId,
								   strVal);
			}
			else {
				throw new IllegalStateException(Strings.customized("{} with a {} type code is not supported!!",
																   EnumWithCode.class,code.getClass()));
			}
		} else {
			fieldsFactory.createField(luceneFieldId)
						 .setStringValue(en.name());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LanguageTexts
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _createLanguageTextsFields(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
												   final IndexDocumentFieldValue<LanguageTexts> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createLanguageTextsField(fieldsFactory,
								 		  							luceneFieldId,
								 		  							value));
	}
	private static void _createLanguageTextsField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
									 			  final IndexDocumentFieldID luceneFieldId,
									 			  final LanguageTexts langTexts) {
		// Guess the language from the id
		Language lang = Languages.fromNameOrThrow(IndexDocumentFieldID.dynamicDimensionPointFromFieldId(luceneFieldId));

		_createStringField(fieldsFactory,
						   luceneFieldId,
						   langTexts.get(lang));		// dynamic dimension (language) dependent
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Summary
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _createSummaryField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
									 		final IndexDocumentFieldValue<Summary> indexableField) {
		_process(indexableField,
				 (luceneFieldId,value) -> _createSummaryField(fieldsFactory,
															  luceneFieldId,
															  value));
	}
	private static void _createSummaryField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
									 		final IndexDocumentFieldID luceneFieldId,
									 		final Summary summary) {
		if (summary != null) {
			if (summary.isLangDependent()) {
				LangDependentSummary langDepSummary = summary.asLangDependent();
				log.debug("\t-Lang Dependent Summary field: {} = {}",
						  luceneFieldId,Strings.customized("--Lang dependant summary in {} languages",CollectionUtils.of(langDepSummary.getAvailableLanguages())
																																	   .toStringCommaSeparated()));
				_createLangDependentSummaryFields(fieldsFactory,
												  luceneFieldId,
												  langDepSummary);

			} else if (summary.isLangIndependent()) {
				LangIndependentSummary langIndSummary = summary.asLangIndependent();
				log.debug("\t-Lang Independent Summary field: {} = {}",
						  luceneFieldId,langIndSummary.asString());

				_createLangIndependentSummaryField(fieldsFactory,
												   luceneFieldId,
												   langIndSummary);
			}
		}
	}
	private static void _createLangDependentSummaryFields(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
												   		  final IndexDocumentFieldID luceneFieldId,
												   		  final LangDependentSummary summary) {
		// Guess the language from the id
		Language lang = Languages.fromNameOrThrow(IndexDocumentFieldID.dynamicDimensionPointFromFieldId(luceneFieldId));

		// Field value: note that summaries are only stored but full-text summaries are indexed and not stored
		if (summary.isFullTextSummary()) {
			_createReaderField(fieldsFactory,
							   luceneFieldId,
							   summary.asReader(lang));		// dynamic dimension (language) dependent
		} else {
			_createStringField(fieldsFactory,
							   luceneFieldId,
							   summary.asString(lang));		// dynamic dimension (language) dependent
		}
	}
	private static void _createLangIndependentSummaryField(final LuceneDocumentFactoryForIndexableObject fieldsFactory,
														   final IndexDocumentFieldID luceneFieldId,
														   final LangIndependentSummary summary) {
		// Field value: note that summaries are only stored but full-text summaries are indexed and not stored
		if (summary.isFullTextSummary()) {
			_createReaderField(fieldsFactory,
							   luceneFieldId,
							   summary.asReader());			// NOT dynamic dimension (language) dependent
		} else {
			_createStringField(fieldsFactory,
							   luceneFieldId,
							   summary.asString());			// NOT dynamic dimension (language) dependent
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Process a field value
	 * - If the field value is language dependent creates N fields, one per lang
	 * - If the field value is language independent creates only ONE field
	 * @param indexableField
	 * @param fieldFactory
	 */
	private static <T> void _process(final IndexDocumentFieldValue<T> indexableField,
							  		 final FieldFactory<T> fieldFactory) {
		if (indexableField.dependsOnDynamicDimension()) {
			// Create a field for each dynamic dimension point (ie language) with the id = [fieldId].[lang]
			for (Object point : indexableField.getDynamicDimensionPoints()) {
				IndexDocumentFieldID luceneFieldId = IndexDocumentFieldID.fieldIdOf(indexableField.getIndexableFieldId(),point);
				fieldFactory.create(luceneFieldId,
									indexableField.getValue());
			}
		} else {
			// Create ONLY one field with the id = [fieldId]
			IndexDocumentFieldID luceneFieldId = IndexDocumentFieldID.fieldIdOf(indexableField.getIndexableFieldId());
			fieldFactory.create(luceneFieldId,
								indexableField.getValue());
		}
	}
	@FunctionalInterface
	private static interface FieldFactory<T> {
		public abstract void create(final IndexDocumentFieldID luceneFieldId,
									final T value);
	}
}
