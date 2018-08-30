package models.serializer;

import java.lang.reflect.Type;

import models.Mission;
import models.User;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UserSimpleJsonSerializer implements JsonSerializer<User> {

	public static UserSimpleJsonSerializer instance = new UserSimpleJsonSerializer();
	 
    private UserSimpleJsonSerializer() {}
 
    public static UserSimpleJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(User user, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", user.id);
		obj.addProperty("imageId", user.getImageId());
		obj.addProperty("login", user.getLogin());
		obj.addProperty("isFB", (user.getFacebookId() != null));
		obj.addProperty("deleted", user.getDeleted());


		return obj;
	}

}
