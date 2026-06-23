package r01f.services.core.persistence.db.annotations;

import java.lang.annotation.Annotation;

public record DataSourceForImpl(String value,Class<? extends Annotation> annotationType)
   implements DataSourceFor {
	
	public DataSourceForImpl(final String value) {
		this(value,
			 DataSourceFor.class);
	}
	
	@Override
	public int hashCode() {
	    // This is specified in java.lang.Annotation.
	    return (127 * "value".hashCode()) ^ this.value().hashCode();
	}
	@Override
	public String toString() {
		return '@' + DataSourceFor.class.getName() + "(value=\"" + this.value() + "\")";
	}
}