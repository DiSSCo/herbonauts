package models.serializer.contributions;

import com.google.gson.*;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;

import java.lang.reflect.Type;

public class ContributionAnswerJsonSerializer implements JsonSerializer<ContributionAnswer> {

    public static ContributionAnswerJsonSerializer instance = new ContributionAnswerJsonSerializer();

    private final JsonParser jsonParser = new JsonParser();

    private ContributionAnswerJsonSerializer() {}

    public static ContributionAnswerJsonSerializer get() {
        return instance;
    }

    @Override
    public JsonElement serialize(ContributionAnswer answer,
                                 Type type,
                                 JsonSerializationContext context) {

        JsonObject obj = new JsonObject();

        obj.addProperty("id", answer.getId());
        obj.addProperty("questionId", answer.getQuestionId());
        obj.addProperty("specimenId", answer.getSpecimenId());
        obj.addProperty("userId", answer.getUserId());
        obj.addProperty("userLogin", answer.getUserLogin());

        if (answer.getJsonValue() != null) {
            JsonElement jsonValue = jsonParser.parse(answer.getJsonValue());
            obj.add("jsonValue", jsonValue);
        }

        return obj;
    }

}
