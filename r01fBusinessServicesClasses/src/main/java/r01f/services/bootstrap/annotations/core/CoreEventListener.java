package r01f.services.bootstrap.annotations.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CoreEventListener {
	
	// [Important!] 
	// #####################################
	//  1."CoreEventListener" was created to scan Component with methods annotated with "CoreEventListenerSubscriber"
	//  2. Spring equivalent is just the "Component" annotation This is agnostic annotation so the core is free from "Component"  Spring Aplicacion 
	//  3. SpringBoostrap uses  CoreSpringComponentScan to scan anotations as  CoreEventListener
	
}
