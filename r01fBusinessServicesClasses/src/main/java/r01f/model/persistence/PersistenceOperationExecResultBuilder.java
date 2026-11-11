package r01f.model.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.model.security.context.SecurityContext;
import r01f.model.services.COREServiceErrorType;
import r01f.model.services.COREServiceErrorTypes;
import r01f.model.services.COREServiceMethod;
import r01f.model.services.COREServiceMethodExecError;
import r01f.patterns.IsBuilder;
import r01f.util.types.Strings;

/**
 * Builder type for {@link PersistenceOperationExecResult}-implementing types:
 * <ul>
 * 		<li>A successful operation execution result: {@link PersistenceOperationExecOK}</li>
 * 		<li>An error on a FIND operation execution: {@link PersistenceOperationExecError}</li>
 * </ul>
 * If the operation execution was successful:
 * <pre class='brush:java'>
 * 		PersistenceOperationExecOK<MyReturnedObjType> opOK = PersistenceOperationExecResultBuilder.using(securityContext)
 * 																	   			   				  .executed("an operation")
 * 																								  .returning(myReturnedObjTypeInstance);
 * </pre>
 * If an error is raised while executing the persistence operation:
 * <pre class='brush:java'>
 * 		PersistenceOperationExecError<MyReturnedObjType> opError = PersistenceOperationExecResultBuilder.using(securityContext)
 *			  																			 		    	.notExecuted("an operation")
 *			  																								.because(error);
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class PersistenceOperationExecResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static PersistenceOperationExecResultBuilderResultStep using(final SecurityContext securityContext) {
		return new PersistenceOperationExecResultBuilder() {/* nothing */ }
						.new PersistenceOperationExecResultBuilderResultStep(securityContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class PersistenceOperationExecResultBuilderResultStep {
		private final SecurityContext _securityContext;
		
		public PersistenceOperationExecResultBuilderReturnedObjStep succeeded(final COREServiceMethod requestedMethod) {
			return new PersistenceOperationExecResultBuilderReturnedObjStep(_securityContext,
																			requestedMethod);
		}
		public PersistenceOperationExecResultBuilderErrorStep failed(final COREServiceMethod requestedMethod) {
			return new PersistenceOperationExecResultBuilderErrorStep(_securityContext,
																	  requestedMethod);
		}
		public PersistenceOperationExecResultBuilderReturnedObjStep succeeded(final PersistenceRequestedOperation requestedMethod) {
			return new PersistenceOperationExecResultBuilderReturnedObjStep(_securityContext,
																			requestedMethod.getCOREServiceMethod());
		}
		public PersistenceOperationExecResultBuilderErrorStep failed(final PersistenceRequestedOperation requestedMethod) {
			return new PersistenceOperationExecResultBuilderErrorStep(_securityContext,
																	  requestedMethod.getCOREServiceMethod());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class PersistenceOperationExecResultBuilderReturnedObjStep {
		protected final SecurityContext _securityContext;
		protected final COREServiceMethod _requestedMethod;
		
		public <T> PersistenceOperationExecOK<T> returning(final T instance) {
			PersistenceOperationExecOK<T> outOpOK = new PersistenceOperationExecOK<>();
			outOpOK.setMethodExecResult(instance);
			return outOpOK;
		}
		
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class PersistenceOperationExecResultBuilderErrorStep {
		protected final SecurityContext _securityContext;
		protected final COREServiceMethod _requestedMethod;
		
		public <T> PersistenceOperationExecError<T> because(final PersistenceOperationExecError<?> other) {
			return other.getError() != null ? this.because(other.getError())
											: this.because(other.getErrorMessage(),other.getErrorType());
		}
		public <T> PersistenceOperationExecError<T> because(final COREServiceMethodExecError<?> other) {
			return this.because(other.getError());
		}
		public <T> PersistenceOperationExecError<T> because(final Throwable th) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<>(_requestedMethod,
																							th);
			return outError;
		}
		public <T> PersistenceOperationExecError<T> because(final String error,
															final COREServiceErrorType errType) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<>(_requestedMethod,
																							errType,
																							error);
			return outError;
		}
		public <T> PersistenceOperationExecError<T> becauseClientBadRequest(final String msg,final Object... vars) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<>(_requestedMethod,
																							COREServiceErrorTypes.BAD_CLIENT_REQUEST,
																							Strings.customized(msg,vars));		// the error message
			return outError;
		}
		public <T,M> PersistenceOperationResult<T> because(final CRUDError<M> crudError) {
			PersistenceOperationExecError<T> outError = new PersistenceOperationExecError<>(_requestedMethod,
																							crudError.getErrorType(),	
																							crudError.getErrorMessage());
			return outError;
		}
	}
}
