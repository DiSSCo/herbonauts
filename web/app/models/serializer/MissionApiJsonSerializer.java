package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.Mission;

import java.lang.reflect.Type;

public class MissionApiJsonSerializer implements JsonSerializer<Mission> {

	public static MissionApiJsonSerializer instance = new MissionApiJsonSerializer();

    public MissionApiJsonSerializer() {}
 
    public static MissionApiJsonSerializer get() {
        return new MissionApiJsonSerializer();
    }
	
	@Override
	public JsonElement serialize(Mission mission, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", mission.id);
		//obj.addProperty("imageId", mission.getImageId());
		obj.addProperty("title", mission.getTitle());
		obj.addProperty("description", mission.getShortDescription());
		obj.addProperty("specimenCount", mission.getAllSpecimensCount());
		//obj.addProperty("url", "#to-mission");
		
		return obj;
	}

}
