package r01f.services.core.persistence.search.db;

import java.util.Collection;

import jakarta.persistence.Query;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.types.geo.GeoPositionFilter;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class GeoPositionFilterToJPQLTranslator {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static Collection<String> composeJPQLPredicatesFor(final GeoPositionFilter filter) {
		Collection<String> preds = Lists.newArrayList();
		if (filter.getPosition() != null) {
			preds.add("geo._pointX = :x");
			preds.add("geo._pointY = :y");
		}
		if (filter.getTerritoryId() != null) {
			preds.add("geo._territoryId = :territoryId");
		}
		if (filter.getCountryId() != null) {
			preds.add("geo._countryId = :countryId");
		}
		if (filter.getStateId() != null) {
			preds.add("geo._stateId = :stateId");
		}
		if (filter.getCountyId() != null) {
			preds.add("geo._countyId = :countyId");
		}
		if (filter.getRegionId() != null) {
			preds.add("geo._regionId = :regionId");
		}
		if (filter.getMunicipalityId() != null) {
			preds.add("geo._municipalityId = :municipalityId");
		}
		if (filter.getLocalityId() != null) {
			preds.add("geo._localityId = :localityId");
		}
		if (filter.getDistrictId() != null) {
			preds.add("geo._districtId = :districtId");
		}
		if (filter.getNeighborhoodId() != null) {
			preds.add("geo._neighborhoodId = :neighborhoodId");
		}
		if (filter.getStreetId() != null) {
			preds.add("geo._streetId = :streetId");
		}
		if (filter.getZipCode() != null) {
			preds.add("geo._zipCode = :zipCode");
		}
		return preds;
	}
	public static void setJPAQueryParameters(final GeoPositionFilter filter,final Query qry) {
		if (filter.getPosition() != null) {
			qry.setParameter("x",filter.getPosition().getX());
			qry.setParameter("y",filter.getPosition().getY());
		}
		if (filter.getTerritoryId() != null) {
			qry.setParameter("territoryId",filter.getTerritoryId().asString());
		}
		if (filter.getCountryId() != null) {
			qry.setParameter("countryId",filter.getCountryId().asString());
		}
		if (filter.getStateId() != null) {
			qry.setParameter("stateId",filter.getStateId().asString());
		}if (filter.getCountyId() != null) {
			qry.setParameter("countyId",filter.getCountyId().asString());
		}
		if (filter.getRegionId() != null) {
			qry.setParameter("regionId",filter.getRegionId().asString());
		}
		if (filter.getMunicipalityId() != null) {
			qry.setParameter("municipalityId",filter.getMunicipalityId().asString());
		}
		if (filter.getLocalityId() != null) {
			qry.setParameter("localityId",filter.getLocalityId().asString());
		}
		if (filter.getDistrictId() != null) {
			qry.setParameter("districtId",filter.getDistrictId().asString());
		}
		if (filter.getNeighborhoodId() != null) {
			qry.setParameter("neighborhoodId",filter.getNeighborhoodId().asString());
		}
		if (filter.getStreetId() != null) {
			qry.setParameter("streetId",filter.getStreetId().asString());
		}
		if (filter.getZipCode() != null) {
			qry.setParameter("zipCode",filter.getZipCode().asString());
		}
	}
}
