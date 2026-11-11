package r01f.services.core.persistence.db.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.oids.OID;
import r01f.services.core.persistence.db.DBEntityHasOID;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.services.core.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;

@MappedSuperclass
//@Multitenant(MultitenantType.SINGLE_TABLE)
@IdClass(DBPrimaryKeyForModelObjectImpl.class)	// BEWARE!!!!
												// hibernate requires this annotation to be 
												// at the same level of the @Id field(s)
												// (and not in a superclass)
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityBaseWithOID
			  extends DBEntityBase
		   implements DBEntityForModelObject<DBPrimaryKeyForModelObject>,
		   			  DBEntityHasOID {

	private static final long serialVersionUID = -1001565203286537182L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Id @Column(name="OID",length=OID.OID_LENGTH,nullable=false) @Basic
	@Getter @Setter protected String  _oid;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public DBPrimaryKeyForModelObject getDBEntityPrimaryKey() {
		return DBPrimaryKeyForModelObjectImpl.from(_oid);
	}
}