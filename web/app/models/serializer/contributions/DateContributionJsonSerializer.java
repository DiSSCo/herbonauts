package models.serializer.contributions;

import java.lang.reflect.Type;
import java.util.Date;

import models.contributions.DateContribution;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.templates.JavaExtensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateContributionJsonSerializer implements JsonSerializer<DateContribution> {

	public static DateContributionJsonSerializer instance = new DateContributionJsonSerializer();
	 
    private DateContributionJsonSerializer() {}
 
    public static DateContributionJsonSerializer get() {
        return instance;
    }
	
	
	private String formatOrNull(Date date) {
		if (date == null) {
			return null;
		}
		return JavaExtensions.format(date, "dd MMM yyyy");
	}

	@Override
	public JsonElement serialize(DateContribution contribution, Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", contribution.id);
		obj.addProperty("type", contribution.getType());
		obj.addProperty("notPresent", contribution.isNotPresent());
		
		if (contribution.getUser() != null) {
			obj.addProperty("userLogin", contribution.getUser().getLogin());
			obj.addProperty("userImageId", contribution.getUser().getImageId());
			obj.addProperty("userHasImage", contribution.getUser().isHasImage());
		}
		
		if (!contribution.isNotPresent()) {
			obj.addProperty("period", contribution.isPeriod());
			obj.addProperty("collectDate", formatOrNull(contribution.getCollectDate()));
			obj.addProperty("collectStartDate", formatOrNull(contribution.getCollectStartDate()));
			obj.addProperty("collectEndDate", formatOrNull(contribution.getCollectEndDate()));
			obj.addProperty("notSure", contribution.isNotSure());
		}
		return obj;
	}

}
