package r01f.services.core.persistence.db;

import r01f.model.PersistableModelObjectWithID;
import r01f.oids.PersistableObjectID;
import r01f.oids.PersistableObjectOID;
import r01f.services.interfaces.CRUDServicesForModelObjectWithID;

public interface DBCRUDForModelObjectWithID<O extends PersistableObjectOID,I extends PersistableObjectID<O>,M extends PersistableModelObjectWithID<O,I>>
		 extends DBCRUDForModelObject<O,M>,
		 		 CRUDServicesForModelObjectWithID<O,I,M> {
	// just a marker interface
}
