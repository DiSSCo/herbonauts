package models.serializer.activities;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

import models.activities.Activity;
import play.templates.JavaExtensions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateSinceJsonSerializer implements JsonSerializer<Date> {

	public static DateSinceJsonSerializer instance = new DateSinceJsonSerializer();
	 
    private DateSinceJsonSerializer() {}
 
    public static DateSinceJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Date date, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("timestamp", date.getTime());
		obj.addProperty("since", JavaExtensions.since(date));
		
		return obj;
	}

	
}
