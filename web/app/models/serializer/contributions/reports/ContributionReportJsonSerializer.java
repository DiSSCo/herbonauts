package models.serializer.contributions.reports;

import java.lang.reflect.Type;

import models.Mission;
import models.Specimen;
import models.contributions.ContributionFeedback;
import models.contributions.reports.ContributionReport;
import models.serializer.contributions.ContributionFeedbackJsonSerializer;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ContributionReportJsonSerializer implements JsonSerializer<ContributionReport> {
	
	public static ContributionReportJsonSerializer instance = new ContributionReportJsonSerializer();
	 
    private ContributionReportJsonSerializer() {}
 
    public static ContributionReportJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(ContributionReport report, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		
		obj.addProperty("conflicts", report.isConflicts());
		obj.addProperty("complete", report.isComplete());
		obj.addProperty("count", report.getCount());
		obj.addProperty("type", report.getType());
		
		obj.add("validatedContribution", context.serialize(report.getValidatedContribution()));
		
		return obj;
	}
}
