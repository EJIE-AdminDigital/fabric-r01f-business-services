package r01f.services.bootstrap.core.spring;

import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.MethodParameter;

import r01f.services.bootstrap.annotations.LazyInject;


public class LazyInjectAutowireConfigurator 
		implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    	//
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof org.springframework.beans.factory.support.DefaultListableBeanFactory) {
            org.springframework.beans.factory.support.DefaultListableBeanFactory factory = 
                (org.springframework.beans.factory.support.DefaultListableBeanFactory) beanFactory;
            
            factory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver() {
                @Override
                protected boolean isLazy(DependencyDescriptor descriptor) {
                    MethodParameter methodParam = descriptor.getMethodParameter();
                    
                    if (methodParam != null) {
                        // 1. Revisa si el parámetro individual tiene @LazyInject
                        if (methodParam.hasParameterAnnotation(LazyInject.class)) {
                            return true;
                        }
                        
                        // 2. Revisa si el constructor completo tiene @LazyInject
                        Constructor<?> constructor = methodParam.getConstructor();
                        if (constructor != null && constructor.isAnnotationPresent(LazyInject.class)) {
                            return true;
                        }
                    }
                    
                    return super.isLazy(descriptor);
                }
            });
        }
    }
}