package models.contributions.reports;

import java.util.Collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.contributions.BotanistsContribution;
import models.contributions.UnusableContribution;

@Entity
@DiscriminatorValue("UNUSABLE")
public class UnusableContributionReport extends ContributionReport<UnusableContribution> {
	
	@Override
	public void takeForExample(Collection<UnusableContribution> contributions) {
		// Rien à faire
	}
	
	@Override
	public UnusableContribution getValidatedContribution() {
		return null;
	}

}
