package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * Région niveau 1
 */
@Entity
@Table(name="H_REGION_LEVEL_1")
public class RegionLevel1 extends Model {

	@ManyToOne
	private Country country;
	
	private String name;
	
	public static List<RegionLevel1> getRegionsForCountry(Long countryId) {
		return RegionLevel1.find("country.id = ? order by name", countryId).fetch();
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Country getCountry() {
		return country;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static List<RegionLevel1> findAllOrdered() {
		return RegionLevel1.find("order by name").fetch();
	}
	
}
