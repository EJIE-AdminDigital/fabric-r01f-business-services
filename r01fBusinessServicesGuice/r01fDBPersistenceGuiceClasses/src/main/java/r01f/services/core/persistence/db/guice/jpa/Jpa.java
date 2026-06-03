package r01f.services.core.persistence.db.guice.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.BindingAnnotation;

/**
 * A binding annotation for internal JPA module properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
@interface Jpa {
}
