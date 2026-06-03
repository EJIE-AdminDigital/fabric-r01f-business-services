package r01f.services.core.rest.spring;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.io.util.StringPersistenceUtils;
import r01f.objectstreamer.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class RESTMessageConverterBase<T>
		   implements GenericHttpMessageConverter<T> {

	@Getter private final Class<?> _mappedType;
	@Getter private final MediaType _mediaType;
	@Getter private final Marshaller _modelObjectsMarshaller;	
/////////////////////////////////////////////////////////////////////////////////////////
//	CAN READ
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canRead( final Class<?> type, final MediaType mediaType) {
		log.warn("... received  type '{}' ,  received mediaType : {} "
				+ ", registred _type :{}, _mediaType: {} ",
				             type, mediaType,
				             _mappedType,_mediaType);
		if (mediaType  == null ) {
			return ReflectionUtils.isImplementingAny(ReflectionUtils.classOfType(type),_mappedType);
		}

		boolean canRead = mediaType.isCompatibleWith(_mediaType)
								&& ReflectionUtils.isImplementingAny(ReflectionUtils.classOfType(type),_mappedType);

		return canRead;
	}
	
	@Override
	public boolean canRead(final Type type, final Class<?> contextClass, final  MediaType mediaType) {
		boolean canReadMediaType = mediaType.isCompatibleWith(_mediaType);
		boolean canreadType = _mappedType.isAssignableFrom(TypeToken.of(type).getRawType());
		log.warn( " canReadMediaType ? {}" ,canReadMediaType );
		log.warn( " canreadType ? {}" ,canreadType );
		return canReadMediaType && canreadType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// CAN WRITE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canWrite(final Type type, final Class<?> contextClass, final  MediaType mediaType) {
		boolean outWriteable = false;	
		try {
			log.warn("... should write  received type '{}'   with received mediaType : {} ??? "
					+ "[ registred _type :{}, _mediaType: {} ]",
					             type, mediaType,
					            _mappedType,_mediaType);

			if (mediaType == null) {
				outWriteable = ReflectionUtils.isImplementingAny(TypeToken.of(type).getRawType(), _mappedType);
		    } else if (mediaType != null && mediaType.isCompatibleWith(_mediaType)
					&& ReflectionUtils.isImplementingAny(ReflectionUtils.classOfType(type),_mappedType)) {
				outWriteable = true;
			}
			log.warn("\n [1] The {} type is {} writeable with {}", type,
																   outWriteable ? "" : "NOT",
																   this.getClass().getName());
		} catch (final Throwable th) {
			th.printStackTrace();
			throw new IllegalStateException(th.getLocalizedMessage());

		}
		return outWriteable;
	}

	@Override
	public boolean canWrite(final Class<?> type,  final MediaType mediaType) {
		boolean outWriteable = false;
		if (mediaType != null && mediaType.isCompatibleWith(_mediaType)
				&& ReflectionUtils.isImplementingAny(type,_mappedType)) {
			outWriteable = true;
		}
		log.warn(" \n [2] The {} type is {} writeable with {}", type.getName(),
															    outWriteable ? "" : "NOT",
															    this.getClass().getName());
		return outWriteable;

	}
	@Override
	public List<MediaType> getSupportedMediaTypes() {
		List<MediaType> supportedTypes = Lists.newArrayList();
		supportedTypes.add(_mediaType);
		return supportedTypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	READ PUBLIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public T read(final Class<? extends T> type, final HttpInputMessage inputMessage) throws IOException {
		return _doRead(type,inputMessage);
	}
	@Override
	public T read( final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage) throws IOException  {
		Class<? extends T> classFromType  =  ReflectionUtils.typeFromClassName(TypeToken.of(type).getRawType().getCanonicalName());
		return _doRead(classFromType,inputMessage);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	WRITE PUBLIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void write(final T object, final MediaType contentType, final HttpOutputMessage outputMessage)  throws IOException {
		 log.warn(".....(1) write so :" , object.getClass().getName());
		_doWrite(object,outputMessage);
	}

	@Override
	public void write(final T object, final Type type, final  MediaType contentType, final  HttpOutputMessage outputMessage)throws IOException {
		 log.warn(".....(2) write so :" , object.getClass().getName());
		_doWrite(object,outputMessage);
	}
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _doWrite(final T object, 
			                final HttpOutputMessage outputMessage) throws IOException {
		String json = null;
		if (object instanceof RESTResponsePayload) {
			RESTResponsePayload responsePayload = (RESTResponsePayload)object;
			if (responsePayload.forModelObject()) {
				@SuppressWarnings("rawtypes")
				RESTResponseOnModelObject restResponseOnModelObject = (RESTResponseOnModelObject )responsePayload;
				json =  this.getModelObjectsMarshaller().forWriting().toJson(restResponseOnModelObject.getModelObjet());
			} else {
				@SuppressWarnings("rawtypes")
				RESTResponseOnCRUDResult restResponseOnCRUDResult = (RESTResponseOnCRUDResult )responsePayload;
				json =  this.getModelObjectsMarshaller().forWriting().toJson(restResponseOnCRUDResult.getCrudOnModelObjet());
			}
		} else  {
			json =  this.getModelObjectsMarshaller().forWriting().toJson(object);
		}
		
		// write
		if (Strings.isNOTNullOrEmpty(json)) {
			outputMessage.getBody().write(json.getBytes());
		} else {
			log.error("Received JSON Error !!");
		}
	}
	protected T _doRead(final Class<? extends T> type,
			            final HttpInputMessage inputMessage) throws IOException {
		log.warn("reading {} type",type.getName());
		// json -> java
		String json = StringPersistenceUtils.load(inputMessage.getBody());
		T outObj = null;
		if (Strings.isNOTNullOrEmpty(json)) {
			outObj = _modelObjectsMarshaller.forReading().fromJson(json,
																   type);
		}
		return outObj;
	}
}
