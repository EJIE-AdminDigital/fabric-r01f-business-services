package r01f.services.bootstrap.annotations.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import r01f.services.core.IsCoreService;

/**
 * Annotation used to mark a service as a CORE service, that is, 
 * a service EXTENDING {@link IsCoreService} MUST also annotated with this annotation
 * This is REPLACEMENT of Spring's @Service annotation that makes possible to remove
 * the Spring dependency from the CORE module and thus make it more reusable 
 * and portable to other frameworks
 * 
 * see: [includeFilters] property  at @CoreSpringComponentScan in r01f business service
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CoreService {
	// just a marker annotation used to replace Spring's @Service annotation
}
