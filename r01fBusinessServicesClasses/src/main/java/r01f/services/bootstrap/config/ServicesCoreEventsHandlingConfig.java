package r01f.services.bootstrap.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.services.ServicesConfigObject;
import r01f.types.ExecutionMode;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
public class ServicesCoreEventsHandlingConfig 
  implements ServicesConfigObject {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * How are the events executed: async / sync
	 */
	@Getter private final ExecutionMode _executionMode;
	/**
	 * If events are executed asynchronously, this property holds the number of background threads
	 */
	@Getter private final int _numberOfBackgroundThreads;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreEventsHandlingConfig(final ExecutionMode execMode,final int numOfBackgroundThreads) {
		_executionMode = execMode;
		_numberOfBackgroundThreads = numOfBackgroundThreads;
	}
	public ServicesCoreEventsHandlingConfig(final ExecutionMode executionMode) {
		_executionMode = executionMode;
		_numberOfBackgroundThreads = executionMode == ExecutionMode.ASYNC ? 5 : 0;

	}
	public ServicesCoreEventsHandlingConfig(final XMLPropertiesForAppComponent xmlProps) {
		// Get from the properties the way CRUD events are to be consumed: synchronously or asynchronously
		ExecutionMode execMode = xmlProps.propertyAt("services/crudEventsHandling/@mode")
									  	 .asEnumElement(ExecutionMode.class);
		if (execMode == null) {
			log.warn("Events Handling config could NOT be found at {}.{}.properties.xml, please ensure that the {}.{}.properties.xml" +
					 "contains a 'crudEventsHandling' section; meanwhile SYNC event handling is assumed",
					 xmlProps.getAppCode(),xmlProps.getAppComponent(),xmlProps.getAppCode(),xmlProps.getAppComponent());
			execMode = ExecutionMode.SYNC;
		}
		_executionMode = execMode;
		
		if (execMode == ExecutionMode.ASYNC) {
			// create the executer service provider
			_numberOfBackgroundThreads = xmlProps.propertyAt("services/crudEventsHandling/numberOfThreadsInPool")
												 .asInteger(1); 	// single threaded by default
		} else {
			_numberOfBackgroundThreads = 0;
		}
	}
	public static ServicesCoreEventsHandlingConfig asyncEventHandlingUsingThreadPoolOf(final int numberOfBackgroundThreads) {
		return new ServicesCoreEventsHandlingConfig(ExecutionMode.ASYNC,
												  numberOfBackgroundThreads);
	}
	public static ServicesCoreEventsHandlingConfig syncEventHandling() {
		return new ServicesCoreEventsHandlingConfig(ExecutionMode.SYNC);
	}
	public static ServicesCoreEventsHandlingConfig from(final XMLPropertiesForAppComponent xmlProps) {
		return new ServicesCoreEventsHandlingConfig(xmlProps);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("events are handled {} {}",
								  _executionMode == ExecutionMode.ASYNC ? "asynchronously" : "synchronously",
								  _executionMode == ExecutionMode.ASYNC ? "(" + _numberOfBackgroundThreads + " background threads)" : "");
	}
}
