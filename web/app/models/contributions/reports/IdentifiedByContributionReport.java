package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import models.contributions.BotanistsContribution;
import models.contributions.IdentifiedByContribution;

@Entity
@DiscriminatorValue("IDENTIFIEDBY")
public class IdentifiedByContributionReport extends ContributionReport<IdentifiedByContribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private IdentifiedByContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<IdentifiedByContribution> contributions) {
		
		IdentifiedByContribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new IdentifiedByContribution());
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
	public IdentifiedByContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(IdentifiedByContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
