package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.Botanist;
import models.Country;
import models.contributions.BotanistsContribution;
import models.contributions.Contribution;
import models.contributions.CountryContribution;

@Entity
@DiscriminatorValue("BOTANISTS")
public class BotanistsContributionReport extends ContributionReport<BotanistsContribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private BotanistsContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<BotanistsContribution> contributions) {
		
		BotanistsContribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new BotanistsContribution());
		}
		
		if (contribution.isCollectorNotPresent()) {
			this.getValidatedContribution().setCollectorNotPresent(true);
			this.getValidatedContribution().setCollector(null);
		} else {
			this.getValidatedContribution().setCollectorNotPresent(false);
			this.getValidatedContribution().setCollector(contribution.getCollector());
		}
		
		if (contribution.isDeterminerNotPresent()) {
			this.getValidatedContribution().setDeterminerNotPresent(true);
			this.getValidatedContribution().setDeterminer(null);
		} else {
			this.getValidatedContribution().setDeterminerNotPresent(false);
			this.getValidatedContribution().setDeterminer(contribution.getDeterminer());
		}
		
		this.getValidatedContribution().setSpecimen(contribution.getSpecimen());
		this.getValidatedContribution().setReport(true);
	}

	@Override
	public BotanistsContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(BotanistsContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
