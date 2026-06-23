package r01f.services.core.persistence.db.annotations;

import java.lang.annotation.Annotation;

public record TransactionInterceptorForImpl(String value,Class<? extends Annotation> annotationType) 
   implements TransactionInterceptorFor {
	
	public TransactionInterceptorForImpl(final String value) {
		this(value,
			 TransactionInterceptorFor.class);
	}
	
	@Override
	public int hashCode() {
	    // This is specified in java.lang.Annotation.
	    return (127 * "value".hashCode()) ^ this.value().hashCode();
	}
	@Override
	public String toString() {
		return '@' + TransactionInterceptorFor.class.getName() + "(value=\"" + this.value() + "\")";
	}
}