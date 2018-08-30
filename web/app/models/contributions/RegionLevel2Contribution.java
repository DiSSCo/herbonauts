package models.contributions;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.RegionLevel1;
import models.RegionLevel2;

@Entity
@DiscriminatorValue("REGION_2")
public class RegionLevel2Contribution extends Contribution<RegionLevel2Contribution> {

	@ManyToOne
	private RegionLevel2 regionLevel2;
	
	@Override
	public boolean isInConflict(RegionLevel2Contribution other) {
		RegionLevel2Contribution otherRegion = (RegionLevel2Contribution) other;
		
		if (this.isNotPresent() && otherRegion.isNotPresent()) { // 1. identiques -> pas de conflits
			return false;
		} else  if (this.isNotPresent() || otherRegion.isNotPresent()) { // 2. differents (cf 1.)
			return true;
		} else {
			if (this.getRegionLevel2() == null || otherRegion.getRegionLevel2() == null || this.getRegionLevel2().id == null) return true;
			return !(this.getRegionLevel2().id.equals(otherRegion.getRegionLevel2().id)); // 3. comparons les pays
		}
	}

	@Override
	public void sanitize() {
		if (isNotPresent()) {
			setRegionLevel2(null);
			setDeducted(false);
			setNotSure(false);
		}
	}

	@Override
	public void validate(RegionLevel2Contribution other) {
		this.setNotPresent(other.isNotPresent());
		this.setRegionLevel2(other.getRegionLevel2());
		this.setNotSure(false);
		this.setDeducted(false);
	}

	public void setRegionLevel2(RegionLevel2 regionLevel2) {
		this.regionLevel2 = regionLevel2;
	}

	public RegionLevel2 getRegionLevel2() {
		return regionLevel2;
	}
	
}
