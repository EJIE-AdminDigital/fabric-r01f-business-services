package r01f.services.client.servicesproxy.rest;


import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.oids.OID;
import r01f.oids.OIDForVersionableModelObject;
import r01f.oids.SecurityIDs.SecurityPrincipalID;
import r01f.oids.SecurityOIDs.SecurityPrincipalOID;
import r01f.oids.VersionIndependentOID;
import r01f.types.Path;
import r01f.types.Range;
import r01f.types.url.Host;
import r01f.types.url.UrlPath;
import r01f.util.types.Paths;
import r01f.util.types.Strings;
import r01f.util.types.datetime.DateTimeMarshaller;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Interfaces used at {@link RESTServicesProxyBase} and {@link RESTServicesForModelObjectProxyBase} and {@link RESTServicesForVersionableCRUDServicesProxyBase}
 * to build the REST endpoint url
 */
@Slf4j
public class RESTServiceResourceUrlPathBuilders {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Encapsulates the service endpoint url (the host & base path)
	 * which usually is loaded from the properties file
	 * at a standard section as:
	 * <pre class='brush:xml'>
	 * 	<restEndPoints>
	 *		<host>http://localhost:8080/</host>
	 *		<persistenceEndPointBasePath>xxRESTServicesWar</persistenceEndPointBasePath>
	 *	</restEndPoints>
	 * </pre>
	 */
	@Accessors(prefix="_")
	public static class RESTServiceEndPointUrl {
		@Getter private final Host _host;
		@Getter private final UrlPath _endPointBasePath;

		public RESTServiceEndPointUrl(final Host host,final UrlPath endPointBasePath) {
			_host = host;
			_endPointBasePath = endPointBasePath;
		}

		public RESTServiceEndPointUrl(final XMLPropertiesForAppComponent clientProps,
									  final String selector) {
			this(clientProps.propertyAt("client/restEndPoints/host")
					   		.asHost(Host.LOCALHOST),
				 clientProps.propertyAt(Strings.customized("client/restEndPoints/{}EndPointBasePath",selector))
					  		.asUrlPath());

			// warn if the properties could NOT be retrieved
			if (_host == null || _endPointBasePath == null) {
				// the config is NOT valid!
				throw new IllegalStateException(Strings.customized("The provided client properties file DOES NOT contains the REST endpoint section; " +
																   "ensure that it contains the xPaths client/restEndPoints/host and client/restEndPoints/{}EndPointBasePath",
																   selector));
			}
			// ... or log
			else if (log.isTraceEnabled()) {
				log.trace("REST service endpoint for {} at {}: host={}, basePath={}",
					  	  selector,Strings.customized("client/restEndPoints/{}EndPointBasePath",selector),
					  	  _host,_endPointBasePath);
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GENERAL
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface RESTServiceResourceUrlPathBuilder {
		public Host getHost();
		public UrlPath getEndPointBasePath();
		public UrlPath pathOfResource();
	}
	@RequiredArgsConstructor
	public static abstract class RESTServiceResourceUrlPathBuilderBase
					  implements RESTServiceResourceUrlPathBuilder {
		private final Host _host;					// localhost
		private final UrlPath _endPointBasePath;	// xxFooWar
		private final UrlPath _resourcePath;		// bar/baz

		public RESTServiceResourceUrlPathBuilderBase(final RESTServiceEndPointUrl endPointUrl,
													 final UrlPath resourcePath) {
			this(endPointUrl.getHost(),
				 endPointUrl.getEndPointBasePath(),
				 resourcePath);
			if (resourcePath == null) throw new IllegalArgumentException("The REST resource url path is mandatory");
		}

		@Override
		public Host getHost() {
			return _host;
		}
		@Override
		public UrlPath getEndPointBasePath() {
			return _endPointBasePath;
		}
		@Override
		public UrlPath pathOfResource() {
			return UrlPath.from(_resourcePath);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODEL OBJECT
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O extends OID>
					extends RESTServiceResourceUrlPathBuilder {
		public UrlPath pathOfEntity(final O oid);
		public UrlPath pathOfAllEntities(final Path... prePathElements); /*   Sometimes to build a full "path of all entities", some extra info is required */
		public UrlPath pathOfEntityList();
		public UrlPath pathOfEntityListByCreateDate(final Range<Instant> dateRange);
		public UrlPath pathOfEntityListByLastUpdateDate(final Range<Instant> dateRange);
		public UrlPath pathOfEntityListByCreator(final SecurityPrincipalOID creatorPrincipalOid);
		public UrlPath pathOfEntityListByCreator(final SecurityPrincipalID creatorPrincipalId);
		public UrlPath pathOfEntityListByLastUpdater(final SecurityPrincipalOID updaterPrincipalOid);
		public UrlPath pathOfEntityListByLastUpdater(final SecurityPrincipalID updaterPrincipalId);
	}
	public static abstract class RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase<O extends OID>
						 extends RESTServiceResourceUrlPathBuilderBase
			 		  implements RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O> {
		public RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase(final RESTServiceEndPointUrl endPointUrl,
																			  final UrlPath resourcePath) {
			super(endPointUrl,
				  resourcePath);
		}
		public RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase(final Host host,
																			  final UrlPath endPointBasePath,
																			  final UrlPath resourcePath) {
			super(host,
				  endPointBasePath,
				  resourcePath);
		}
		@Override
		public UrlPath pathOfEntity(final O oid) {
			return Paths.forUrlPaths().join(this.pathOfResource(),
					   						oid);
		}
		@Override
		public UrlPath pathOfAllEntities(final Path...prePathElements) {
			return this.pathOfResource();
		}
		@Override
		public UrlPath pathOfEntityList() {
			return Paths.forUrlPaths().join(this.pathOfAllEntities(),
					   						"list");
		}
		@Override
		public UrlPath pathOfEntityListByCreateDate(final Range<Instant> dateRange) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byCreateDate",
					   						dateRange.asString());
		}
		@Override
		public UrlPath pathOfEntityListByLastUpdateDate(final Range<Instant> dateRange) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byLastUpdateDate",
					   						dateRange.asString());
		}
		@Override
		public UrlPath pathOfEntityListByCreator(final SecurityPrincipalOID creatorPrincipalOid) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   					    "byCreatorOid",
					   					    creatorPrincipalOid.asString());
		}
		@Override
		public UrlPath pathOfEntityListByCreator(final SecurityPrincipalID creatorPrincipalId) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   					    "byCreator",
					   					    creatorPrincipalId.asString());
		}
		@Override
		public UrlPath pathOfEntityListByLastUpdater(final SecurityPrincipalOID updaterPrincipalOid) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byLastUpdatorOid",
					   						updaterPrincipalOid.asString());
		}
		@Override
		public UrlPath pathOfEntityListByLastUpdater(final SecurityPrincipalID updaterPrincipalId) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byLastUpdator",
					   						updaterPrincipalId.asString());
		}
	}
	public static abstract class RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase<O extends OIDForVersionableModelObject>
						 extends RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase<O> {
		public RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase(final RESTServiceEndPointUrl endPointUrl,
																			  			 final UrlPath resourcePath) {
			super(endPointUrl,
				  resourcePath);
		}
		public RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase(final Host host,
																						 final UrlPath endPointBasePath,
																		       			 final UrlPath resourcePath) {
			super(host,
				  endPointBasePath,
				  resourcePath);
		}
		@Override
		public UrlPath pathOfEntity(final O oid) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid.getVersionIndependentOid()),			// beware!!
					   						oid.getVersion());
		}
		public UrlPath pathOfVersionIndependent(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfResource(),
					   						oid);
		}
		public UrlPath pathOfAllVersions(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfVersionIndependent(oid),
					   						"versions");
		}
		public UrlPath pathOfWorkVersion(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid),
					   						"workVersion");
		}
		public UrlPath pathOfActiveVersion(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid),
					   						"activeVersion");
		}
		public UrlPath pathOfActiveVersionAt(final VersionIndependentOID oid,final Instant date) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid),
					   						"activeAt",
					   						DateTimeMarshaller.forMarshalling(date)
					   										  .asEPOHMillis());
		}
	}
}
