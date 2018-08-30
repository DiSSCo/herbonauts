package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * Région de niveau 2
 */
@Entity
@Table(name="H_REGION_LEVEL_2")
public class RegionLevel2 extends Model {

	@ManyToOne
	private RegionLevel1 region1;
	
	private String name;
	
	public static List<RegionLevel2> getRegionsForRegion1(Long region1Id) {
		return RegionLevel2.find("region1.id = ? order by name", region1Id).fetch();
	}

	public void setRegion1(RegionLevel1 region1) {
		this.region1 = region1;
	}

	public RegionLevel1 getRegion1() {
		return region1;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
