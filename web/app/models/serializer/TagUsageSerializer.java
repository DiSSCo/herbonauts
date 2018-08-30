package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.stats.TagUsage;
import models.tags.Tag;

import java.lang.reflect.Type;

public class TagUsageSerializer implements JsonSerializer<TagUsage> {

	public static TagUsageSerializer instance = new TagUsageSerializer();

    private TagUsageSerializer() {}
 
    public static TagUsageSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(TagUsage tag,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();

		obj.addProperty("tagId", tag.getTag().getId());
		obj.addProperty("tagLabel", tag.getTag().getTagLabel());
		obj.addProperty("tagType", tag.getTag().getTagType().toString());
		obj.addProperty("countUsage", tag.getCountUsage());
		obj.addProperty("lastUsage", tag.getLastUsage().getTime());

		return obj;
	}

}
