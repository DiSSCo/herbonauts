package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.BotanistsContribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BotanistsContributionJsonSerializer implements JsonSerializer<BotanistsContribution> {

	public static BotanistsContributionJsonSerializer instance = new BotanistsContributionJsonSerializer();
	 
    private BotanistsContributionJsonSerializer() {}
 
    public static BotanistsContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(BotanistsContribution contribution, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", contribution.id);
		obj.addProperty("type", contribution.getType());
		
		if (contribution.getUser() != null) {
			obj.addProperty("userLogin", contribution.getUser().getLogin());
			obj.addProperty("userImageId", contribution.getUser().getImageId());
			obj.addProperty("userHasImage", contribution.getUser().isHasImage());
		}
		
		if (!contribution.isDeterminerNotPresent()) {
			obj.addProperty("determiner", contribution.getDeterminer().getName());
			obj.addProperty("determinerNotSure", contribution.isDeterminerNotSure());
		} else {
			obj.addProperty("determinerNotPresent", contribution.isDeterminerNotPresent());
		}
		
		if (!contribution.isCollectorNotPresent()) {
			obj.addProperty("collector", contribution.getCollector().getName());
			obj.addProperty("collectorNotSure", contribution.isCollectorNotSure());
			obj.add("otherCollectors", context.serialize(contribution.getOtherCollectors()));
		} else {
			obj.addProperty("collectorNotPresent", contribution.isCollectorNotPresent());
		}
		
		
		return obj;
	}

}
