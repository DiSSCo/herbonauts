package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.User;
import services.Page;
import services.PageImpl;

import java.lang.reflect.Type;

public class PageJsonSerializer implements JsonSerializer<PageImpl<?>> {

	public static PageJsonSerializer instance = new PageJsonSerializer();

    private PageJsonSerializer() {}
 
    public static PageJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(PageImpl<?> page,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();

        obj.addProperty("hasNext", page.getHasNext());
        obj.addProperty("hasPrev", page.getHasPrev());
        obj.addProperty("order", page.getOrder());
        obj.addProperty("end", page.getEnd());
        obj.addProperty("pageCount", page.getPageCount());
        obj.addProperty("pageIndex", page.getPageIndex());
        obj.addProperty("pageSize", page.getPageSize());
        obj.addProperty("sortBy", page.getSortBy());
        obj.addProperty("start", page.getStart());
        obj.addProperty("totalRowCount", page.getTotalRowCount());
        obj.add("list", context.serialize(page.getList()));
		
		return obj;
	}

}
