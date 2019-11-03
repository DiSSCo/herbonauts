package models.serializer;

import java.lang.reflect.Type;

import models.Mission;
import models.Specimen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SpecimenSimpleJsonSerializer implements JsonSerializer<Specimen> {

	public static SpecimenSimpleJsonSerializer instance = new SpecimenSimpleJsonSerializer();
	 
    private SpecimenSimpleJsonSerializer() {}
 
    public static SpecimenSimpleJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Specimen specimen, 
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", specimen.id);
		obj.addProperty("genusSpecies", specimen.getGenusSpecies());
		obj.addProperty("family", specimen.getFamily());
		obj.addProperty("genus", specimen.getGenus());
		obj.addProperty("specificEpithet", specimen.getSpecificEpithet());
		obj.addProperty("institute", specimen.getInstitute());
		obj.addProperty("collection", specimen.getCollection());
		obj.addProperty("code", specimen.getCode());
        obj.addProperty("sonneratURL", specimen.getSonneratURL());
        obj.addProperty("tw", specimen.getTileWidth());
        obj.addProperty("th", specimen.getTileHeight());


		obj.addProperty("masterId", specimen.getMaster().id);

		return obj;
	}

}
