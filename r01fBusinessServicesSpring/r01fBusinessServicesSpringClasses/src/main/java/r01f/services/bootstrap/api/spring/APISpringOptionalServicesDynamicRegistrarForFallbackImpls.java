package r01f.services.bootstrap.api.spring;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import lombok.extern.slf4j.Slf4j;
import r01f.util.types.collections.Lists;

@Slf4j
public class APISpringOptionalServicesDynamicRegistrarForFallbackImpls 
        implements BeanDefinitionRegistryPostProcessor {

	private final Collection<Class<?>> _baseInterfaces;

    public APISpringOptionalServicesDynamicRegistrarForFallbackImpls(final Class<?>... baseInterfaces) {
        _baseInterfaces = Lists.newArrayList(baseInterfaces);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        // 1. [ Find interfaces at package
        Set<Class<?>> targetInterfaces = _findInterfacesInPackage(_baseInterfaces);

        // 2. RegistryBeanDefinitions with Proxy Fallback if not exists
         /** For example, imagine your interface is PersonService:

				1.If (a) PersonServiceImpl implements PersonService exists:
					Spring says: "I already have a real implementation!". exists becomes true and it does NOT create the proxy.
				
				2. If NO class implements PersonService:
				Spring says: "Nobody created this interface". exists remains false and it creates the emergency Proxy (the Fallback) so the application doesn't crash due to a missing bean.
          */
        for (final Class<?> iface : targetInterfaces) {
            String[] beanNames = registry.getBeanDefinitionNames();
            
            // Verificamos si ya existe alguna definición para esta interfaz
            boolean exists = false;
            for (String name : beanNames) {
                try {
                	// getType(name) asks Spring: "What Java class/type is this Bean?" 
                	//Spring responds by returning the actual class, for example PersonServiceImpl.class
                    Class<?> beanType = ((ConfigurableListableBeanFactory) registry).getType(name);
                    if (beanType != null && iface.isAssignableFrom(beanType)) {
                        exists = true;
                        break;
                    }
                } catch (final Exception ignored) {
                	//
                }
            }
            if (!exists) {
                //log.warn("--> [fallback registrar] Creating Proxy Fallback Bean for inexistent impl: {}", iface.getName());

                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(iface);
                beanDefinition.setInstanceSupplier(() -> _createFallbackProxy(iface));

                String beanName = iface.getSimpleName() + "::FallBackProxy";
                registry.registerBeanDefinition(beanName, beanDefinition);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      //
    }
//////////////////////////////////////////////////////////////////////////////////////////////
///  Find interfaces at package 
/////////////////////////////////////////////////////////////////////////////////////////////
    private static Set<Class<?>> _findInterfacesInPackage(final Collection<Class<?>> baseIfaces) {
        if (baseIfaces == null
        		|| baseIfaces.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> baseIface : baseIfaces) {
            if (baseIface != null) {
                result.addAll(_findInterfacesInPackage(baseIface));
            }
        }
        return result;
    }

    /**
     * Scans classpath to find interfaces extending la interfaz base interface..  
     */
    private static Set<Class<?>> _findInterfacesInPackage(final Class<?> baseIface) {
        Set<Class<?>> result = new HashSet<>();
        result.add(baseIface);

        // Create ClassPathScanningCandidateComponentProvider
        ClassPathScanningCandidateComponentProvider scanner = 
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition beanDefinition) {
                        // Forzar a que acepte Interfaces (Spring por defecto las ignora)
                        return beanDefinition.getMetadata().isInterface();
                    }
                };

        // filter.
        scanner.addIncludeFilter(new AssignableTypeFilter(baseIface));

        String packageName = baseIface.getPackageName();
        var candidates = scanner.findCandidateComponents(packageName);

        for (var candidate : candidates) {
            try {
                Class<?> clazz = Class.forName(candidate.getBeanClassName());
                if (clazz.isInterface()
                				&& baseIface.isAssignableFrom(clazz)) {
                    result.add(clazz);
                }
            } catch (final ClassNotFoundException e) {
                log.error("Could not load class during scan: {}", candidate.getBeanClassName(), e);
            }
        }

        return result;
    }

    /**
     * Factoría para crear la instancia del Proxy dinámico.
     */
    @SuppressWarnings("unchecked")
    private <T> T _createFallbackProxy(Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{ iface },
                (proxy, method, args) -> {
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) return false;
                    if (returnType.isPrimitive()) return 0;
                    return null;
                }
        );
    }
}