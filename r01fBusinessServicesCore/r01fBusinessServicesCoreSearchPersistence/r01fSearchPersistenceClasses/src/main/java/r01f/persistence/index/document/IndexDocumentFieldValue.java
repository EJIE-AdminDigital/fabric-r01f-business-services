package r01f.persistence.index.document;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.model.metadata.FieldID;
import r01f.model.metadata.FieldMetaData;
import r01f.oids.OID;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.IsPath;
import r01f.types.Range;
import r01f.types.summary.LangDependentSummary;
import r01f.types.summary.LangIndependentSummary;
import r01f.types.summary.Summary;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
public class IndexDocumentFieldValue<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The metadata id
	 */
	@Getter private FieldMetaData _metaDataConfig;
	/**
	 * @return the metadata field id
	 */
	public FieldID getIndexableFieldId() {
		return _metaDataConfig.getIndexableFieldId();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The value
	 */
	@Getter private T _value;
	/**
	 * Sometimes a metadata has a value in certain "points" of a dimension, for example,
	 * the summary metadata is available in certain languages (es|eu|en|fr|de)
	 * The metadata value for every language is indexed in the SAME document BUT in different
	 * fields
	 *
	 * This field contains the set of point for which there's a value for this metaData
	 * for example:
	 * 		For the metaData "name" there's values for every language (es|eu|en|fr|de)
	 * 		so there exists multiple "name" metaData:
	 * 			name.es
	 * 			name.eu
	 * 			name.en
	 * 			name.fr
	 * 			name.de
	 */
	@Getter private Collection<?> _dynamicDimensionPoints;
	/**
	 * @return true if there's another dimension for the metadata values
	 */
	public boolean dependsOnDynamicDimension() {
		return CollectionUtils.hasData(_dynamicDimensionPoints);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	private IndexDocumentFieldValue(final FieldMetaData fieldMetaDataConfig,
							        final T value,
							        final Collection<?> dynamicDimensionPoints) {
		if (!fieldMetaDataConfig.hasMultipleDimensions()) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a multi-dimensions FIELD as it's supposed to; please check the model object's document definition",fieldMetaDataConfig.getFieldId()));
		_metaDataConfig = fieldMetaDataConfig;
		_value = value;
		_dynamicDimensionPoints = dynamicDimensionPoints;
	}
	private IndexDocumentFieldValue(final FieldMetaData metaDataConfig,
							        final T value) {
		_metaDataConfig = metaDataConfig;
		_value = value;
		_dynamicDimensionPoints = null;
	}
	public static IndexableFieldBuilderStep1 forMetaData(final FieldMetaData metaDataCfg) {
		return new IndexableFieldBuilderStep1(true,		// check dataType
											  metaDataCfg);
	}
	public static IndexableFieldBuilderStep1 forMetaDataNotCheckingType(final FieldMetaData metaDataCfg) {
		return new IndexableFieldBuilderStep1(false,		// do not check dataType
											  metaDataCfg);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class IndexableFieldBuilderStep1 {
		private final boolean _checkType;
		private final FieldMetaData _metaDataCfg;

		////////// String
		public IndexDocumentFieldValue<String> andValue(final String strValue) {
			Preconditions.checkArgument(Strings.isNOTNullOrEmpty(strValue),"The input string must NOT be null or empty");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(strValue);		// check the data type
			return new IndexDocumentFieldValue<String>(_metaDataCfg,
											           strValue);
		}
		/////////// Boolean
		public IndexDocumentFieldValue<Boolean> andValue(final Boolean bool) {
			Preconditions.checkArgument(bool != null,"The boolean must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(bool);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											     bool);
		}
		////////// Number
		public <N extends Number> IndexDocumentFieldValue<N> andValue(final N num) {
			Preconditions.checkArgument(num != null,"The number must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(num);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											     num);
		}
		/////////// Enum
		public <E extends Enum<E>> IndexDocumentFieldValue<E> andValue(final E en) {
			Preconditions.checkArgument(en != null,"The enum value must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(en);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											     en);
		}
		////////// Range
		public <C extends Comparable<C>> IndexDocumentFieldValue<Range<C>> andValue(final Range<C> range) {
			Preconditions.checkArgument(range != null,"The range must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(range);
			return new IndexDocumentFieldValue<>(_metaDataCfg,
												  range);
		}
		////////// Class
		public IndexDocumentFieldValue<Class<?>> andValue(final Class<?> type) {
			Preconditions.checkArgument(type != null,"The type must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(type);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											  	 type);
		}
		////////// Date / Time
		public <T extends Temporal> IndexDocumentFieldValue<T> andValue(final T date) {
			Preconditions.checkArgument(date != null,"The date must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(date);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											     date);
		}
		////////// OID
		public IndexDocumentFieldValue<OID> andValue(final OID oid) {
			Preconditions.checkArgument(oid != null,"The oid must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(oid);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											     oid);
		}
		////////// Language
		public IndexDocumentFieldValue<Language> andValue(final Language lang) {
			Preconditions.checkArgument(lang != null,"The language must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(lang);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											  	 lang);
		}
		/////////// Path
		public <P extends IsPath> IndexDocumentFieldValue<P> andValue(final P path) {
			Preconditions.checkArgument(path != null,"The path must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(path);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											  	 path);
		}
		//////////// Url
		public IndexDocumentFieldValue<Url> andValue(final Url url) {
			Preconditions.checkArgument(url != null,"The url must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(url);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											  	 url);
		}
		////////// LanguageTexts
		public IndexDocumentFieldValue<LanguageTexts> andValue(final LanguageTexts langTexts) {
			Preconditions.checkArgument(langTexts != null,"The language texts must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(langTexts);			// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
											     langTexts,
											     langTexts.getDefinedLanguages());				// the value is dependent on language dimension
		}
		/////////// Summary
		public IndexDocumentFieldValue<Summary> andValue(final Summary summary) {
			Preconditions.checkArgument(summary != null,"The summary must not be null");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(summary);		// check the data type

			IndexDocumentFieldValue<Summary> outSummaryField = null;
			if (summary instanceof LangDependentSummary) {
				LangDependentSummary langDepSum = summary.asLangDependent();
				if (CollectionUtils.isNullOrEmpty(langDepSum.getAvailableLanguages())) throw new IllegalArgumentException("The lang-dependent summary does NOT contain any summary in any language!");
				outSummaryField = new IndexDocumentFieldValue<>(_metaDataCfg,
											  				    summary,
											  				    langDepSum.getAvailableLanguages());	// the value is dependent on language dimension
			} else {
				LangIndependentSummary langIndepSum = summary.asLangIndependent();
				outSummaryField = new IndexDocumentFieldValue<>(_metaDataCfg,
											  				    langIndepSum);
			}
			return outSummaryField;
		}
		/////////// CanBeRepresentedAsString
		public IndexDocumentFieldValue<CanBeRepresentedAsString> andValue(final CanBeRepresentedAsString canBeString) {
			Preconditions.checkArgument(canBeString != null,"The object must not be null");
			return new IndexDocumentFieldValue<>(_metaDataCfg,
												 canBeString);
		}
		/////////// Collection & Map
		public <T> IndexDocumentFieldValue<Collection<T>> andValues(final Collection<T> values) {
			Preconditions.checkArgument(CollectionUtils.hasData(values),"The values collection must not be null or empty");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(values);						// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
												 values);
		}
		public <D,T> IndexDocumentFieldValue<Map<D,T>> andValues(final Map<D,T> valuesByDimensionPoint) {
			Preconditions.checkArgument(CollectionUtils.hasData(valuesByDimensionPoint),"The values Map must not be null or empty");
			if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(valuesByDimensionPoint);		// check the data type
			return new IndexDocumentFieldValue<>(_metaDataCfg,
												 valuesByDimensionPoint,
												 valuesByDimensionPoint.keySet());	// the dimension points are the keys of the map
		}
		@SuppressWarnings("unchecked")
		public <T> IndexDocumentFieldValue<T> andValue(final T value) {
			/////////// String
			if (value instanceof String strVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(strVal);
			}
			////////// Boolean
			else if (value instanceof Boolean boolVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(boolVal);
			}
			////////// Number
			else if (value instanceof Number numVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(numVal);
			}
			////////// Enum
			else if (value instanceof Enum enumVal) {
				Preconditions.checkArgument(value != null,"The enum value must not be null");
				if (_checkType) _metaDataCfg.checkIfIsAcceptableValueOrThrow(value);			// check the data type
				return new IndexDocumentFieldValue<>(_metaDataCfg,
												     value);
			}
			////////// Class
			else if (value instanceof Class clazz) {
				return (IndexDocumentFieldValue<T>)this.andValue(clazz);
			}
			////////// Range
			else if (value instanceof Range rangeVal) {
				return (IndexDocumentFieldValue<T>)this.andValue((Range<? extends Comparable<?>>)value);
			}
			////////// DateTime
			else if (value instanceof Temporal temporalVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(temporalVal);
			}
			////////// Language
			else if (value instanceof Language langVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(langVal);
			}
			////////// OID
			else if (value instanceof OID oidVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(oidVal);
			}
			////////// Path
			else if (value instanceof IsPath isPathVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(isPathVal);
			}
			////////// Url
			else if (value instanceof Url urlVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(urlVal);
			}
			////////// Summary / LanguageTexts
			else if (value instanceof LanguageTexts langTextsVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(langTextsVal);
		    } else if (value instanceof Summary summaryVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(summaryVal);
			}
			////////// CanBeRepresentedAsString (LAST RESORT!... DO NOT MOVE!!)
		    else if (value instanceof CanBeRepresentedAsString canBeStringVal) {
				return (IndexDocumentFieldValue<T>)this.andValue(canBeStringVal);
			}
			////////// Collection & Map
		    else if (value instanceof Collection collectionVal) {
				return this.andValues(collectionVal);
			} else if (value instanceof Map mapVal) {
				return this.andValues(mapVal);
			}
			else {
				throw new IllegalArgumentException(Throwables.message("The indexable field value of type {} is NOT a valid type",value.getClass()));
			}
		}
	}
}
