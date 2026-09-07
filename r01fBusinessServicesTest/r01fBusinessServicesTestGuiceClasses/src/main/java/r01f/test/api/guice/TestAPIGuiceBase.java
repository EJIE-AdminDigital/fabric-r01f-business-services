package r01f.test.api.guice;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.objectstreamer.Marshaller;
import r01f.services.api.ClientAPI;
import r01f.services.bootstrap.annotations.api.ClientAPIForSystemUser;
import r01f.services.bootstrap.guice.ServicesBootstrapGuiceUtil;
import r01f.test.api.TestAPIBase;
import r01f.util.types.Strings;

/**
 * 		JVM argument:
 * 				-DR01_HOME={DEV_HOME}		(ej: d:/develop)
 * 				-javaagent:{DEV_HOME}/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 * 				-DEJIE_PROPERTIES_PATTERN=/default/[entityCode]/[entityCode].properties.xml -DEJIE_PROPERTY_LOADER=classPathLoader
 * 		Add to the classpath:
 * 			- The MySQL / Posgre / Oracle driver
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class TestAPIGuiceBase 
		      extends TestAPIBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected static Injector GUICE_INJECTOR;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static Injector getGuiceInjector() {
		return GUICE_INJECTOR;
	}
	public static Marshaller getMarshaller() {
		return GUICE_INJECTOR.getInstance(Key.get(Marshaller.class,
												  ModelObjectsMarshaller.class));
	}
	public static <A extends ClientAPI> A getClientApi(final Class<A> apiType) {
		return GUICE_INJECTOR.getInstance(apiType);
	}

	public static  <O> O getOther(final Class<O> instance) {
		return GUICE_INJECTOR.getInstance(instance);
	}

	/**
	 * Get a client api binded as @ClientAPIForSystemUser
     * ..... so (IMPORTANT !) requires a ClientAPIForSystemUser Provider at client api bootstrap guice module , fe
     * 	    @Provides					// provides a client api
			@ClientAPIForSystemUser		// for SYSTEM system usage
			@Singleton					// BEWARE!!!
			@SuppressWarnings({ "rawtypes","static-method" })
			private MT01GeoClientAPI _provideMasterClientAPI(@SecurityContextProviderForSystemUser final Provider<SecurityContext> securityContextProvider,
													    	   			   @ModelObjectsMarshaller final Marshaller modelObjectsMarshaller,
													    	  @Named(MT01AppCodes.API_APPCODE_STR) final Map<Class,ServiceInterface> srvcIfaceMappings
	 * @param <A>
	 * @param apiType
	 * @return
	 */
	public static <A extends ClientAPI> A getClientApiForSystemUser(final Class<A> apiType) {
		A api = GUICE_INJECTOR.getProvider(Key.get(apiType,
				                          ClientAPIForSystemUser.class)).get();
		if (api == null) {
			throw new IllegalStateException(Strings.customized("There is NO instance binded as @ClientAPIForSystemUser for api {}.\n" +
															   "Check at client api level (XXXClientBootstrapGuiceModule) " +
															   "...if exists a client api provider for System User ",
															   apiType) );
		}
		return api;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RUN EXACTLY ONCE AT THE VERY BEGINNING OF THE TEST AS A WHOLE
//  (in fact they're run even before the type is constructed -that's why they're static)
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _setUpGuiceModulesBeforeClass(final Module... guiceModules) {
		_setUpGuiceModulesBeforeClass(guiceModules != null ? Arrays.asList(guiceModules) : Collections.emptyList());
	}
	protected static void _setUpGuiceModulesBeforeClass(final Collection<Module> guiceModules) {
		GUICE_INJECTOR = Guice.createInjector(guiceModules);

		// If stand-alone (no app-server is used), init the JPA service or any service that needs to be started
		// like the search engine index
		// 		If the core is available at client classpath, start it
		// 		This is the case where there's no app-server
		// 		(usually the JPA's ServiceHandler is binded at the Guice module extending DBGuiceModuleBase at core side)
		ServicesBootstrapGuiceUtil.startServices(GUICE_INJECTOR);
	}
	protected static void _tearDownAfterClass() {
		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesBootstrapGuiceUtil.stopServices(GUICE_INJECTOR);
	}


}
