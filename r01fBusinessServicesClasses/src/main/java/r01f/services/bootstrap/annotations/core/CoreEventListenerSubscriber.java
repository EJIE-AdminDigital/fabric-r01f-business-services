package r01f.services.bootstrap.annotations.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import r01f.eventbus.annotations.EventListenerSubscriber;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented @EventListenerSubscriber
public @interface CoreEventListenerSubscriber {
	
	// [CoreEventListenerSubscriber!] 
	// #####################################
	//  1."CoreEventListenerSubscriber" was created for method used as @EventListener spring annotation
	//  2. This is agnostic annotation so the core is free from "@EventListener"  Spring Aplicacion 
	//  3. SringBoostrap uses  CoreSpringComponentScan to scan anotarions as  CoreEventListener
	
}
