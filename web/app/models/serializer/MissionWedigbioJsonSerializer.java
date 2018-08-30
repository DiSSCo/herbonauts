package models.serializer;

import java.lang.reflect.Type;

import models.Mission;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MissionWedigbioJsonSerializer implements JsonSerializer<Mission> {

	public static MissionWedigbioJsonSerializer instance = new MissionWedigbioJsonSerializer();
	 
    public MissionWedigbioJsonSerializer() {}
 
    public static MissionWedigbioJsonSerializer get() {
        return new MissionWedigbioJsonSerializer();
    }
	
	@Override
	public JsonElement serialize(Mission mission, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("project", mission.getTitle());
		obj.addProperty("contributors", mission.getMembersCount());
		obj.addProperty("numberOfSubjects", mission.getTiledSpecimensCount() );
				
		return obj;
	}

}
