package models.serializer;

import com.google.gson.*;
import play.templates.JavaExtensions;

import java.lang.reflect.Type;
import java.sql.Timestamp;

public class TimestampJsonSerializer implements JsonSerializer<Timestamp> {

	public static TimestampJsonSerializer instance = new TimestampJsonSerializer();

    private TimestampJsonSerializer() {}
 
    public static TimestampJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Timestamp date, 
			Type type,
			JsonSerializationContext context) {
		
		return new JsonPrimitive(date.getTime());
	}

	
}
