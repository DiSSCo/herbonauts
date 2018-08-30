package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import controllers.Users;
import models.User;
import play.templates.JavaExtensions;

import java.lang.reflect.Type;
import java.util.Date;

public class AnswerContributionSimpleJsonSerializer implements JsonSerializer<Users.AnswerWithQuestionAndMission> {

	public static AnswerContributionSimpleJsonSerializer instance = new AnswerContributionSimpleJsonSerializer();

    private AnswerContributionSimpleJsonSerializer() {}
 
    public static AnswerContributionSimpleJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(Users.AnswerWithQuestionAndMission answer,
			Type type,
			JsonSerializationContext context) {

		JsonObject obj = new JsonObject();


		obj.add("specimen", context.serialize(answer.specimen));
		obj.addProperty("missionId", answer.mission.id);
		obj.addProperty("missionTitle", answer.mission.getTitle());
		obj.addProperty("since", JavaExtensions.since(new Date(answer.createdAt)));
		//obj.add("mission", context.serialize(answer.mission));

		obj.add("question", context.serialize(answer.question));


		return obj;
	}

}
