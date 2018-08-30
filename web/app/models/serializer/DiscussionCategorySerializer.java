package models.serializer;

import com.google.gson.*;
import models.discussions.DiscussionCategory;

import java.lang.reflect.Type;

public class DiscussionCategorySerializer implements JsonSerializer<DiscussionCategory> {

	public static DiscussionCategorySerializer instance = new DiscussionCategorySerializer();

    private DiscussionCategorySerializer() {}
 
    public static DiscussionCategorySerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(DiscussionCategory discussionCategory,
			Type type,
			JsonSerializationContext context) {

		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", discussionCategory.getId());
		obj.addProperty("label", discussionCategory.getLabel());
		obj.addProperty("nbDiscussions", discussionCategory.getDiscussions().size());
		return obj;
	}

}
