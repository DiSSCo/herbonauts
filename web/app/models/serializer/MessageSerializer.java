package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.discussions.Message;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MessageSerializer implements JsonSerializer<Message> {

	public static MessageSerializer instance = new MessageSerializer();

    private MessageSerializer() {}
 
    public static MessageSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Message message,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", message.getId());
		obj.addProperty("author", message.getAuthor().getLogin());
		obj.addProperty("discussionId", message.getDiscussion().getId());
		obj.addProperty("resolution", message.isResolution());
		obj.addProperty("date", message.getLastUpdateDate().getTime());
		obj.addProperty("imageId", message.getAuthor().getImageId());
		obj.addProperty("moderator", message.getModeratorLogin() == null ? "" : message.getModeratorLogin());
		obj.addProperty("text", message.getModeratorLogin() == null ? message.getText() : null);
		obj.addProperty("first", message.isFirst() == null ? false : message.isFirst());
		return obj;
	}

}
