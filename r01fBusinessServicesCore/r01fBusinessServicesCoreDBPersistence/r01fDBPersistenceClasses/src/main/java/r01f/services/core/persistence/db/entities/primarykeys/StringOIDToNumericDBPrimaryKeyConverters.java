package r01f.services.core.persistence.db.entities.primarykeys;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class StringOIDToNumericDBPrimaryKeyConverters {

    /////////////////////////////////////////////////////////////////////////////
    // Base class for all the converters
    /////////////////////////////////////////////////////////////////////////////
    public static abstract class StringOIDToNumericDBPrimaryKeyConverterBase
                      implements AttributeConverter<String, Number> {

        abstract Number _convertObjectValueToDataValue(final String objectValue);
        abstract String _convertDataValueToObjectValue(final Number dataValue);

        @Override
        public Number convertToDatabaseColumn(final String attribute) {
            if (attribute == null) return null;
            return _convertObjectValueToDataValue(attribute);
        }
        @Override
        public String convertToEntityAttribute(final Number dbData) {
            if (dbData == null) return null;
            return _convertDataValueToObjectValue(dbData);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Specific converters for different numeric types
    /////////////////////////////////////////////////////////////////////////////
    @Converter(autoApply = true)
    public static class StringOIDToIntegerDBPrimaryKeyConverter
                extends StringOIDToNumericDBPrimaryKeyConverterBase {

        @Override
        Integer _convertObjectValueToDataValue(final String objectValue) {
            return Integer.parseInt(objectValue);
        }
        @Override
        String _convertDataValueToObjectValue(final Number dataValue) {
            return Integer.toString(dataValue.intValue());
        }
    }

    @Converter(autoApply = true)
    public static class StringOIDToLongDBPrimaryKeyConverter
                extends StringOIDToNumericDBPrimaryKeyConverterBase {

    	@Override
        Long _convertObjectValueToDataValue(final String objectValue) {
            return Long.parseLong(objectValue);
        }
        @Override
        String _convertDataValueToObjectValue(final Number dataValue) {
            return Long.toString(dataValue.longValue());
        }
    }

    @Converter(autoApply = true)
    public static class StringOIDToDoubleDBPrimaryKeyConverter
                extends StringOIDToNumericDBPrimaryKeyConverterBase {

        @Override
        Double _convertObjectValueToDataValue(final String objectValue) {
            return Double.parseDouble(objectValue);
        }
        @Override
        String _convertDataValueToObjectValue(final Number dataValue) {
            return Double.toString(dataValue.doubleValue());
        }
    }

    @Converter(autoApply = true)
    public static class StringOIDToBigDecimalDBPrimaryKeyConverter
                extends StringOIDToNumericDBPrimaryKeyConverterBase {

        @Override
        BigDecimal _convertObjectValueToDataValue(final String objectValue) {
            return new BigDecimal(objectValue);
        }
        @Override
        String _convertDataValueToObjectValue(final Number dataValue) {
            return dataValue.toString();
        }
    }
}