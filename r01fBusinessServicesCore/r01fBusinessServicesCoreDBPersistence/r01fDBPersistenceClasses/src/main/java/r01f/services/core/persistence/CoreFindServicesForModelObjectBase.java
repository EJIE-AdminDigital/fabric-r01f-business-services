package r01f.services.core.persistence;

import java.time.Instant;
import java.util.stream.Stream;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.experimental.Accessors;
import r01f.eventbus.EventBus;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.services.core.ServicesCoreModuleConfig;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;


/**
 * Core service base for persistence services
 *
 * INJECTED STATUS
 * ===============
 * 	The TRANSACTIONAL methods are located at the Services layer (this type);
 * 	Some operations might span multiple @Transactional-annotated methods so it's very important
 *  	for the {@link EntityManager} to have Extended-scope
 * 		see http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/
 * 		 or http://www.javacodegeeks.com/2013/06/jpa-2-entitymanagers-transactions-and-everything-around-it.html
 *
 * 	The {@link EntityManager} should be created at the services layer (this type) and handled to every delegated
 * 	type (crud, search, etc)
 *
 *  	Beware that:
 *  	<ul>
 *  		<li>This type is (at the end) injected in a service-layer that usually is a {@link Singleton} instance</li>
 *  		<li>{@link EntityManager} is NOT usually thread safe and an {@link EntityManagerFactory} should be used if the type is NOT thread safe to create an {@link EntityManager}</li>
 *  		<li>Because this type is (at the end) injected in a {@link Singleton} and it's NOT thread-safe, the {@link EntityManagerFactory} should be used</li>
 *  		<li>WOhen creating an {@link EntityManager} from a {@link EntityManagerFactory} the application is responsible for creation and
 *  			removal of the {@link EntityManager}... so it's an [Application-Managed {@link EntityManager}] and these types of
 *  			managers ALLWAYS have EXTENDED SCOPE (see http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/)</li>
 *  	</ul>
 *  	See
 *  	<ul>
 *  		<li>http://www.javacodegeeks.com/2013/06/jpa-2-entitymanagers-transactions-and-everything-around-it.html</li>
 *  		<li>http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/</li>
 *  	</ul>
 *
 * @param <O>
 * @param <M>
 * @param <FD>
 */
@Accessors(prefix="_")
public abstract class CoreFindServicesForModelObjectBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
			  extends CorePersistenceServicesBase
		   implements FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param modelObjectsMarshaller
	 * @param eventBus
	 * @param entityManagerProvider
	 */
	public CoreFindServicesForModelObjectBase(final ServicesCoreModuleConfig coreCfg,
											  final Marshaller modelObjectsMarshaller,
									   		  final EventBus eventBus,
									   		  final Provider<EntityManager> entityManagerProvider) {
		super(coreCfg,
			  modelObjectsMarshaller,
			  eventBus,
			  entityManagerProvider);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
////////// --- FIND ALL	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findAll(final SecurityContext securityContext) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findAll(securityContext);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findAllAsStream(final SecurityContext securityContext) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findAllAsStream(securityContext);
	}
////////// --- FIND BY CREATE DATE	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreateDate(final SecurityContext securityContext,
											  final Range<Instant> createDate) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByCreateDate(securityContext,
											  createDate);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByCreateDateAsStream(final SecurityContext securityContext,
											  final Range<Instant> createDate) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByCreateDateAsStream(securityContext,
													  createDate);
	}
////////// --- FIND BY LAST UPDATE DATE	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdateDate(final SecurityContext securityContext,
												  final Range<Instant> lastUpdateDate) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByLastUpdateDate(securityContext,
												  lastUpdateDate);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByLastUpdateDateAsStream(final SecurityContext securityContext,
												  final Range<Instant> lastUpdateDate) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByLastUpdateDateAsStream(securityContext,
														  lastUpdateDate);
	}
////////// --- FIND BY CREATOR OID
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
										   final SecurityPrincipalID creatorPrincipalId) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByCreator(securityContext,
										   creatorPrincipalId);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByCreatorAsStream(final SecurityContext securityContext,
										   final SecurityPrincipalID creatorPrincipalId) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByCreatorAsStream(securityContext,
										   	   		 creatorPrincipalId);
	}
////////// --- FIND BY CREATOR ID	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
										   final SecurityPrincipalOID creatorPrincipalOid) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByCreator(securityContext,
										   creatorPrincipalOid);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByCreatorAsStream(final SecurityContext securityContext,
										   final SecurityPrincipalOID creatorPrincipalOid) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByCreatorAsStream(securityContext,
										   	   		 creatorPrincipalOid);
	}
////////// --- FIND BY LAST UPDATER OID	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
											   final SecurityPrincipalID updaterPrincipalId) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByLastUpdater(securityContext,
											   updaterPrincipalId);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByLastUpdaterAsStream(final SecurityContext securityContext,
											   final SecurityPrincipalID updaterPrincipalId) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByLastUpdaterAsStream(securityContext,
											   	   		 updaterPrincipalId);
	}
////////// --- FIND BY LAST UPDATER ID	
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
											   final SecurityPrincipalOID updaterPrincipalOid) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByLastUpdater(securityContext,
											   updaterPrincipalOid);
	}
	@Override @SuppressWarnings("unchecked")
	public Stream<O> findByLastUpdaterAsStream(final SecurityContext securityContext,
											   final SecurityPrincipalOID updaterPrincipalOid) {
		return this.forSecurityContext(securityContext)
						.createDelegateAs(FindServicesForModelObject.class)
							.findByLastUpdaterAsStream(securityContext,
											   	   		 updaterPrincipalOid);
	}
}
