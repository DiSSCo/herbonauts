package models.serializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import play.templates.JavaExtensions;
import models.Mission;
import models.Specimen;
import models.User;
import models.contributions.reports.BotanistsContributionReport;
import models.contributions.reports.CollectorContributionReport;
import models.contributions.reports.ContributionReport;
import models.contributions.reports.IdentifiedByContributionReport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SpecimenWithBotanistsReportsJsonSerializer implements JsonSerializer<Specimen> {

	public static SpecimenWithBotanistsReportsJsonSerializer instance = new SpecimenWithBotanistsReportsJsonSerializer();
	 
    private SpecimenWithBotanistsReportsJsonSerializer() {}
 
    public static SpecimenWithBotanistsReportsJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Specimen specimen, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", specimen.id);
		obj.addProperty("family", specimen.getFamily());
		obj.addProperty("genus", specimen.getGenus());
		obj.addProperty("institute", specimen.getInstitute());
		obj.addProperty("collection", specimen.getCollection());
		obj.addProperty("code", specimen.getCode());
		obj.addProperty("genusSpecies", specimen.getGenusSpecies());
		if (specimen.getLastModified() != null) {
			obj.addProperty("since", JavaExtensions.since(specimen.getLastModified()));
		}
		
		CollectorContributionReport collectorReport = 
			(CollectorContributionReport) specimen.getContributionReport("COLLECTOR");
		IdentifiedByContributionReport identifiedByReport = 
				(IdentifiedByContributionReport) specimen.getContributionReport("IDENTIFIEDBY");
		
		JsonObject botanistsJSON = new JsonObject();
		if (!collectorReport.getValidatedContribution().isCollectorNotPresent()) {
			botanistsJSON.addProperty("collectorId", collectorReport.getValidatedContribution().getCollector().id);
		}
		if (!identifiedByReport.getValidatedContribution().isDeterminerNotPresent()) {
			botanistsJSON.addProperty("determinerId", identifiedByReport.getValidatedContribution().getDeterminer().id);
		}
		
		obj.add("botanists", botanistsJSON);
		
		return obj;
	}

}
