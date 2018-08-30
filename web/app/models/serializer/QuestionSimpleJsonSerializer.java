package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.Specimen;
import models.questions.ContributionQuestion;

import java.lang.reflect.Type;

public class QuestionSimpleJsonSerializer implements JsonSerializer<ContributionQuestion> {

	public static QuestionSimpleJsonSerializer instance = new QuestionSimpleJsonSerializer();

    private QuestionSimpleJsonSerializer() {}
 
    public static QuestionSimpleJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(ContributionQuestion question,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
        obj.addProperty("name", question.getName());
        obj.addProperty("label", question.getLabel());
        obj.addProperty("shortLabel", question.getShortLabel());
		
		return obj;
	}

}
