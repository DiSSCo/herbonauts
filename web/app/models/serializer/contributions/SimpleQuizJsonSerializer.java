package models.serializer.contributions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.contributions.DateContribution;
import models.quiz.Quiz;
import play.Logger;
import play.mvc.Router;
import play.templates.JavaExtensions;

import java.lang.reflect.Type;
import java.util.HashMap;

public class SimpleQuizJsonSerializer implements JsonSerializer<Quiz> {

	public static SimpleQuizJsonSerializer instance = new SimpleQuizJsonSerializer();

    private SimpleQuizJsonSerializer() {}
 
    public static SimpleQuizJsonSerializer get() {
        return instance;
    }

	@Override
	public JsonElement serialize(Quiz quiz, Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("id", quiz.getId());
        obj.addProperty("title", quiz.getTitle());
        obj.addProperty("name", quiz.getName());

		return obj;
	}

}
