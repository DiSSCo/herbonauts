package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.tags.Tag;

import java.lang.reflect.Type;

public class TagSerializer implements JsonSerializer<Tag> {

	public static TagSerializer instance = new TagSerializer();

    private TagSerializer() {}
 
    public static TagSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Tag tag,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();

		obj.addProperty("tagId", tag.getId());
		obj.addProperty("tagLabel", tag.getTagLabel());
		obj.addProperty("tagType", tag.getTagType().toString());

		return obj;
	}

}
