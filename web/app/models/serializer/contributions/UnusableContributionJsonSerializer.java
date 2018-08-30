package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.CountryContribution;
import models.contributions.GeolocalisationContribution;
import models.contributions.LocalityContribution;
import models.contributions.UnusableContribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UnusableContributionJsonSerializer implements JsonSerializer<UnusableContribution> {

	public static UnusableContributionJsonSerializer instance = new UnusableContributionJsonSerializer();
	 
    private UnusableContributionJsonSerializer() {}
 
    public static UnusableContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(UnusableContribution contribution, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", contribution.id);
		obj.addProperty("type", contribution.getType());
		
		return obj;
	}

}
