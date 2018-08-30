package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.Specimen;

import java.lang.reflect.Type;

public class SpecimenSimpleWithMissionJsonSerializer implements JsonSerializer<Specimen> {

	public static SpecimenSimpleWithMissionJsonSerializer instance = new SpecimenSimpleWithMissionJsonSerializer();

    private SpecimenSimpleWithMissionJsonSerializer() {}
 
    public static SpecimenSimpleWithMissionJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Specimen specimen, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", specimen.id);
		obj.addProperty("family", specimen.getFamily());
		obj.addProperty("genus", specimen.getGenus());
		obj.addProperty("institute", specimen.getInstitute());
		obj.addProperty("collection", specimen.getCollection());
		obj.addProperty("code", specimen.getCode());
		obj.addProperty("genusSpecies", specimen.getGenusSpecies());
        obj.add("mission", context.serialize(specimen.getMission()));
		
		return obj;
	}

}
