package r01f.services.core.persistence.db.entities;


import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.NamedQueries;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.oids.OID;

/**
 * This is a BASE type for the DB entity that stores the label catalog
 * Just extend this entity and set the [table name]
 * <pre class='brush:java'>
 *		@Entity @Cacheable(false)
 *		@Table(name="MYGEOPICKS")
 *			@IdClass(DBEntityForCatalogableGeoPickBase.class)
 *
 *		@Accessors(prefix="_")
 *		@NoArgsConstructor
 *		public class MyDBEntityForMyCatalogableObjectLabelPicks
 *			 extends DBEntityForCatalogableGeoPickBase {
 *			// nothing else
 *		}
 * </pre>
 */
@MappedSuperclass

//({
//	// simple
//	@Index(name="GEOPICKIDX_BY_TERRITORY",
//		   columnNames = {"TERRITORY_ID"}),
//	@Index(name="GEOPICKIDX_BY_COUNTRY",
//		   columnNames = {"COUNTRY_ID"}),
//	@Index(name="GEOPICKIDX_BY_STATE",
//		   columnNames = {"STATE_ID"}),
//	@Index(name="GEOPICKIDX_BY_COUNTY",
//		   columnNames = {"COUNTY_ID"}),
//	@Index(name="GEOPICKIDX_BY_REGION",
//		   columnNames = {"REGION_ID"}),
//	@Index(name="GEOPICKIDX_BY_MUNICIPALITY",
//		   columnNames = {"MUNICIPALITY_ID"}),
//	@Index(name="GEOPICKIDX_BY_DISTRICT",
//		   columnNames = {"DISTRICT_ID"}),
//	@Index(name="GEOPICKIDX_BY_STREET",
//		   columnNames = {"STREET_ID"}),
//	@Index(name="GEOPICKIDX_BY_ZIPCODE",
//		   columnNames = {"ZIPCODE"}),
//	// compound
//	@Index(name="GEOPICKIDX_BY_COUNTRY_STATE_COUNTY",
//		   columnNames = {"COUNTRY_ID","STATE_ID","COUNTY_ID"}),
//	@Index(name="GEOPICKIDX_BY_STATE_MUNICIPALITY",
//		   columnNames = {"STATE_ID","MUNICIPALITY_ID"}),
//	@Index(name="GEOPICKIDX_BY_STATE_MUNICIPALITY_STREET",
//		   columnNames = {"STATE_ID","MUNICIPALITY_ID","STREET_ID"}),
//	// by point
//	@Index(name="GEOPICKIDX_BY_POINT",
//		   columnNames = {"POINT_X","POINT_y"})
//})
@NamedQueries({

})

@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityForCatalogableGeoPickBase
	 		  extends DBEntityForCatalogablePickBase {
	private static final long serialVersionUID = 4019488858598113891L;

/////////////////////////////////////////////////////////////////////////////////////////
//	POSITION FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Column(name="TERRITORY_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _territoryId;

	@Column(name="COUNTRY_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _countryId;

	@Column(name="STATE_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _stateId;

	@Column(name="COUNTY_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _countyId;

	@Column(name="REGION_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _regionId;

	@Column(name="MUNICIPALITY_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _municipalityId;

	@Column(name="LOCALITY_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _localityId;

	@Column(name="DISTRICT_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _districtId;

	@Column(name="NEIGHBORHOOD_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _neighborhoodId;

	@Column(name="STREET_ID",length=OID.OID_LENGTH) @Basic
	@Getter @Setter private String _streetId;

	@Column(name="ZIPCODE",length=20) @Basic
	@Getter @Setter private String _zipCode;
/////////////////////////////////////////////////////////////////////////////////////////
//	COORDINATES FIELS
/////////////////////////////////////////////////////////////////////////////////////////
	@Column(name="POINT_X",nullable=false) @Basic
	@Getter @Setter private double _pointX;

	@Column(name="POINT_Y",nullable=false) @Basic
	@Getter @Setter private double _pointY;
}
