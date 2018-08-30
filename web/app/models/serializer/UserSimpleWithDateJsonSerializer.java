package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.User;

import java.lang.reflect.Type;

public class UserSimpleWithDateJsonSerializer implements JsonSerializer<User> {

	public static UserSimpleWithDateJsonSerializer instance = new UserSimpleWithDateJsonSerializer();

    private UserSimpleWithDateJsonSerializer() {}
 
    public static UserSimpleWithDateJsonSerializer get() {
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
		if (user.getRegistrationDate() != null) {
			obj.addProperty("registrationDate", user.getRegistrationDate().getTime());
		}


		return obj;
	}

}
