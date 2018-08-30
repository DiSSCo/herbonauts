package models.serializer.contributions;

import com.google.gson.*;
import models.questions.ContributionAnswer;

import java.lang.reflect.Type;

public class ContributionAnswerSimpleJsonSerializer implements JsonSerializer<ContributionAnswer> {

    public static ContributionAnswerSimpleJsonSerializer instance = new ContributionAnswerSimpleJsonSerializer();

    private final JsonParser jsonParser = new JsonParser();

    private ContributionAnswerSimpleJsonSerializer() {}

    public static ContributionAnswerSimpleJsonSerializer get() {
        return instance;
    }

    @Override
    public JsonElement serialize(ContributionAnswer answer,
                                 Type type,
                                 JsonSerializationContext context) {

        JsonObject e = (JsonObject) jsonParser.parse(answer.getJsonValue());

        e.addProperty("specimenId", answer.getSpecimenId());

        return e;
    }

}
