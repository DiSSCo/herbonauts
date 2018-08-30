package models.serializer;

import com.google.gson.*;
import helpers.GsonUtils;
import models.discussions.Discussion;
import models.discussions.DiscussionCategory;
import models.discussions.Message;
import models.tags.Tag;
import models.tags.TagLinkType;

import java.lang.reflect.Type;
import java.util.List;

public class DiscussionSerializer implements JsonSerializer<Discussion> {

	public static DiscussionSerializer instance = new DiscussionSerializer();

    private DiscussionSerializer() {}
 
    public static DiscussionSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Discussion discussion,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();

		obj.addProperty("id", discussion.getId());
		obj.addProperty("title", discussion.getTitle());
		obj.addProperty("resolved", discussion.isResolved());
		obj.addProperty("author", discussion.getAuthor().getLogin());
		if (discussion.getCategories() != null && !discussion.getCategories().isEmpty()) {
			GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
			gsonBuilder.registerTypeAdapter(DiscussionCategory.class, DiscussionCategorySerializer.get());
			obj.add("categories", gsonBuilder.create().toJsonTree(discussion.getCategories()));
		}

		if (discussion.getMessages() != null && !discussion.getMessages().isEmpty()) {
			GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
			gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
			obj.add("messages", gsonBuilder.create().toJsonTree(discussion.getMessages()));
		}

		List<Tag> tags = Tag.findByLinkTypeAndTargetId(TagLinkType.DISCUSSION, discussion.getId());
		if(tags != null && !tags.isEmpty()) {
			GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
			gsonBuilder.registerTypeAdapter(Tag.class, TagSerializer.get());
			obj.add("tags", gsonBuilder.create().toJsonTree(tags));
		}
		return obj;
	}

}
