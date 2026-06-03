package r01f.services.core.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.guids.OIDs;
import r01f.io.util.StringPersistenceUtils;
import r01f.model.ModelObject;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.rest.RESTRequestBodyTypeMappersForBasicTypes.MarshalledObjectRequestBodyTypeMapper;
import r01f.services.core.persistence.index.IndexManagementCommand;
import r01f.types.jobs.EnqueuedJob;
import r01f.util.types.Strings;

/**
 * Type mappers for user types received as POST payload
 */
@Slf4j
public class RESTRequestBodyTypeMappersForModelObjects {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	public static abstract class ModelObjectRequestBodyTypeMapperBase<M extends ModelObject>
		  	    		 extends MarshalledObjectRequestBodyTypeMapper<M> {

		@Getter private final Marshaller _objectsMarshaller;

		public ModelObjectRequestBodyTypeMapperBase(final Marshaller marshaller) {
			this(marshaller,
				 MediaType.APPLICATION_XML_TYPE,MediaType.APPLICATION_JSON_TYPE);
		}

		public ModelObjectRequestBodyTypeMapperBase(final Marshaller marshaller,
													final MediaType... mediaType) {
			super(ModelObject.class,
				  mediaType);
			_objectsMarshaller = marshaller;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OID
/////////////////////////////////////////////////////////////////////////////////////////
	public static abstract class OIDRequestBodyTypeMapperBase<O extends OID>
		  	 		  implements MessageBodyReader<O> {

		private final Marshaller _marshaller;

		@Inject
		public OIDRequestBodyTypeMapperBase(final Marshaller marshaller) {
			_marshaller = marshaller;
		}

		@Override
		public boolean isReadable(final Class<?> type,final Type genericType,
								  final Annotation[] annotations,
								  final MediaType mediaType) {
			// every application/xml received params are transformed to java in this type
			return (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE) || mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE))
				&& ReflectionUtils.isImplementing(type,OID.class);
		}
		@Override
		public O readFrom(final Class<O> type,final Type genericType,
						  final Annotation[] annotations,
						  final MediaType mediaType,
						  final MultivaluedMap<String,String> httpHeaders,
						  final InputStream entityStream) throws IOException,
							   								     WebApplicationException {
			log.trace("reading {} type",type.getName());
			// xml -> java
			String oidStr = StringPersistenceUtils.load(entityStream).trim();
			O outObj = null;
			if (Strings.isNOTNullOrEmpty(oidStr)) {
				// marshall from xml
				if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)
				 && oidStr.startsWith("<")) {
					outObj = _marshaller.forReading().fromXml(oidStr,
															  type);
				}
				// marshall from json
				else if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)
						&& oidStr.startsWith("{")) {
					outObj = _marshaller.forReading().fromJson(oidStr,
															   type);
				}
				// invoke oid.forId(...) method
				else {
					outObj = OIDs.createOIDFromString(type,
													  oidStr);
				}
			}
			return outObj;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	IndexManagementCommand & EnqueuedJob
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * MessageBodyReader for all {@link IndexManagementCommand}s
	 */
	@Accessors(prefix="_")
	public static abstract class IndexManagementCommandRequestBodyTypeMapperBase
		     			 extends MarshalledObjectRequestBodyTypeMapper<IndexManagementCommand> {

		@Getter private final Marshaller _objectsMarshaller;

		public IndexManagementCommandRequestBodyTypeMapperBase(final Marshaller marshaller) {
			this(marshaller,
				 MediaType.APPLICATION_XML_TYPE,MediaType.APPLICATION_JSON_TYPE);
		}

		public IndexManagementCommandRequestBodyTypeMapperBase(final Marshaller marshaller,
														   	   final MediaType... mediaType) {
			super(IndexManagementCommand.class,
				  mediaType);
			_objectsMarshaller = marshaller;
		}
	}
	/**
	 * MessageBodyReader for all {@link EnqueuedJob}s
	 */
	@Accessors(prefix="_")
	public static abstract class EnqueuedJobRequestBodyTypeMapperBase
		     			 extends MarshalledObjectRequestBodyTypeMapper<EnqueuedJob> {

		@Getter private final Marshaller _objectsMarshaller;

		public EnqueuedJobRequestBodyTypeMapperBase(final Marshaller marshaller) {
			this(marshaller,
				 MediaType.APPLICATION_XML_TYPE,MediaType.APPLICATION_JSON_TYPE);
		}

		public EnqueuedJobRequestBodyTypeMapperBase(final Marshaller marshaller,
													final MediaType... mediaType) {
			super(EnqueuedJob.class,
				  mediaType);
			_objectsMarshaller = marshaller;
		}
	}
}
