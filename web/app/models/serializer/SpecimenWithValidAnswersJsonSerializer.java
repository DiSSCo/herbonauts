package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.Specimen;

import java.lang.reflect.Type;

public class SpecimenWithValidAnswersJsonSerializer implements JsonSerializer<Specimen> {

	public static SpecimenWithValidAnswersJsonSerializer instance = new SpecimenWithValidAnswersJsonSerializer();

    private SpecimenWithValidAnswersJsonSerializer() {}
 
    public static SpecimenWithValidAnswersJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Specimen specimen, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("herbonautes_id", specimen.id);
		obj.addProperty("family", specimen.getFamily());
		obj.addProperty("genus", specimen.getGenus());
        obj.addProperty("specificEpiteth", specimen.getSpecificEpithet());
		obj.addProperty("institute", specimen.getInstitute());
		obj.addProperty("collection", specimen.getCollection());
		obj.addProperty("code", specimen.getCode());
        obj.addProperty("sonneratURL", specimen.getSonneratURL());
		obj.addProperty("genusSpecies", specimen.getGenusSpecies());

        obj.add("validAnswers", context.serialize(specimen.allValidAnswers()));
		
		return obj;
	}

}
