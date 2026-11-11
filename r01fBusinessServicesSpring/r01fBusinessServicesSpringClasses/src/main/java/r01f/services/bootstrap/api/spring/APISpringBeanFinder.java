package r01f.services.bootstrap.api.spring;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;

import jakarta.inject.Provider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.patterns.ThrowingFunction;
import r01f.patterns.ThrowingPredicate;
import r01f.services.interfaces.ExposedServiceInterface;
import r01f.services.interfaces.ServiceInterface;
import r01f.types.JavaPackage;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

@Slf4j
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class APISpringBeanFinder {
/////////////////////////////////////////////////////////////////////////////////////////
//	MARSHALLER
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds the marshaller bean in the spring context
	 * @param springContext
	 * @return
	 */
	public static Marshaller findMarshallerAt(final ApplicationContext springContext){
		Marshaller marshaller =  null;  
		try {
			marshaller = (Marshaller)CollectionUtils.of(springContext.getBeansWithAnnotation(ModelObjectsMarshaller.class))
													.pickOneAndOnlyEntry()
													.getValue();		
		} catch (final Throwable th) {
			log.error(".. bootstrap error on finding available marshaller {}",  
					  th.getLocalizedMessage(),
					  th);
		}
		return marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SECURITY CONTEXT
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Finds the security context provider bean in the spring context
	 * @param springContext
	 * @return
	 */
	public static Provider<SecurityContext> findSecurityContextProviderAt(final ApplicationContext springContext) {			
		ObjectProvider<Provider<SecurityContext>> providerRef = springContext.getBeanProvider(ResolvableType.forClassWithGenerics(Provider.class, 
                    															 											  	   SecurityContext.class));
		return providerRef.getIfAvailable();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SERVICE INTERFACE IMPL FROM BASE SERVICE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
     * <pre class='brush:java'>
	 *   // Sample usage:
	 *   // Find implementations of service interfaces extending MyBaseService
	 *   Map<Class<? extends ServiceInterface>, 
	 *   	 ServiceInterface> implsMap = SpringApplicationContextForAPIHandler.serviceInterfaceCOREImplsFinderAt(springContext)
	 *       															  		.findServiceInterfaceCOREImplsOFBaseServiceInterfaces(MyBaseServiceIface.class);
	 *   // implsMap will contain:
	 *   // {
	 *   //   MyServiceA.class -> MyServiceAImpl instance,
	 *   //   MyServiceB.class -> MyServiceBImpl instance,
	 *   //   MyServiceC.class -> MyServiceCImpl instance
	 *   // }
	 * </pre>
	 * @param springContext
	 * @return
	 */
	public static SpringServiceInterfaceCOREImplFinder serviceInterfaceCOREImplsFinderAt(final ApplicationContext springContext) {
		return new SpringServiceInterfaceCOREImplFinder(springContext);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final static class SpringServiceInterfaceCOREImplFinder {
		private final ApplicationContext _springContext;
		

		/**
		 * Finds CORE implementations of the service interfaces that extend the given base service interfaces
		 * (the same as #findServiceInterfaceCOREImplsOfBaseServiceInterface but for a collection of base service interface)
		 * @param <B>
		 * @param baseServiceInterfaces
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public <B extends ServiceInterface> Map<Class,ServiceInterface> findServiceInterfaceCOREImplsOFBaseServiceInterfaces(final Collection<Class<? extends B>> baseServiceInterfaces) {
			return baseServiceInterfaces.stream()
									    // map every base service interface to its extending service interfaces and their implementations
										// creates a stream of Map<Class<S>,S> for each base service interface 
										// finding at the spring's context where all CORE impls (annotated with @Service) are registered
										.map(baseServiceInterface -> this.<B>findServiceInterfaceCOREImplsOfBaseServiceInterface(baseServiceInterface))
										// flatmaps all those maps into a single map
										// creates an steam of Map.Entry<Class<S>,S>
										.flatMap(map -> map.entrySet()
														   .stream())
										// collects all entries into a single map
										.collect(Collectors.toMap(Map.Entry::getKey, 
																  Map.Entry::getValue));
		}
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public <B extends ServiceInterface> Map<Class,ServiceInterface> findServiceInterfaceCOREImplsOFBaseServiceInterfaces(final Class<? extends B>... baseInterfaces) {
				return findServiceInterfaceCOREImplsOFBaseServiceInterfaces(Lists.newArrayList(baseInterfaces));
		}
		/**
		 * Finds CORE implementations of the service interfaces that extend the given base service interface
		 * @param <B>
		 * @param baseInterface
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public <B extends ServiceInterface> Map<Class,ServiceInterface> findServiceInterfaceCOREImplsOfBaseServiceInterface(final Class<? extends B> baseInterface) {
			// [1] - Find service interfaces extending the given base service interface 
			Collection<Class<? extends B>> extendingServiceInterfaces = SpringServiceInterfaceFinder.using(_springContext.getClassLoader())
					 															   		  			 .findServiceInterfacesExtendingFrom(baseInterface);
			// [2] - Find the core implementations of those service interfaces
			//		 searches into the spring context where all CORE impls (annotated with @Service) are registered
			return _findServiceInterfaceCOREImplsOf(extendingServiceInterfaces);
		}	
		@SuppressWarnings("rawtypes")
		private <S extends ServiceInterface> Map<Class, ServiceInterface> _findServiceInterfaceCOREImplsOf(final Collection<Class<? extends S>> serviceInterface) {
		    return serviceInterface.stream()
		            .collect(Collectors.toMap( Function.<Class>identity(),
		            						   s -> (ServiceInterface) _serviceInterfaceImplOf(s)
		            ));
		}		
		
		private <S extends ServiceInterface> S _serviceInterfaceImplOf(final Class<S> serviceInterface) {
			return _springContext.getBean(serviceInterface);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private final static class SpringServiceInterfaceFinder {
		private final ClassLoader _classLoader;
		
		static SpringServiceInterfaceFinder using(final ClassLoader classLoader) {
			return new SpringServiceInterfaceFinder(classLoader);
		}
		
		/**
		 * in the spring context finds the service interfaces that extend the given base service interface
		 * BEWARE!! only the interfaces annotated with @ExposedServiceInterface are returned (as guice's ServicesClassPathScanner does):
		 *          intermediate BASE service interfaces are NOT annotated and can be implemented by many CORE beans,
		 *          so a single bean could NOT be found for them
		 * @param <B>
		 * @param classLoader
		 * @param baseServiceInterface
		 * @return
		 */
		@SneakyThrows
		@SuppressWarnings({ "unchecked" })
		public <B extends ServiceInterface> Collection<Class<? extends B>> findServiceInterfacesExtendingFrom(final Class<? extends B> baseServiceInterface) {        
	        // [1] Interface service base package
	        JavaPackage baseServiceInterfaceJavaPackage = JavaPackage.of(baseServiceInterface);
	        
	        // [2] Resource patter resolver & reader
	                  
	        // package search path (ie: classpath*:com/mycompany/myapp/services/**/*.class)
	        String packageSearchPath = // classpath*:
	        						   ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
	        						   // com/mycompany/myapp/services/**/*.class
	                                  baseServiceInterfaceJavaPackage.asPath()
	        														   .joinedWith("/**/*.class")
	        														   .asRelativeString();
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(_classLoader);
			
	        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(_classLoader);
	        AssignableTypeFilter typeFilter = new AssignableTypeFilter(baseServiceInterface);
	        
			return Stream.of(resolver.getResources(packageSearchPath))
													// only readable resources (ie: .class files)
													.filter(Resource::isReadable)
													// get the MetaDataReader for each resource
													.map(ThrowingFunction.unchecked(resource -> metadataReaderFactory.getMetadataReader(resource)))
													// with the resource metadata
													// ... filter only those that:
													//			- are interfaces
													//			- are not the base service interface itself 
													//			- that match the type filter (ie: they extend the base service interface)
													//			- are annotated with @ExposedServiceInterface (BASE service interfaces are NOT)
													.filter(metaDataReader ->  metaDataReader.getClassMetadata().isInterface())
													.filter(metaDataReader -> !metaDataReader.getClassMetadata().getClassName().equalsIgnoreCase(baseServiceInterface.getName()))
													.filter(ThrowingPredicate.unchecked(metaDataReader -> typeFilter.match(metaDataReader,metadataReaderFactory)))
													.filter(metaDataReader -> metaDataReader.getAnnotationMetadata().hasAnnotation(ExposedServiceInterface.class.getName()))
													// map to the type definition
													.map(ThrowingFunction.unchecked(metaDataReader -> (Class<B>)Class.forName(metaDataReader.getClassMetadata().getClassName())))
													.collect(Collectors.toList());
		}
	}

}
