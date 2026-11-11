package r01f.services.bootstrap.core.eventbus.spring;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import r01f.services.bootstrap.annotations.core.CoreEventListenerSubscriber;
import r01f.services.core.events.COREEventBusEventListener;


@Slf4j
public class COREEventBusSmartInitializingSingleton 
  implements SmartInitializingSingleton {
/////////////////////////////////////////////////////////////////////////////////////////
// Application Context
/////////////////////////////////////////////////////////////////////////////////////////	
    private ApplicationContext _springContext;
/////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
    @Inject
    public COREEventBusSmartInitializingSingleton(final ApplicationContext springContext) {
    	_springContext  = springContext;
    }    
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////	
    @Override
    public void afterSingletonsInstantiated() {
        if (!(_springContext instanceof ConfigurableApplicationContext cfgContext)) return;     
        // (Warm-up delay)[ the event listener will be registered asyncronously 
         //                This has nothing to do with whether the eventBus executes asynchronously or synchronously. Don't get confused!
        CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS)
        				 .execute(() -> {
        									_registerEventListeners(cfgContext);
        				 				});
    }
/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE/POTECTED METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
    private void _registerEventListeners(ConfigurableApplicationContext cfgContext) {
        try {
             log.info(">>> [CORE] Warming up EventBus listeners after startup delay...");            
	            _springContext.getBeansOfType(COREEventBusEventListener.class)
	                          .values()
	                          .forEach(bean -> {		            
	                              for (Method method : bean.getClass().getDeclaredMethods()) {
	                                  if (!method.isAnnotationPresent(CoreEventListenerSubscriber.class)) continue;			                    
	                                  
	                                  cfgContext.addApplicationListener(event -> {
	                                      Object payload = (event instanceof PayloadApplicationEvent<?> payloadAppEvent) 
			                                              ? payloadAppEvent.getPayload() 
			                                              : event;
	                                      if (payload != null
	                                    		  	&& method.getParameterCount() > 0
	                                    		  	&& method.getParameterTypes()[0].isAssignableFrom(payload.getClass())) {
	                                          try {
	                                              method.setAccessible(true);
	                                              method.invoke(bean, payload);
	                                          } catch (final Throwable e) { 
	                                              log.error("Error invoking event listener method: {}", method.getName(), e);
	                                          }
	                                      }
	                                  });
	                                  
	                                  log.warn(">>> [CORE] Linked: {} #{}",
	                                           bean.getClass().getSimpleName(),
	                                           method.getName());
	                              }
	                          });            
            log.info(">>> [CORE] EventBus listeners successfully activated.");            
        } catch (final Throwable e) {
            log.error(">>> [CORE] Error during EventBus warm-up initialization", e);
        }
    }
}
