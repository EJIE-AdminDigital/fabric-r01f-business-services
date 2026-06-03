package r01f.services.core.persistence.db.guice.jpa.finder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Marks a method stub as a dynamic finder. The method is intercepted and replaced with the
 * specified JPAQL query. Provides result auto-boxing and automatic parameter binding.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Finder {
  /**
   * Returns the configured named query's name. Specify a named query's name here. This name is
   * typically specified in your JPA configuration.
   */
  String namedQuery() default "";

  /** Returns the configured query string. Directly specify a JPAQL query here. */
  String query() default "";

  /**
   * Returns the configured autoboxing collection class. Use this clause to specify a collection
   * impl to autobox result lists into. The impl must have a default no-arg constructor and be a
   * subclass of {@code java.util.Collection}.
   */
  @SuppressWarnings({"rawtypes"}) // Unavoidable because class literal uses raw types.
  Class<? extends Collection> returnAs() default Collection.class;
}
