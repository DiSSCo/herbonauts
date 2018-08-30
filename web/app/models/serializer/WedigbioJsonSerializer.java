package models.serializer;

import java.lang.reflect.Type;

import models.wedigbio.ContribSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class WedigbioJsonSerializer implements JsonSerializer<ContribSet> {

	public static WedigbioJsonSerializer instance = new WedigbioJsonSerializer();
	 
    private WedigbioJsonSerializer() {}
 
    public static WedigbioJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(ContribSet cs, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("numFound", cs.getNumFound());
		obj.addProperty("start", cs.getStart());
		obj.addProperty("rows", cs.getRows());
		obj.add("items", context.serialize(cs.getItems()));
				
		return obj;
	}

}
