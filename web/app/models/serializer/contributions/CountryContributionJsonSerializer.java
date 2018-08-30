package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.CountryContribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CountryContributionJsonSerializer implements JsonSerializer<CountryContribution> {

	public static CountryContributionJsonSerializer instance = new CountryContributionJsonSerializer();
	 
    private CountryContributionJsonSerializer() {}
 
    public static CountryContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(CountryContribution contribution, 
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
		
		if (contribution.getCountry() != null) {
			obj.add("country", context.serialize(contribution.getCountry()));
		}
		obj.addProperty("notPresent", contribution.isNotPresent());
		obj.addProperty("notSure", contribution.isNotSure());
		obj.addProperty("deducted", contribution.isDeducted());
		
		return obj;
	}

}
