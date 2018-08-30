package models.serializer;

import java.lang.reflect.Type;

import models.Botanist;
import models.Mission;
import models.User;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BotanistLabelValueJsonSerializer implements JsonSerializer<Botanist> {

	public static BotanistLabelValueJsonSerializer instance = new BotanistLabelValueJsonSerializer();
	 
    private BotanistLabelValueJsonSerializer() {}
 
    public static BotanistLabelValueJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Botanist botanist, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("value", botanist.id);
		obj.addProperty("login", botanist.getName());
		
		return obj;
	}

}
