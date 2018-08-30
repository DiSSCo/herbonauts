package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.CountryContribution;
import models.contributions.GeolocalisationContribution;
import models.contributions.LocalityContribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GeolocalisationContributionJsonSerializer implements JsonSerializer<GeolocalisationContribution> {

	public static GeolocalisationContributionJsonSerializer instance = new GeolocalisationContributionJsonSerializer();
	 
    private GeolocalisationContributionJsonSerializer() {}
 
    public static GeolocalisationContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(GeolocalisationContribution contribution, 
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
			obj.addProperty("latitude", contribution.getLatitude());
			obj.addProperty("longitude", contribution.getLongitude());
			if (contribution.getPrecision() != null) {
				obj.addProperty("precision", contribution.getPrecision());
			}
			
		}
		
		obj.addProperty("notSure", contribution.isNotSure());
		obj.addProperty("specimenId", contribution.getSpecimen().id);
		return obj;
	}

}
