package models.contributions.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import models.Botanist;
import models.contributions.BotanistsContribution;
import models.contributions.CollectorContribution;

@Entity
@DiscriminatorValue("COLLECTOR")
public class CollectorContributionReport extends ContributionReport<CollectorContribution>{
	@OneToOne(cascade={CascadeType.ALL})
	private CollectorContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<CollectorContribution> contributions) {
		
		CollectorContribution contribution = contributions.iterator().next();
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new CollectorContribution());
		}
		
		if (contribution.isCollectorNotPresent()) {
			this.getValidatedContribution().setCollectorNotPresent(true);
			this.getValidatedContribution().setCollector(null);
		} else {
			this.getValidatedContribution().setCollectorNotPresent(false);
			this.getValidatedContribution().setCollector(contribution.getCollector());
			List<Botanist> otherBotanist = new ArrayList();
			for(Botanist botanist : contribution.getOtherCollectors()){
				botanist = Botanist.findById(botanist.id);
				if(botanist != null) otherBotanist.add(botanist);
			}
			this.getValidatedContribution().setOtherCollectors(otherBotanist);
		}
		
		this.getValidatedContribution().setSpecimen(contribution.getSpecimen());
		this.getValidatedContribution().setReport(true);
	}

	@Override
	public CollectorContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(CollectorContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}
}
