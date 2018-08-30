package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.CountryContribution;
import models.contributions.RegionLevel1Contribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RegionLevel1ContributionJsonSerializer implements JsonSerializer<RegionLevel1Contribution> {

	public static RegionLevel1ContributionJsonSerializer instance = new RegionLevel1ContributionJsonSerializer();
	 
    private RegionLevel1ContributionJsonSerializer() {}
 
    public static RegionLevel1ContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(RegionLevel1Contribution contribution, 
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
		/*if (contribution.regionLevel1 != null) {
			obj.addProperty("regionLevel1", contribution.regionLevel1.name);
		}*/
		if (contribution.getRegionLevel1() != null) {
			obj.add("regionLevel1", context.serialize(contribution.getRegionLevel1()));
		}
		obj.addProperty("notPresent", contribution.isNotPresent());
		obj.addProperty("notSure", contribution.isNotSure());
		obj.addProperty("deducted", contribution.isDeducted());
		
		return obj;
	}

}
