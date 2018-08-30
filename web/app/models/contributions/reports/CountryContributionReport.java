package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import models.contributions.BotanistsContribution;
import models.contributions.CountryContribution;

@Entity
@DiscriminatorValue("COUNTRY")
public class CountryContributionReport extends ContributionReport<CountryContribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private CountryContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<CountryContribution> contributions) {
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new CountryContribution());
		}
		CountryContribution contribution = contributions.iterator().next();
		
		if (contribution.isNotPresent()) {
			this.getValidatedContribution().setNotPresent(true);
			this.getValidatedContribution().setCountry(null);
		} else {
			this.getValidatedContribution().setNotPresent(false);
			this.getValidatedContribution().setCountry(contribution.getCountry());
		}
		
		this.getValidatedContribution().setReport(true);
	}
	
	@Override
	public CountryContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(CountryContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
