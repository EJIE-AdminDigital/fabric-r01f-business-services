package r01f.services.bootstrap.core.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

import r01f.services.bootstrap.annotations.core.CoreEventListener;
import r01f.services.bootstrap.annotations.core.CoreService;

/**
 * This interface alongside with the @CoreService annotation is used 
 * to make CORE projects spring independent
 * This annotation is used at the spring bootstrap project as a 
 * REPLACEMENT of spring's @ComponentScan
 * It's used THE SAME WAY as @ComponentScan (is just like an alias) 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan // Meta-annotate spring's @ComponentScan
public @interface CoreSpringComponentScan {	

	/**
	 * Default recursive scan pattern for Spring pacgages and subpackages.
	 */
    String DEFAULT_SCAN_PATTERN = "**/*.class";
    
    /**
     * Non-recursive scan pattern ("*.class").
     * Restricts component scanning strictly to the root level of the base package,
     * ignoring any nested subpackages.
     */
    String PACKAGE_LEVEL_SCAN_PATTERN = "*.class";
	
    @AliasFor(annotation = ComponentScan.class, 
    		  attribute = "value")
    String[] value() default {};
 
    @AliasFor(annotation = ComponentScan.class, 
    		  attribute = "basePackages")
    String[] basePackages() default {};
 
    @AliasFor(annotation = ComponentScan.class, 
    		  attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};
 
    @AliasFor(annotation = ComponentScan.class, 
    		  attribute = "includeFilters")
    ComponentScan.Filter[] includeFilters() default {
    	@ComponentScan.Filter(type = FilterType.ANNOTATION, 
    						   classes = { 
								   CoreService.class,
								   CoreEventListener.class
    	                       })
    };
 
    @AliasFor(annotation = ComponentScan.class, 
    		  attribute = "excludeFilters")
    ComponentScan.Filter[] excludeFilters() default {};
 
    @AliasFor(annotation = ComponentScan.class, 
    		  attribute = "useDefaultFilters")
    boolean useDefaultFilters() default true;
 
    @AliasFor(annotation = ComponentScan.class,
    		  attribute = "lazyInit")
    boolean lazyInit() default false;
    
    @AliasFor(annotation = ComponentScan.class, attribute = "resourcePattern")
    String resourcePattern() default CoreSpringComponentScan.DEFAULT_SCAN_PATTERN;
    
}
