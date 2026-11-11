package r01f.services.core.persistence.db.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.NamedQueries;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.oids.OID;

@MappedSuperclass

@NamedQueries({

})

@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityForCatalogablePickBase
	 		  extends DBEntityBase {

	private static final long serialVersionUID = 3951588982510525921L;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The target object being labeled
	 */
	@Id @Column(name="TARGET_OBJ",length=OID.OID_LENGTH,nullable=false) @Basic
	@Getter @Setter private String _targetObjectOid;
	/**
	 * The target object type being labeled
	 */
	@Id @Column(name="TARGET_OBJ_TYPE",length=OID.OID_LENGTH,nullable=false) @Basic
	@Getter @Setter private String _targetObjectType;
	/**
	 * The pick oid 
	 */
	@Id @Column(name="PICK_OID",length=OID.OID_LENGTH,nullable=false) @Basic
	@Getter @Setter private String _pickOid;
	
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void _preCreate() {
		// nothing
	}
	@Override
	protected void _preUpdate() {
		// nothing
	}
}
