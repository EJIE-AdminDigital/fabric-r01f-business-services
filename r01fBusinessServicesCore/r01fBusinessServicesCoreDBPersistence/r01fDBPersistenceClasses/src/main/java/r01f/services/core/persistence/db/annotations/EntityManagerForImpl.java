package r01f.services.core.persistence.db.annotations;

import java.lang.annotation.Annotation;

public record EntityManagerForImpl(String value, Class<? extends Annotation> annotationType)
   implements EntityManagerFor {
	
	public EntityManagerForImpl(final String value) {
		this(value,
			 EntityManagerFor.class);
	}
	
	@Override
	public int hashCode() {
	    // This is specified in java.lang.Annotation.
	    return (127 * "value".hashCode()) ^ this.value().hashCode();
	}
	
	@Override
	public String toString() {
		return '@' + EntityManagerFor.class.getName() + "(value=\"" + this.value() + "\")";
	}
}