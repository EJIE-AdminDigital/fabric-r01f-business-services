package r01f.services.bootstrap.core.eventbus.spring;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class COREEventBusRegistrar 
		implements ImportBeanDefinitionRegistrar {	
/////////////////////////////////////////////////////////////
/// REGISTER COREEventBusSmartInitializingSingleton
/////////////////////////////////////////////////////////////
    @Override
    public void registerBeanDefinitions(final AnnotationMetadata metadata, final BeanDefinitionRegistry registry) {
        @SuppressWarnings("unused")
        
		// Currenttly no attributes, this couald be usefull for future features,
         //AnnotationAttributes attrs = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableCoreEvents.class.getName()));
        // [Registry the singleton]
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(COREEventBusSmartInitializingSingleton.class);
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR); // for the @Inject
        registry.registerBeanDefinition(COREEventBusSmartInitializingSingleton.class.getName(), builder.getBeanDefinition());
    }
}