package r01f.services.core.rest.spring;

import java.io.IOException;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import jakarta.ws.rs.core.Response;
import r01f.exceptions.Throwables;
import r01f.model.persistence.PersistenceException;
import r01f.model.services.COREServiceException;

/**
 * {@link CoreServiceExceptionConverter}(s) used to convert {@link Exception}s to {@link Response}s
 *
 * <pre>
 * IMPORTANT!	Do NOT forget to include this types at
 * </pre>
 */
public class RESTMessageConverterForException {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public static class CoreServiceExceptionConverter
	          	extends RESTMessageConverterBaseForBasicTypes<COREServiceException> {
		public CoreServiceExceptionConverter() {
			super(PersistenceException.class);
		}
		@Override
		protected void _doWrite(final COREServiceException exception,final HttpOutputMessage outputMessage) throws IOException {
			 outputMessage.getBody().write(Throwables.getStackTraceAsString(exception).getBytes());
		}
		@Override
		protected PersistenceException _doRead(final Class<? extends COREServiceException> type,final HttpInputMessage inputMessage) throws IOException {
			throw new IllegalArgumentException( " A exception should not be read ");
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	THROWABLE
/////////////////////////////////////////////////////////////////////////////////////////	
	public static class ThrowableExceptionConverter
				extends RESTMessageConverterBaseForBasicTypes<Throwable> {

		public ThrowableExceptionConverter() {
			super(Throwable.class);
		}
		@Override
		protected void _doWrite(final Throwable exception,final HttpOutputMessage outputMessage) throws IOException {
			 outputMessage.getBody()
			 			  .write(Throwables.getStackTraceAsString(exception).getBytes());
		}
		@Override
		protected Throwable _doRead(final Class<? extends Throwable> type,final HttpInputMessage inputMessage) throws IOException {
			throw new IllegalArgumentException( " A exception should not be read ");
		}
	}
}
