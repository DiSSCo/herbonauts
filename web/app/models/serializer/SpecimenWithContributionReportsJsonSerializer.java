package models.serializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import play.templates.JavaExtensions;

import models.Mission;
import models.Specimen;
import models.User;
import models.contributions.reports.ContributionReport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SpecimenWithContributionReportsJsonSerializer implements JsonSerializer<Specimen> {

	public static SpecimenWithContributionReportsJsonSerializer instance = new SpecimenWithContributionReportsJsonSerializer();
	 
    private SpecimenWithContributionReportsJsonSerializer() {}
 
    public static SpecimenWithContributionReportsJsonSerializer get() {
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
		
		Map<String, ContributionReport> reports = specimen.getContributionReportsByType();
		
		// TODO voir gson et exclusion
		Map<String, Map<String, Object>> simpleReports = new HashMap<String, Map<String,Object>>();
		for (Entry<String, ContributionReport> entry : reports.entrySet()) {
			HashMap<String, Object> simpleReport = new HashMap<String, Object>();
			simpleReport.put("type", entry.getValue().getType());
			simpleReport.put("count", entry.getValue().getCount());
			simpleReport.put("complete", entry.getValue().isComplete());
			simpleReport.put("conflicts", entry.getValue().isConflicts());
			simpleReports.put(entry.getValue().getType(), simpleReport);
		}
		
		obj.add("reports", context.serialize(simpleReports));
		
		return obj;
	}

}
