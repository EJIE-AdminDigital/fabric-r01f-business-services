package r01f.services.bootstrap.api.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

import r01f.services.bootstrap.annotations.proxy.ProxyService;

/**
 * Equivalent for CoreSpringComponentScan but for proxy impls
 * This interface alongside with the @ProxyService annotation is used 
 * to make PROXY imps projects spring independent
 * This annotation is used at the spring bootstrap project as a 
 * REPLACEMENT of spring's @ComponentScan
 * It's used THE SAME WAY as @ComponentScan (is just like an alias) 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan // Meta-annotate spring's @ComponentScan
public @interface APIProxySpringComponentScan {
    /**
     * Alias para basePackages.
     */
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
    							   ProxyService.class, // Important the proxy impl must be annotated as @ProxyService.
								 
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
}
