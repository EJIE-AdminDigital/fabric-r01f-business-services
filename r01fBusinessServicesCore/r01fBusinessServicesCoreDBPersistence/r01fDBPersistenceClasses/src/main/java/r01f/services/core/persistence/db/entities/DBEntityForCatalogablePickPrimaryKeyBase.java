package r01f.services.core.persistence.db.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.services.core.persistence.db.entities.primarykeys.DBEntityPrimaryKey;
import r01f.util.types.Strings;

/**
 * PrimaryKey for any versionable object
 */
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityForCatalogablePickPrimaryKeyBase
           implements DBEntityPrimaryKey {

	private static final long serialVersionUID = 2171063621029281484L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter protected String _targetObjectOid;
	@Getter @Setter protected String _targetObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public DBEntityForCatalogablePickPrimaryKeyBase(final String targetObjectOid,
													final String targetObjectType) {
		_targetObjectOid = targetObjectOid;
		_targetObjectType = targetObjectType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		return Strings.customized("{}:{}",
					  			  _targetObjectType,_targetObjectOid);
	}
}
