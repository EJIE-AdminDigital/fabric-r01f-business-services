package r01f.services.bootstrap.core.eventbus.spring;

import java.lang.reflect.Method;

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
													                         && method.getParameterTypes()[0]
													                        		  .isAssignableFrom(payload.getClass())) {
													                            try {
													                                method.setAccessible(true);
													                                method.invoke(bean, payload);
													                            } catch (final Throwable e) { /* Ignorar errores de invocación */ }
													                        }
													                    });
						                   log.warn(">>> [CORE] Linked: {} #{}",
						                		    bean.getClass().getSimpleName(),
						                		    method.getName());
						            }
	        				 });
    }
}