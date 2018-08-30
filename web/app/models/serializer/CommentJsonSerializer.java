package models.serializer;

import java.lang.reflect.Type;

import models.Mission;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CommentJsonSerializer implements JsonSerializer<Mission> {

	public static CommentJsonSerializer instance = new CommentJsonSerializer();
	 
    private CommentJsonSerializer() {}
 
    public static CommentJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Mission mission, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", mission.id);
		obj.addProperty("title", mission.getTitle());
		obj.addProperty("url", "#to-mission");
		
		return obj;
	}

}
