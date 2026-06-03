package r01f.services.bootstrap.core.web.guice.spring;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.services.bootstrap.guice.ServicesBootstrapGuiceUtil;
import r01f.services.interfaces.ExposedServiceInterface;
import r01f.services.interfaces.ServiceInterface;
import r01f.servlet.spring.SpringRootConfigBootstrapBase;
import r01f.util.types.collections.Lists;


@Slf4j
@Configuration
public abstract class SpringRootConfigBootstrapGuiceBase
			  extends SpringRootConfigBootstrapBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	LIFECYCLE MANAGEMENT INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////	
	public static interface ServiceBootstrapSpringHandler {
		public void startServices();
		public void stopServices();
		public Injector getInjector();
	}

	@Bean (initMethod="startServices" , destroyMethod = "stopServices")
	public ServiceBootstrapSpringHandler bootstrapSpring() {
	   return new ServiceBootstrapSpringHandler() {
			   			Injector  GUICE_INJECTOR =  createInjector();

						@Override
						public void startServices() {
							ServicesBootstrapGuiceUtil.startServices(GUICE_INJECTOR);
						}

						@Override
						public void stopServices() {
							ServicesBootstrapGuiceUtil.stopServices(GUICE_INJECTOR);
						}
						@Override
						public Injector getInjector() {
							return GUICE_INJECTOR;
						}
			 };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject  	//  Inject ServiceBootstrapSpringHandler
	@Bean		//  Expose
	public  GuiceExposedServicesToBeanProcessor exposeGuiceServicesToBeans(final ServiceBootstrapSpringHandler servicesBootstrap) {
	  return new GuiceExposedServicesToBeanProcessor(servicesBootstrap);
	}

	@SuppressWarnings("static-method")
	@Inject						   // Inject ServiceBootstrapSpringHandler
	@Bean @ModelObjectsMarshaller  // Expose as @ModelObjectsMarshaller
	public Marshaller marshaller(final ServiceBootstrapSpringHandler servicesBootstrap) {
		return servicesBootstrap.getInjector()
									.getInstance(Key.get(Marshaller.class,ModelObjectsMarshaller.class));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	GUICE INJECTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	protected Injector createInjector() {
		Injector GUICE_INJECTOR = Guice.createInjector(_createGuiceModules());
		return GUICE_INJECTOR;
	}
	/**
	 * Creates the guice modules to be used by the application
	 * @return
	 */
	protected abstract Iterable<Module> _createGuiceModules();
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public class GuiceExposedServicesToBeanProcessor
   	  implements BeanDefinitionRegistryPostProcessor {

		private final ServiceBootstrapSpringHandler _servicesBootstrap;

	  	public GuiceExposedServicesToBeanProcessor(final ServiceBootstrapSpringHandler servicesBootstrap) {
			_servicesBootstrap = servicesBootstrap;
	  	}
		@Override
		public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
		   List<Class<?>>  exposedClass = _getExposedServicesAsClass();
		   for (Class<?> serviceClass : exposedClass) {
			   BeanDefinitionBuilder beanDefBuilder =  BeanDefinitionBuilder.genericBeanDefinition(serviceClass).setLazyInit(true);
			   registry.registerBeanDefinition(serviceClass.getName(),beanDefBuilder.getBeanDefinition());
		   }
		}
		@Override
		public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
			List<Class<?>>  exposedClass = _getExposedServicesAsClass();
			for (Class<?> serviceClass : exposedClass) {
				log.warn(">> Registred bean: {} ",
						 serviceClass.getName());
			   	beanFactory.registerSingleton(serviceClass.getName(), 
			   								  _servicesBootstrap.getInjector()
			   												    .getInstance(serviceClass));
		   }
		}
		private List<Class<?>> _getExposedServicesAsClass() {
			List<Class<?>> servicesInterfaces = Lists.newArrayList();
			for (Binding<?> b : _servicesBootstrap.getInjector().getBindings().values()) {
				 boolean isAsignable = ServiceInterface.class.isAssignableFrom(b.getKey().getTypeLiteral().getRawType()) ;
				 if (isAsignable) {
					 if (b.getKey()
						  .getTypeLiteral()
						  .getRawType()
						  .isAnnotationPresent(ExposedServiceInterface.class) ) {
						servicesInterfaces.add(ReflectionUtils.typeFromClassName(b.getKey().getTypeLiteral().getRawType().getCanonicalName()));
					 }
				 }
			}
			return servicesInterfaces;
		}
   }
}