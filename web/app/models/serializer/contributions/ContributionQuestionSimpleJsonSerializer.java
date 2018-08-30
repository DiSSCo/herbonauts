package models.serializer.contributions;

import com.google.gson.*;
import models.questions.ContributionQuestion;

import java.lang.reflect.Type;

public class ContributionQuestionSimpleJsonSerializer implements JsonSerializer<ContributionQuestion> {

    public static ContributionQuestionSimpleJsonSerializer instance = new ContributionQuestionSimpleJsonSerializer();

    private final JsonParser jsonParser = new JsonParser();

    private ContributionQuestionSimpleJsonSerializer() {}

    public static ContributionQuestionSimpleJsonSerializer get() {
        return instance;
    }

    @Override
    public JsonElement serialize(ContributionQuestion question,
                                 Type type,
                                 JsonSerializationContext context) {

        JsonObject obj = new JsonObject();

        obj.addProperty("id", question.getId());
        obj.addProperty("name", question.getName());
        obj.addProperty("label", question.getLabel());
        if (question.getShortLabel() != null) {
            obj.addProperty("shortLabel", question.getShortLabel());
        } else {
            obj.addProperty("shortLabel", question.getLabel());
        }
        obj.addProperty("missionId", question.getMissionId());

        return obj;
    }

}
