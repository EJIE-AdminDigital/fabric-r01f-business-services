package r01f.test.api.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.services.api.ClientAPI;
import r01f.test.api.TestAPIBase;

/**
 *  [Steps to build test a about an API bootrapped by SpringBoot 
 
 	       
          [1]  Define the SpringBoot Fabric[APP]              
		  ###########################################################
		 *      @SpringBootFabricApplication()
				@ComponentScan(basePackageClasses = {	// [1]. API  & CORE OrgConfig Bootstrap
							                                //  -----------------------------------
															DN00APISpringBootstrapConfigurationForOrgConfig.class,	
															DN01CORESpringBootstrapConfigurationForOrgConfig.class ,
															
															// [1]. API  & CORE OrgConfig Bootstrap
							                                //  -----------------------------------
															DN00APISpringBootstrapConfigurationForPerson.class,	
															DN01CORESpringBootstrapConfigurationForPerson.class ,
																									
															
															//[N] Common Bindings										
															DN00APIBootstrapSpringConfigurationCommon.class,
															DN00COREBootstrapSpringConfigurationCommon.class
													    }) // 
				@Slf4j
				public class DN99DENASpringTestForOrgConfigSpringBoot {
       
       
                 [Importan !!!! ] 
                   
                    Define a @Bean for the common test cases of [Spring/Guice ]
                     
        
       				@Bean
					public DN99DENATestPersistenceForOrgAdminGroup test(final DN00ClientAPIForOrgConfig api ) {
					    return new  DN99DENATestPersistenceForOrgAdminGroup(api);	
					}	
					
		   
          [2]  Define the SpringBoot Base Test              
		  ######################################################################################			
				abstract class DN99DENASpringTestForOrgConfigBase
					       extends TestAPISpringBase {
					/////////////////////////////////////////////////////////////////////////////////////////
					//  JUnit
					/////////////////////////////////////////////////////////////////////////////////////////
						@BeforeAll	
						public static void setUpBeforeClass() {
							//
						}
						@AfterAll	
						public static void tearDownAfterClass()  {
							// [99]-Tear things down
							try {
								_tearDownAfterClass();
							} catch(Exception ex) {
								ex.printStackTrace(System.out);
								Throwables.throwUnchecked(ex);
							}
		 }
		 
         [3]  Define the SpringBoot Test about the the bootstrapped SpringBoot Fabric[APP]              
		 #################################################################################	
 *       
 *       
 *       	@SpringBootTest(classes = DN99DENASpringTestForOrgConfigSpringBoot.class)
			@ActiveProfiles("test")
			public class DN99DENASpringTestForOrgConfigSpringBootTest
				 extends DN99DENASpringTestForOrgConfigBase {
 *       
 *       
 * 
 * 
 * 
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class TestAPISpringBase 
		extends TestAPIBase {	
// /////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	@Autowired
	protected  ApplicationContext _context;	 	
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/*public static Marshaller getMarshaller() {
		return GUICE_INJECTOR.getInstance(Key.get(Marshaller.class,
												  ModelObjectsMarshaller.class));
	}*/
	public  <A extends ClientAPI> A getClientApi(final Class<A> apiType) {
		return _context.getBean(apiType);
	}

	public  <O> O getOther(final Class<O> instance) {
		return _context.getBean(instance);
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
	/*public static <A extends ClientAPI> A getClientApiForSystemUser(final Class<A> apiType) {
		/*A api = GUICE_INJECTOR.getProvider(Key.get(apiType,
				                          ClientAPIForSystemUser.class)).get();
		if (api == null) {
			throw new IllegalStateException(Strings.customized("There is NO instance binded as @ClientAPIForSystemUser for api {}.\n" +
															   "Check at client api level (XXXClientBootstrapGuiceModule) " +
															   "...if exists a client api provider for System User ",
															   apiType) );
		}
		return api;*/
	/*}*/

}
