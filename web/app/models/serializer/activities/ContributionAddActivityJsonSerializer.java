package models.serializer.activities;

import java.lang.reflect.Type;

import play.i18n.Messages;

import models.Mission;
import models.User;
import models.activities.Activity;
import models.activities.ContributionAddActivity;
import models.contributions.Contribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ContributionAddActivityJsonSerializer implements JsonSerializer<ContributionAddActivity> {

	public static ContributionAddActivityJsonSerializer instance = new ContributionAddActivityJsonSerializer();
	 
    private ContributionAddActivityJsonSerializer() {}
 
    public static ContributionAddActivityJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(ContributionAddActivity activity, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", activity.id);
		obj.addProperty("html", Messages.get(activity.getI18nKey(), 
				activity.getUser().getLogin(), 
				activity.getContribution().getI18nKey(),
				activity.getContribution().getSpecimen().getCode())	
		);
		obj.addProperty("type", activity.getType());
		
		return obj;
	}

}
