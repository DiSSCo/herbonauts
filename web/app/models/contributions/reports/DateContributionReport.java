package models.contributions.reports;

import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import models.contributions.BotanistsContribution;
import models.contributions.CountryContribution;
import models.contributions.DateContribution;

@Entity
@DiscriminatorValue("DATE")
public class DateContributionReport extends ContributionReport<DateContribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private DateContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<DateContribution> contributions) {
		
		DateContribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new DateContribution());
		}
		
		this.getValidatedContribution().setSpecimen(contribution.getSpecimen());
		
		if (contribution.isNotPresent()) {
			this.getValidatedContribution().setNotPresent(true);
			this.getValidatedContribution().setCollectDate(null);
			this.getValidatedContribution().setCollectStartDate(null);
			this.getValidatedContribution().setCollectEndDate(null);
		} else if (contribution.isPeriod()) {
			this.getValidatedContribution().setPeriod(true);
			this.getValidatedContribution().setNotPresent(false);
			this.getValidatedContribution().setCollectDate(null);
			this.getValidatedContribution().setCollectStartDate(contribution.getCollectStartDate());
			this.getValidatedContribution().setCollectEndDate(contribution.getCollectEndDate());
		} else {
			this.getValidatedContribution().setPeriod(false);
			this.getValidatedContribution().setCollectDate(contribution.getCollectDate());
			this.getValidatedContribution().setCollectStartDate(null);
			this.getValidatedContribution().setCollectEndDate(null);
		}
		this.getValidatedContribution().setReport(true);
	}

	@Override
	public DateContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(DateContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}
	
}
