package models.serializer.contributions;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import models.contributions.DateContribution;
import play.Logger;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.templates.JavaExtensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateContributionForTimelineJsonSerializer implements JsonSerializer<DateContribution> {

	public static DateContributionForTimelineJsonSerializer instance = new DateContributionForTimelineJsonSerializer();
	 
    private DateContributionForTimelineJsonSerializer() {}
 
    public static DateContributionForTimelineJsonSerializer get() {
        return instance;
    }

	@Override
	public JsonElement serialize(DateContribution contribution, Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("title", contribution.getSpecimen().toCompleteString());
		try {
		if (!contribution.isNotPresent()) {
			if (contribution.isPeriod()) {
				obj.addProperty("start", JavaExtensions.format(contribution.getCollectStartDate(), "yyyy-MM-dd'T'HH:mm:ssZ"));
				obj.addProperty("end", JavaExtensions.format(contribution.getCollectEndDate(), "yyyy-MM-dd'T'HH:mm:ssZ"));
				obj.addProperty("isDuration", true);
			} else {
				obj.addProperty("start", JavaExtensions.format(contribution.getCollectDate(), "yyyy-MM-dd'T'HH:mm:ssZ")); //contribution.collectDate.toString());
				obj.addProperty("isDuration", false);
			}
		}
		} catch (Exception e) {
			Logger.error(e, "Error Date JSON");
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", contribution.getSpecimen().id);
		String url = Router.getFullUrl("Specimens.bubble", map);
		obj.addProperty("link", url);
		
		return obj;
	}

}
