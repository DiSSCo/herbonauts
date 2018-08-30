package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.BotanistsContribution;
import models.contributions.IdentifiedByContribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class IdentifiedByContributionJsonSerializer implements JsonSerializer<IdentifiedByContribution>{
	public static IdentifiedByContributionJsonSerializer instance = new IdentifiedByContributionJsonSerializer();
	 
    private IdentifiedByContributionJsonSerializer() {}
 
    public static IdentifiedByContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(IdentifiedByContribution contribution, 
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
		
		
		return obj;
	}
}
