package r01f.services.client.servicesproxy.rest;

import java.time.Instant;

import r01f.model.PersistableModelObject;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.security.context.SecurityContext;
import r01f.objectstreamer.Marshaller;
import r01f.oids.PersistableObjectOID;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilderForModelObjectPersistence;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;
import r01f.types.url.Url;

public abstract class RESTServicesForDBFindProxyBase<O extends PersistableObjectOID,M extends PersistableModelObject<O>>
              extends RESTServicesForModelObjectProxyBase<O,M>
           implements FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Delegate
	 */
	protected final DelegateForRawRESTFind<O,M> _findDelegate;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public <P extends RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O>>
		   RESTServicesForDBFindProxyBase(final Marshaller marshaller,
								   	      final Class<M> modelObjectType,
								   	      final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_findDelegate = new DelegateForRawRESTFind<O,M>(marshaller,
														modelObjectType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsResult<O> findAll(final SecurityContext securityContext) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
															   			  .pathOfEntityList());
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreateDate(final SecurityContext securityContext,
										  	  final Range<Instant> createDate) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntityListByCreateDate(createDate));
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdateDate(final SecurityContext securityContext,
											  	  final Range<Instant> lastUpdateDate) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntityListByLastUpdateDate(lastUpdateDate));
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
	@Override
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
									   	   final SecurityPrincipalOID creatorPrincipalOid) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntityListByCreator(creatorPrincipalOid));
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
	@Override
	public FindOIDsResult<O> findByCreator(final SecurityContext securityContext,
									   	   final SecurityPrincipalID creatorPrincipalId) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntityListByCreator(creatorPrincipalId));
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
										   	   final SecurityPrincipalOID updaterPrincipalOid) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntityListByLastUpdater(updaterPrincipalOid));
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdater(final SecurityContext securityContext,
										   	   final SecurityPrincipalID updaterPrincipalId) {
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
													 .pathOfEntityListByLastUpdater(updaterPrincipalId));
		return _findDelegate.doFindOids(securityContext,
				           				restResourceUrl);
	}
}
