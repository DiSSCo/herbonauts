package models.serializer.contributions;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import models.contributions.Contribution;
import models.contributions.ContributionFeedback;
import models.contributions.reports.ContributionReport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ContributionFeedbackJsonSerializer implements JsonSerializer<ContributionFeedback> {

	public static ContributionFeedbackJsonSerializer instance = new ContributionFeedbackJsonSerializer();
	 
    private ContributionFeedbackJsonSerializer() {}
 
    public static ContributionFeedbackJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(ContributionFeedback feedback, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		
		obj.add("contribution", context.serialize(feedback.getContribution()));
		obj.add("byOthers", context.serialize(feedback.getByOthers()));
		obj.addProperty("conflicts", feedback.isConflicts());
		obj.addProperty("showConflicts", feedback.isShowConflicts());
		obj.addProperty("complete", feedback.isComplete());
		obj.addProperty("levelUp", feedback.isLevelUp());
		if (feedback.isLevelUp()) {
			obj.addProperty("userLevel", feedback.getUserLevel());
			obj.addProperty("userPendingLevel", feedback.getUserPendingLevel());
		}
		
		obj.add("report", context.serialize(feedback.getReport(), ContributionReport.class));
		
		obj.add("attributes", context.serialize(feedback.getStringAttributes()));
		
		return obj;
	}

}
