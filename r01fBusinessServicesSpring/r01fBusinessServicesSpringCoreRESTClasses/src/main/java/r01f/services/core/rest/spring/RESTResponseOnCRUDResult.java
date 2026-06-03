package r01f.services.core.rest.spring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.ModelObject;
import r01f.model.persistence.CRUDResult;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class RESTResponseOnCRUDResult<M extends ModelObject> 
		implements RESTResponsePayload {

	private static final long serialVersionUID = 404487962125728418L;
/////////////////////////////////////////////////////////////////////////////////////////
// FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter final CRUDResult<M> _crudOnModelObjet;
}
