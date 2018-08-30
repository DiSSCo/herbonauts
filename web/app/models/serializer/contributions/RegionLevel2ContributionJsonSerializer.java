package models.serializer.contributions;

import java.lang.reflect.Type;

import models.contributions.CountryContribution;
import models.contributions.RegionLevel2Contribution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RegionLevel2ContributionJsonSerializer implements JsonSerializer<RegionLevel2Contribution> {

	public static RegionLevel2ContributionJsonSerializer instance = new RegionLevel2ContributionJsonSerializer();
	 
    private RegionLevel2ContributionJsonSerializer() {}
 
    public static RegionLevel2ContributionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(RegionLevel2Contribution contribution, 
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
		if (contribution.getRegionLevel2() != null) {
			obj.add("regionLevel2", context.serialize(contribution.getRegionLevel2()));
		}
		obj.addProperty("notPresent", contribution.isNotPresent());
		obj.addProperty("notSure", contribution.isNotSure());
		obj.addProperty("deducted", contribution.isDeducted());
		
		return obj;
	}

}
