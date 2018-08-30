package models.contributions;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.RegionLevel1;
import models.RegionLevel2;

@Entity
@DiscriminatorValue("REGION_1")
public class RegionLevel1Contribution extends Contribution<RegionLevel1Contribution> {

	@ManyToOne
	private RegionLevel1 regionLevel1;
	
	@Override
	public boolean isInConflict(RegionLevel1Contribution other) {
		RegionLevel1Contribution otherRegion = (RegionLevel1Contribution) other;
		
		if (this.isNotPresent() && otherRegion.isNotPresent()) { // 1. identiques -> pas de conflits
			return false;
		} else  if (this.isNotPresent() || otherRegion.isNotPresent()) { // 2. differents (cf 1.)
			return true;
		} else {
			return !(this.getRegionLevel1().id.equals(otherRegion.getRegionLevel1().id)); // 3. comparons les pays
		}
	}

	@Override
	public void sanitize() {
		if (isNotPresent()) {
			setRegionLevel1(null);
			setDeducted(false);
			setNotSure(false);
		}
	}

	@Override
	public void validate(RegionLevel1Contribution other) {
		this.setNotPresent(other.isNotPresent());
		this.setRegionLevel1(other.getRegionLevel1());
		this.setNotSure(false);
		this.setDeducted(false);
	}

	public void setRegionLevel1(RegionLevel1 regionLevel1) {
		this.regionLevel1 = regionLevel1;
	}

	public RegionLevel1 getRegionLevel1() {
		return regionLevel1;
	}
	
}
