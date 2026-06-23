package r01f.services.core.persistence.db.guice.jpa.finder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate any dynamic finder method's integer argument with this to pass in the maximum size of
 * returned result window. Useful for paging result sets. Complement of {@link FirstResult}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxResults {}
