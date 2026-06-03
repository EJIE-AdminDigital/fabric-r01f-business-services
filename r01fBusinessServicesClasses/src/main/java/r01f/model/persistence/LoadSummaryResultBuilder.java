package r01f.model.persistence;

import java.time.Instant;

import com.google.common.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.enums.EnumWithCode;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.model.ModelObject;
import r01f.model.SummarizedModelObject;
import r01f.model.services.COREServiceErrorType;
import r01f.model.services.COREServiceErrorTypes;
import r01f.model.services.COREServiceMethodExecError;
import r01f.patterns.IsBuilder;
import r01f.patterns.IsBuilderStep;
import r01f.securitycontext.SecurityContext;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.datetime.DateTimeMarshaller;
import r01f.validation.ObjectValidationResults.ObjectValidationResultNOK;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class LoadSummaryResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static LoadSummaryResultBuilderEntityStep using(final SecurityContext securityContext) {
		return new LoadSummaryResultBuilder() {/* nothing */}
						.new LoadSummaryResultBuilderEntityStep(securityContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class LoadSummaryResultBuilderEntityStep 
			implements IsBuilderStep {
		private final SecurityContext _securityContext;
		
		public <M extends ModelObject> LoadSummaryResultBuilderOperationStep<M> on(final Class<M> entityType) {
			return new LoadSummaryResultBuilderOperationStep<>(_securityContext,
															   entityType);
		}
		@SuppressWarnings("unchecked")
		public <M extends ModelObject> LoadSummaryResultBuilderOperationStep<M> on(final TypeToken<M> typeRef) {
			return new LoadSummaryResultBuilderOperationStep<>(_securityContext,
															   (Class<M>)typeRef.getRawType());
		}	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class LoadSummaryResultBuilderOperationStep<M extends ModelObject> 
	  implements IsBuilderStep {
		protected final SecurityContext _securityContext;
		protected final Class<M> _entityType;
		
		//  --------- ERROR
		public LoadSummaryResultBuilderForErrorAboutStep<M> badClientRequest(final String msg,final Object... vars) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
															 COREServiceErrorTypes.BAD_CLIENT_REQUEST,
															 Strings.customized(msg,vars));
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																   err);
		}
		public LoadSummaryResultBuilderForErrorStep<M> notLoaded() {
			return new LoadSummaryResultBuilderForErrorStep<>(_securityContext,
													  		  _entityType);	
		}
		// --------- SUCCESS
		public LoadSummaryResultBuilderForOKStep<M> loaded() {
			return new LoadSummaryResultBuilderForOKStep<>(_securityContext,
														   _entityType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SUCCESS
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class LoadSummaryResultBuilderForOKStep<M extends ModelObject>
			 implements IsBuilderStep {

		protected final SecurityContext _securityContext;
		protected final Class<M> _entityType;

		public <S extends SummarizedModelObject<M>> LoadSummaryOK<M> summary(final S summary) {
			LoadSummaryOK<M> outPersistenceOpResult = new LoadSummaryOK<>(_entityType,
											 							  summary);
			return outPersistenceOpResult;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class LoadSummaryResultBuilderForErrorStep<M extends ModelObject> 
			 implements IsBuilderStep {
		protected final SecurityContext _securityContext;
		protected final Class<M> _entityType;

		public LoadSummaryResultBuilderForErrorAboutStep<M> because(final Throwable th) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
							    			   				 th);
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
												 				  err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> because(final COREServiceMethodExecError<?> otherError) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											  				 otherError);
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																   err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> becauseClientCannotConnectToServer(final Url serverUrl) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											   				 PersistenceServiceErrorTypes.CLIENT_CANNOT_CONNECT_SERVER,
							    			   				 Strings.customized("Cannot connect to server at {}",serverUrl));
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																   err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> becauseServerError(final String errData,final Object... vars) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											   				 COREServiceErrorTypes.SERVER_ERROR,
											   				 Strings.customized(errData,vars));
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																   err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> becauseClientError(final COREServiceErrorType errorType,
																			   final String msg,final Object... vars) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											   			     errorType,
											   				 Strings.customized(msg,vars));
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																   err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> becauseClientBadRequest(final String msg,final Object... vars) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											   				 COREServiceErrorTypes.BAD_CLIENT_REQUEST,
											   				 Strings.customized(msg,vars));
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																   err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> becauseClientBadRequest(final ObjectValidationResultNOK<?> valid) {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											   				 COREServiceErrorTypes.BAD_CLIENT_REQUEST,
											   				 valid.getReason());
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
															err);
		}
		public LoadSummaryResultBuilderForErrorAboutStep<M> becauseClientRequestedEntityWasNOTFound() {
			LoadSummaryError<M> err = new LoadSummaryError<>(_entityType,
											   				 PersistenceServiceErrorTypes.ENTITY_NOT_FOUND,
							   				   				 (Throwable)null);
			return new LoadSummaryResultBuilderForErrorAboutStep<>(_securityContext,
																	err);
		}
	}	
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class LoadSummaryResultBuilderForErrorAboutStep<M extends ModelObject> 
	         implements IsBuilderStep {
		protected final SecurityContext _securityContext;
		protected final LoadSummaryError<M> _err;

		public LoadSummaryError<M> build() {
			return _err;
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final String meta,final String value) {
			_err.addTargetEntityIdInfo(meta,value);
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final String meta,final CanBeRepresentedAsString value) {
			_err.addTargetEntityIdInfo(meta,value.asString());
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
		public <O extends OID> LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final O entityOid) {
			if (entityOid  != null) {
				_err.addTargetEntityIdInfo("oid",entityOid.asString());
			}
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final VersionIndependentOID oid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final VersionIndependentOID oid,final VersionOID version) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("version",version.asString());
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																  		  _err);
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final VersionIndependentOID oid,final Instant instant) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("date",DateTimeMarshaller.forMarshalling(instant)
																.asISO8601String());
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> aboutWorkVersion(final VersionIndependentOID oid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("version","workVersion");
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
		public LoadSummaryResultBuilderForErrorExtErrorCodeStep<M> about(final VersionIndependentOID oid,final Object version) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			if (version instanceof Instant instant) {
				_err.addTargetEntityIdInfo("date",DateTimeMarshaller.forMarshalling(instant)
																	.asISO8601String());
			} else if (version instanceof VersionOID) {
				_err.addTargetEntityIdInfo("version",((VersionOID)version).asString());
			} else if (version instanceof String || version == null) {
				_err.addTargetEntityIdInfo("version","workVersion");
			}
			return new LoadSummaryResultBuilderForErrorExtErrorCodeStep<>(_securityContext,
																   		  _err);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
	public static class LoadSummaryResultBuilderForErrorExtErrorCodeStep<M extends ModelObject> 
			 implements IsBuilderStep {
		protected final SecurityContext _securityContext;
		protected final LoadSummaryError<M> _err;

		public LoadSummaryError<M> buildWithExtendedErrorCode(final int extErrCode) {
			COREServiceErrorType type = _err.getErrorType() != null ? _err.getErrorType()
																	: COREServiceErrorTypes.UNKNOWN;
			_err.setErrorType(new COREServiceErrorType(type,extErrCode));
			return _err;
		}
		public <E extends Enum<E> & EnumWithCode<Integer,E>> LoadSummaryError<M> buildWithExtendedErrorCode(final E extErrCode) {
			return this.buildWithExtendedErrorCode(extErrCode.getCode());
		}
		public LoadSummaryError<M> build() {
			return _err;
		}
	}
}