package transfert;

import java.lang.reflect.Type;
import java.util.HashMap;

import play.mvc.Router;

import models.Botanist;
import models.Mission;
import models.Specimen;
import models.SpecimenFamilyGenus;
import models.User;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SearchResultJsonSerializer implements JsonSerializer<SearchResult> {
	
	public static SearchResultJsonSerializer instance = new SearchResultJsonSerializer();
	 
    private SearchResultJsonSerializer() {}
 
    public static SearchResultJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(SearchResult searchResult, Type type,
			JsonSerializationContext context) {
		
		int count = 0;
		
		JsonArray ar = new JsonArray();
		
		if (searchResult.missions != null) {
			for (Mission mission : searchResult.missions) {
				JsonObject obj = new JsonObject();
				obj.addProperty("id", mission.id);
				obj.addProperty("label", mission.getTitle());
				obj.addProperty("category", "missions");
				obj.addProperty("showImage", true);
				obj.addProperty("hasImage", mission.isHasImage());
				
				ar.add(obj);
			}
			count += searchResult.missions.size();
		}
		
		if (searchResult.botanists != null) {
			for (Botanist botanist : searchResult.botanists) {
				JsonObject obj = new JsonObject();
				obj.addProperty("id", botanist.id);
				obj.addProperty("label", botanist.getName());
				obj.addProperty("category", "botanists");
				
				obj.addProperty("showImg", false);
				
				ar.add(obj);
			}
			
			count += searchResult.botanists.size();
		}
		
		if (searchResult.familiesGenuses != null) {
			for (SpecimenFamilyGenus familyGenus : searchResult.familiesGenuses) {
				
				JsonObject obj = new JsonObject();
				obj.addProperty("label", familyGenus.getFamily() + " " + familyGenus.getGenus());
				obj.addProperty("family", familyGenus.getFamily());
				obj.addProperty("genus", familyGenus.getGenus());
				obj.addProperty("category", "specimens");
				obj.addProperty("showImg", false);
				
				ar.add(obj);
			}
			
			count += searchResult.familiesGenuses.size();
		}
		
		if (searchResult.specimens != null) {
			for (Specimen specimen : searchResult.specimens) {
				
				JsonObject obj = new JsonObject();
				obj.addProperty("label", specimen.getCode() + " (" + specimen.getFamily() + " " + specimen.getGenus() + ")");
				obj.addProperty("category", "specimens");
				obj.addProperty("showImg", false);
				obj.addProperty("institute", specimen.getInstitute());
				obj.addProperty("collection", specimen.getCollection());
				obj.addProperty("code", specimen.getCode());
				
				ar.add(obj);
			}
			
			count += searchResult.specimens.size();
		}

		if (searchResult.users != null) {
			for (User user : searchResult.users) {
				
				JsonObject obj = new JsonObject();
				obj.addProperty("label", user.getLogin());
				obj.addProperty("category", "users");
				obj.addProperty("showImg", true);
				obj.addProperty("hasImage", user.isHasImage());
				
				ar.add(obj);
			}
			
			count += searchResult.users.size();
		}
		
		if (count == 0) {
			JsonObject noResult = new JsonObject();
			noResult.addProperty("noResult", true);
			ar.add(noResult);
		}
		
		return ar;
	}

}
