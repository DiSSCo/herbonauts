package models.contributions.reports;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.sun.org.apache.bcel.internal.generic.CPInstruction;

import models.contributions.BotanistsContribution;
import models.contributions.CountryContribution;
import models.contributions.GeolocalisationContribution;

@Entity
@DiscriminatorValue("GEOLOCALISATION")
public class GeolocalisationContributionReport extends ContributionReport<GeolocalisationContribution> {
	
	@OneToOne(cascade={CascadeType.ALL})
	private GeolocalisationContribution validatedContribution;
	
	@Override
	public void takeForExample(Collection<GeolocalisationContribution> contributions) {
		
		int count = 0;
		float latSum = 0.0f;
		float lngSum = 0.0f;
		
		if (this.getValidatedContribution() == null) {
			this.setValidatedContribution(new GeolocalisationContribution());
		}
		
		this.getValidatedContribution().setReport(true);
		
		for (GeolocalisationContribution contribution : contributions) {
			
			if (this.getValidatedContribution().getSpecimen() == null) {
				this.getValidatedContribution().setSpecimen(contribution.getSpecimen());
			}
			
			// Ici elles sont toutes sans conflit donc si la 1ère 
			// est notPresent, les autres aussi
			if (contribution.isNotPresent()) {
				this.getValidatedContribution().setNotPresent(contribution.isNotPresent());
				this.getValidatedContribution().setLatitude(null);
				this.getValidatedContribution().setLongitude(null);
				return; 
			}
			
			latSum += contribution.getLatitude();
			lngSum += contribution.getLongitude();
			count++;
		}
		
		this.getValidatedContribution().setNotPresent(false);
		this.getValidatedContribution().setLatitude(latSum / count);
		this.getValidatedContribution().setLongitude(lngSum / count);
		
	}
	
	@Override
	public GeolocalisationContribution getValidatedContribution() {
		return this.validatedContribution;
	}

	public void setValidatedContribution(GeolocalisationContribution validatedContribution) {
		this.validatedContribution = validatedContribution;
	}

}
