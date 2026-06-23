package r01f.services.core.rest.spring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class RESTResponseOnModelObject<M> 
		implements RESTResponsePayload {

	private static final long serialVersionUID = 4418845801293328279L;
/////////////////////////////////////////////////////////////////////////////////////////
//FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter final M _modelObjet;
}
