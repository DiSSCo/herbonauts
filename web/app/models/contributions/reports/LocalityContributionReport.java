package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.Country;
import models.contributions.BotanistsContribution;
import models.contributions.Contribution;
import models.contributions.CountryContribution;
import models.contributions.LocalityContribution;

@Entity
@DiscriminatorValue("LOCALITY")
public class LocalityContributionReport extends ContributionReport<LocalityContribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private LocalityContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<LocalityContribution> contributions) {
		
		LocalityContribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new LocalityContribution());
		}
		
		this.getValidatedContribution().setReport(true);
		
		if (contribution.isNotPresent()) {
			this.getValidatedContribution().setNotPresent(true);
			this.getValidatedContribution().setLocality(null);
		} else {
			this.getValidatedContribution().setNotPresent(false);
			this.getValidatedContribution().setLocality(contribution.getLocality());
		}
	}
	
	@Override
	public LocalityContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(LocalityContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
