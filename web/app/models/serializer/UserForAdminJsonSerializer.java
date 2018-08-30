package models.serializer;

import java.lang.reflect.Type;

import models.Mission;
import models.User;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UserForAdminJsonSerializer implements JsonSerializer<User> {

	public static UserForAdminJsonSerializer instance = new UserForAdminJsonSerializer();
	 
    private UserForAdminJsonSerializer() {}
 
    public static UserForAdminJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(User user, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", user.id);
		obj.addProperty("login", user.getLogin());
		obj.addProperty("fullName", user.getFullName());
        obj.addProperty("firstName", user.getFirstName());
        obj.addProperty("lastName", user.getLastName());
		obj.addProperty("email", user.getEmail());
        obj.addProperty("leader", user.isLeader());
        obj.addProperty("admin", user.isAdmin());
        obj.addProperty("team", user.isTeam());
		obj.addProperty("isFB", (user.getFacebookId() != null));
		
		return obj;
	}

}
