package models.serializer;

import java.lang.reflect.Type;

import models.Mission;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MissionSimpleJsonSerializer implements JsonSerializer<Mission> {

	public static MissionSimpleJsonSerializer instance = new MissionSimpleJsonSerializer();
	 
    private MissionSimpleJsonSerializer() {}
 
    public static MissionSimpleJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Mission mission, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", mission.id);
		obj.addProperty("imageId", mission.getImageId());
		obj.addProperty("title", mission.getTitle());
		
		return obj;
	}

}
