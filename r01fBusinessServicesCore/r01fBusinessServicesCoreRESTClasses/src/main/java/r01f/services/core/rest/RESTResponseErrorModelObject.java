package r01f.services.core.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.model.ModelObject;
import r01f.objectstreamer.annotations.MarshallField;
import r01f.objectstreamer.annotations.MarshallField.MarshallFieldAsXml;

/**
 * Represents a model object error, for example in JSON
 * <pre class='json'>
 *          {
 *          	'code': '1 ',
 *          	'message': 'Message '
 *          }
 * </pre>
 */

@Accessors(prefix="_")
@AllArgsConstructor
public  class RESTResponseErrorModelObject
  implements ModelObject {
	private static final long serialVersionUID = -2984883290553259497L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ATTRIBUTTES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  An error code
	 */
	@MarshallField(as="code",
			       whenXml=@MarshallFieldAsXml(attr=true))
	@Getter @Setter private int _code = 0;

	@MarshallField(as="message")
	@Getter @Setter private String _message ;
/////////////////////////////////////////////////////////////////////////////////////////
// BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static RESTResponseErrorModelObject create (final int errorCode) {
		return  RESTResponseErrorModelObject.create(errorCode,null);
	}
	public static RESTResponseErrorModelObject create (final int errorCode, String messageCode) {
		return new RESTResponseErrorModelObject(errorCode,messageCode);
	}
}