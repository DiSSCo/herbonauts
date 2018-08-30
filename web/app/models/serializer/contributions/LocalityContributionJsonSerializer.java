package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.CountryContribution;
import models.contributions.LocalityContribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalityContributionJsonSerializer implements JsonSerializer<LocalityContribution> {

	public static LocalityContributionJsonSerializer instance = new LocalityContributionJsonSerializer();
	 
    private LocalityContributionJsonSerializer() {}
 
    public static LocalityContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(LocalityContribution contribution, 
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
		obj.addProperty("notPresent", contribution.isNotPresent());
		if (!contribution.isNotPresent()) {
			obj.addProperty("locality", contribution.getLocality());
		}
		
		obj.addProperty("notSure", contribution.isNotSure());
		
		return obj;
	}

}
