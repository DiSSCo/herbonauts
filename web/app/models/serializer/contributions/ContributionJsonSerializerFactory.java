package models.serializer.contributions;

import models.contributions.Contribution;
import models.contributions.UnusableContribution;

import com.google.gson.JsonSerializer;

public class ContributionJsonSerializerFactory {

	public static JsonSerializer<? extends Contribution> getInstance(String typeStr) {
		Contribution.Type type = Contribution.Type.valueOf(typeStr);
		return getInstance(type);
	}
	
	public static JsonSerializer<? extends Contribution> getInstance(Contribution.Type type) {
		switch(type) {
			case COUNTRY: return CountryContributionJsonSerializer.get();
			case REGION_1: return RegionLevel1ContributionJsonSerializer.get();
			case REGION_2: return RegionLevel2ContributionJsonSerializer.get();
			case DATE: return DateContributionJsonSerializer.get();
//			case BOTANISTS: return BotanistsContributionJsonSerializer.get();
			case COLLECTOR: return CollectorContributionJsonSerializer.get();
			case IDENTIFIEDBY: return IdentifiedByContributionJsonSerializer.get();
			case LOCALITY: return LocalityContributionJsonSerializer.get();
			case GEOLOCALISATION: return GeolocalisationContributionJsonSerializer.get();
			case UNUSABLE: return UnusableContributionJsonSerializer.get();
			default: return null;
		}
		
	}
	
}
