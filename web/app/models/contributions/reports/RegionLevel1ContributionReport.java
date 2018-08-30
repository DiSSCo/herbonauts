package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.RegionLevel1;
import models.contributions.BotanistsContribution;
import models.contributions.CountryContribution;
import models.contributions.RegionLevel1Contribution;

@Entity
@DiscriminatorValue("REGION_1")
public class RegionLevel1ContributionReport extends ContributionReport<RegionLevel1Contribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private RegionLevel1Contribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<RegionLevel1Contribution> contributions) {
		
		RegionLevel1Contribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new RegionLevel1Contribution());
		}
		
		this.getValidatedContribution().setReport(true);
		
		if (contribution.isNotPresent()) {
			this.getValidatedContribution().setNotPresent(true);
			this.getValidatedContribution().setRegionLevel1(null);
		} else {
			this.getValidatedContribution().setNotPresent(false);
			this.getValidatedContribution().setRegionLevel1(contribution.getRegionLevel1());
		}
	}
	
	@Override
	public RegionLevel1Contribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(RegionLevel1Contribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
