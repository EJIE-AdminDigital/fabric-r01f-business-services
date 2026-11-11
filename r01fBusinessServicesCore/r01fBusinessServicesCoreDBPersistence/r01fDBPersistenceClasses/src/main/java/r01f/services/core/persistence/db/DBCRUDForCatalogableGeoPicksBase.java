package r01f.services.core.persistence.db;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.EntityManager;
import r01f.model.catalogable.CatalogableIDs.CatalogableObjectTypeID;
import r01f.model.catalogable.CatalogableOIDs.CatalogableObjectOID;
import r01f.model.catalogable.CatalogablePicksChangesBase;
import r01f.model.catalogable.geo.CatalogableGeoPicks;
import r01f.model.catalogable.geo.GeoCatalogableOIDs.CatalogableGeoPickOID;
import r01f.model.catalogable.geo.GeoCatalogableOIDs.CatalogableGeoPicksOID;
import r01f.model.catalogable.geo.GeoPositionPick;
import r01f.model.catalogable.geo.GeoPositionPicksChanges;
import r01f.model.security.context.SecurityContext;
import r01f.patterns.Factory;
import r01f.services.core.persistence.db.config.DBModuleConfig;
import r01f.services.core.persistence.db.entities.DBEntityForCatalogableGeoPickBase;
import r01f.services.core.persistence.db.entities.DBEntityForCatalogablePickPrimaryKeyBase;
import r01f.services.core.persistence.internal.interfaces.CoreCRUDServicesForCatalogableGeoPicks;
import r01f.types.geo.GeoIDs.GeoCountryID;
import r01f.types.geo.GeoIDs.GeoCountyID;
import r01f.types.geo.GeoIDs.GeoDistrictID;
import r01f.types.geo.GeoIDs.GeoLocalityID;
import r01f.types.geo.GeoIDs.GeoMunicipalityID;
import r01f.types.geo.GeoIDs.GeoNeighborhoodID;
import r01f.types.geo.GeoIDs.GeoRegionID;
import r01f.types.geo.GeoIDs.GeoStateID;
import r01f.types.geo.GeoIDs.GeoStreetID;
import r01f.types.geo.GeoIDs.GeoTerritoryID;
import r01f.types.geo.GeoIDs.GeoZipCode;
import r01f.types.geo.GeoLocationItem;
import r01f.types.geo.GeoPosition2D;
import r01f.util.types.Strings;


public abstract class DBCRUDForCatalogableGeoPicksBase<PK extends DBEntityForCatalogablePickPrimaryKeyBase,DB extends DBEntityForCatalogableGeoPickBase>
	 		  extends DBCRUDForCatalogablePicksBase<CatalogableGeoPickOID,GeoPositionPick,CatalogableGeoPicks,
	 									   			PK,DB>
  		   implements CoreCRUDServicesForCatalogableGeoPicks {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	protected DBCRUDForCatalogableGeoPicksBase(final Class<DB> dbPickType,
										   	   final Factory<DB> dbPickFactory,
										   	   final DBModuleConfig dbCfg,
										   	   final EntityManager entityManager) {
		super(CatalogableGeoPicks.class,
			  CatalogableGeoPicks::new,
			  dbPickType,
			  dbPickFactory,
			  dbCfg,
			  entityManager);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	protected <C extends CatalogablePicksChangesBase<GeoPositionPick>> C _changesBetween(final Set<GeoPositionPick> current,
																						 final Set<GeoPositionPick> received) {
		return (C)new GeoPositionPicksChanges(current,received);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected GeoPositionPick _transformDBPickIntoPick(final DB dbPick) {
		GeoPositionPick outPick = new GeoPositionPick();

		outPick.setOid(Strings.isNOTNullOrEmpty(dbPick.getPickOid()) ? CatalogableGeoPickOID.forId(dbPick.getPickOid()) : null);

		outPick.setTerritory(dbPick.getTerritoryId() != null ? GeoLocationItem.normalized(GeoTerritoryID.forId(dbPick.getTerritoryId())) : null);
		outPick.setCountry(dbPick.getCountryId() != null ? GeoLocationItem.normalized(GeoCountryID.forId(dbPick.getCountryId())) : null);
		outPick.setState(dbPick.getStateId() != null ? GeoLocationItem.normalized(GeoStateID.forId(dbPick.getStateId())) : null);
		outPick.setCounty(dbPick.getCountyId() != null ? GeoLocationItem.normalized(GeoCountyID.forId(dbPick.getCountyId())) : null);
		outPick.setRegion(dbPick.getRegionId() != null ? GeoLocationItem.normalized(GeoRegionID.forId(dbPick.getRegionId())) : null);
		outPick.setMunicipality(dbPick.getMunicipalityId() != null ? GeoLocationItem.normalized(GeoMunicipalityID.forId(dbPick.getMunicipalityId())) : null);
		outPick.setLocality(dbPick.getLocalityId() != null ? GeoLocationItem.normalized(GeoLocalityID.forId(dbPick.getLocalityId())) : null);
		outPick.setDistrict(dbPick.getDistrictId() != null ? GeoLocationItem.normalized(GeoDistrictID.forId(dbPick.getDistrictId())) : null);
		outPick.setNeighborhood(dbPick.getNeighborhoodId() != null ? GeoLocationItem.normalized(GeoNeighborhoodID.forId(dbPick.getNeighborhoodId())) : null);
		outPick.setStreet(dbPick.getStreetId() != null ? GeoLocationItem.normalized(GeoStreetID.valueOf(dbPick.getStreetId())) : null);
		outPick.setZipCode(dbPick.getZipCode() != null ? GeoZipCode.forId(dbPick.getZipCode()) : null);
		outPick.setPosition(new GeoPosition2D(dbPick.getPointX(),dbPick.getPointY()));
		return outPick;
	}
	@Override
	protected void _transferPickToDBPick(final SecurityContext securityContext,
										 final CatalogableObjectOID catalogableObjectOid,
										 final CatalogableObjectTypeID catalogableObjectTypeId,
										 final GeoPositionPick pick,final DB dbPick) {
		// context
		Instant createDate = Instant.now();
		dbPick.setCreateDate(createDate);
		dbPick.setCreatorSecurityPrincipalOid(securityContext.getSecurityPrincipalOid());
		dbPick.setCreatorSecurityPrincipalId(securityContext.getSecurityPrincipalId());
		
		// target object
		dbPick.setTargetObjectType(catalogableObjectTypeId.asString());
		dbPick.setTargetObjectOid(catalogableObjectOid.asString());

		dbPick.setPickOid(pick.getOid() != null ? pick.getOid().asString() : CatalogableGeoPicksOID.supply().asString());

		dbPick.setTerritoryId(pick.getTerritoryId() != null ? pick.getTerritoryId().asString() : null);
		dbPick.setCountryId(pick.getCountryId() != null ? pick.getCountryId().asString() : null);
		dbPick.setStateId(pick.getStateId() != null ? pick.getStateId().asString() : null);
		dbPick.setCountyId(pick.getCountyId() != null ? pick.getCountyId().asString() : null);
		dbPick.setRegionId(pick.getRegionId() != null ? pick.getRegionId().asString() : null);
		dbPick.setMunicipalityId(pick.getMunicipalityId() != null ? pick.getMunicipalityId().asString() : null);
		dbPick.setLocalityId(pick.getLocalityId() != null ? pick.getLocalityId().asString() : null);
		dbPick.setDistrictId(pick.getDistrictId() != null ? pick.getDistrictId().asString() : null);
		dbPick.setNeighborhoodId(pick.getNeighborhoodId() != null ? pick.getNeighborhoodId().asString() : null);
		dbPick.setStreetId(pick.getStreetId() != null ? pick.getStreetId().asString() : null);
		dbPick.setZipCode(pick.getZipCode() != null ? pick.getZipCode().asString() : null);
	}
}
