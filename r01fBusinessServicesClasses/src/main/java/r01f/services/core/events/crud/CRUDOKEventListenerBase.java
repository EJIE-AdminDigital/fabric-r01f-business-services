package r01f.services.core.events.crud;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.model.ModelObject;
import r01f.model.persistence.CRUDOK;
import r01f.model.persistence.CRUDResult;
import r01f.reflection.ReflectionUtils;
import r01f.services.bootstrap.config.ServicesBootstrapConfig;
import r01f.services.core.events.COREEventListenerBase;
import r01f.services.core.events.COREServiceMethodExecEventListeners.COREServiceMethodExecOKEventListener;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;

/**
 * Listener to {@link COREServiceMethodExecOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * 
 * ========================================================
 * BEWARE!!!!!
 * ========================================================
 * If no event is handled ensure that:
 * [1] - The CRUDOKEventListener is binded at a bootstrap module extending CoreServicesBootstrapGuiceModuleBindsEventListeners
 * 		 <pre class='brush:java'>
 * 	            @Override
 *	            public void bindEventListeners(final Binder binder) {
 *	            	binder.bind(MyCRUDOKEventListener.class)
 *	            		  .asEagerSingleton();	
 *	            }
 * 		 </pre>
 * [2] - The {@link ServicesBootstrapConfig} specifies how to handle events:
 * 		 <pre class='brush:java'>
 *		        ServicesBootstrapConfig bootCfg = ServicesBootstrapConfigBuilder.forClient(clientBootstrapCfg)
 *		        																.ofCoreModules(// core DB persistence
 *		        																			   dbPersistenceCoreBootstrapCfg,
 *		        																			   // UI
 *		        																			   uiBootstrapCfg)
 *		        																// event handling
 *		        																.coreEventsHandledSynchronously()
 *		        																.build();
 * 		 </pre>
 * @param <M>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class CRUDOKEventListenerBase
			  extends COREEventListenerBase<COREServiceMethodExecOKEvent>
           implements COREServiceMethodExecOKEventListener {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOKEventListenerBase(final CRUDOKEventFilter crudOperationOKEventFilter) {
		super(crudOperationOKEventFilter);
	}
	public CRUDOKEventListenerBase(final Class<? extends ModelObject> type) {
		super(_createCRUDOKEventFilterByModelObjectType(type));
	}
	public CRUDOKEventListenerBase(final Class<? extends ModelObject> type,
								   final CRUDOKEventFilter crudOperationOKEventFilter) {
		this(new CRUDOKEventFilter() {			
						CRUDOKEventFilter byTypeFilter = _createCRUDOKEventFilterByModelObjectType(type);
						
						@Override 
						public boolean hasTobeHandled(final COREServiceMethodExecOKEvent opEvent) {
							// a) check if the type matches
							boolean matchesType = byTypeFilter.hasTobeHandled(opEvent);
							if (!matchesType) return false;
							
							// b) try the given filter
							boolean matchesFilter = crudOperationOKEventFilter.hasTobeHandled(opEvent);
							return matchesFilter;
						}
			 });
	}
	private static CRUDOKEventFilter _createCRUDOKEventFilterByModelObjectType(final Class<? extends ModelObject> type) {
		return new CRUDOKEventFilter() {
						@Override @SuppressWarnings("unchecked")
						public boolean hasTobeHandled(final COREServiceMethodExecOKEvent opEvent) {
							// the event refers to the same model object type THIS event handler handles;
							CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
														    					  .as(CRUDResult.class);
							Class<? extends ModelObject> theObjType = crudResult.as(CRUDOK.class).getObjectType();
							boolean isSubClassOf = ReflectionUtils.isSubClassOf(theObjType,type);
							return isSubClassOf;
						}
			  };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create or update operation
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulCreateUpdateOrDelete(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _eventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;

		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded()
				// it's a create, update or delete event
			 && (crudResult.asCRUDOK().hasBeenCreated()
				 ||
				 crudResult.asCRUDOK().hasBeenUpdated()
				 ||
				 crudResult.asCRUDOK().hasBeenDeleted()));
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create or update operation
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulCreateOrUpdate(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _eventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;

		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded()
				// it's a create or update event
			 && (crudResult.asCRUDOK().hasBeenCreated()
				 ||
				 crudResult.asCRUDOK().hasBeenUpdated()));
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulCreate(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _eventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;

		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded()
			 && (crudResult.asCRUDOK().hasBeenCreated()));												// it's a create event
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful update operation
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulUpdate(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _eventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;

		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded()
			 && (crudResult.asCRUDOK().hasBeenUpdated()));								// it's an update event
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful delete operation
	 * @param opEvent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean _isEventForSuccessfulDelete(final COREServiceMethodExecOKEvent opEvent) {
		boolean handle = _eventFilter.hasTobeHandled(opEvent);
		if (!handle) return false;

		CRUDResult<? extends ModelObject> crudResult = opEvent.getAsCOREServiceMethodExecOK()
									    					  .as(CRUDResult.class);
		return (crudResult.hasSucceeded()
			 && (crudResult.asCRUDOK().hasBeenDeleted()));								// it's a delete event
	}
}
