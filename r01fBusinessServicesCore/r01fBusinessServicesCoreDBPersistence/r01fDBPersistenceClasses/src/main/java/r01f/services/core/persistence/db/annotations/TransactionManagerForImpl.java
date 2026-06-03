package r01f.services.core.persistence.db.annotations;

import java.lang.annotation.Annotation;

public record TransactionManagerForImpl(String value, Class<? extends Annotation> annotationType) 
   implements TransactionManagerFor {

	public TransactionManagerForImpl(final String value) {
		this(value,
			 TransactionManagerFor.class);
	}
	
	@Override
	public int hashCode() {
	    // This is specified in java.lang.Annotation.
	    return (127 * "value".hashCode()) ^ this.value().hashCode();
	}
	@Override
	public String toString() {
		return '@' + TransactionManagerFor.class.getName() + "(value=\"" + this.value() + "\")";
	}
}