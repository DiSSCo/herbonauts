package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.RegionLevel2;
import models.contributions.BotanistsContribution;
import models.contributions.CountryContribution;
import models.contributions.RegionLevel1Contribution;
import models.contributions.RegionLevel2Contribution;

@Entity
@DiscriminatorValue("REGION_2")
public class RegionLevel2ContributionReport extends ContributionReport<RegionLevel2Contribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private RegionLevel2Contribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<RegionLevel2Contribution> contributions) {
		
		RegionLevel2Contribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new RegionLevel2Contribution());
		}
		
		this.getValidatedContribution().setReport(true);
		
		if (contribution.isNotPresent()) {
			this.getValidatedContribution().setNotPresent(true);
			this.getValidatedContribution().setRegionLevel2(null);
		} else {
			this.getValidatedContribution().setNotPresent(false);
			this.getValidatedContribution().setRegionLevel2(contribution.getRegionLevel2());
		}
	}
	
	@Override
	public RegionLevel2Contribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(RegionLevel2Contribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
