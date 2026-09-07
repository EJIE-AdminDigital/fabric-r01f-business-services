package r01f.services.core.events.crud;

import com.google.common.reflect.TypeToken;

import r01f.model.persistence.CRUDOK;
import r01f.model.persistence.PersistencePerformedOperation;
import r01f.model.services.COREServiceMethodExecOK;
import r01f.services.core.events.COREServiceMethodExecEvents.COREServiceMethodExecOKEvent;

@FunctionalInterface
public interface CRUDOKEventFilter {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean hasTobeHandled(COREServiceMethodExecOKEvent opEvent);
	
	public default CRUDOKEventFilter joinedWith(final CRUDOKEventFilter other) {
		return new CRUDOKEventFilter() {
						@Override
						public boolean hasTobeHandled(final COREServiceMethodExecOKEvent opOKEvent) {
							return CRUDOKEventFilter.this.hasTobeHandled(opOKEvent)
									&& other.hasTobeHandled(opOKEvent);
						}
			   		};
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static class CRUDOKEventFilterForSuccessfulCREATION<T>
			 implements CRUDOKEventFilter {
		
		@Override @SuppressWarnings({ "unchecked", "rawtypes" })
		public boolean hasTobeHandled(final COREServiceMethodExecOKEvent opOKEvent) {
			// [1] - Check it's a CRUDOK event 
			COREServiceMethodExecOK<?> opResult = opOKEvent.getAsCOREServiceMethodExecOK();
			if (!(opResult instanceof CRUDOK)) return false;
			
			// [2] - Check the performed operation is one of the supported ones
			CRUDOK<T> crudOK = (CRUDOK<T>)opOKEvent.getAsCOREServiceMethodExecOK()
										 		   .as((TypeToken)new TypeToken<CRUDOK<T>>() { /* nothing */ });
			PersistencePerformedOperation performedOp = crudOK.getPerformedOperation();
			return performedOp.isIn(PersistencePerformedOperation.CREATED);
		}
	}
}
