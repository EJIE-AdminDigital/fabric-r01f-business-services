package r01f.services.core.persistence.db.guice.jpa.finder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate any dynamic finder method's integer argument with this to pass in the index of the first
 * result in the result set you are interested in. Useful for paging result sets. Complemented by
 * {@link MaxResults}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstResult {}
