package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.User;

import java.lang.reflect.Type;

public class UserContributionJsonSerializer implements JsonSerializer<User> {

	public static UserContributionJsonSerializer instance = new UserContributionJsonSerializer();

    public UserContributionJsonSerializer() {}
 
    public static UserContributionJsonSerializer get() {
        return new UserContributionJsonSerializer();
    }
	
	@Override
	public JsonElement serialize(User user, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", user.id);
		obj.addProperty("imageId", user.getImageId());
		obj.addProperty("login", user.getLogin());
		obj.addProperty("level", user.getLevel());
		//obj.add("missions", context.serialize(user.missions));
		
		
		return obj;
	}

}
