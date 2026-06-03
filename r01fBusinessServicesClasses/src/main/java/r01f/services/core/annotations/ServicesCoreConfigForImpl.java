package r01f.services.core.annotations;

import java.lang.annotation.Annotation;

public record ServicesCoreConfigForImpl(String value,Class<? extends Annotation> annotationType)
   implements ServicesCoreConfigFor {

	public ServicesCoreConfigForImpl(final String value) {
		this(value,
			 ServicesCoreConfigFor.class);
	}
	@Override
	public int hashCode() {
	    // This is specified in java.lang.Annotation.
	    return (127 * "value".hashCode()) ^ this.value().hashCode();
	}
	
	@Override
	public String toString() {
		return '@' + ServicesCoreConfigFor.class.getName() + "(value=\"" + this.value() + "\")";
	}
}