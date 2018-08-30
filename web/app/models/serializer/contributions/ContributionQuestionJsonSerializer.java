package models.serializer.contributions;

import com.google.gson.*;
import models.contributions.UnusableContribution;
import models.questions.ContributionQuestion;

import java.lang.reflect.Type;

public class ContributionQuestionJsonSerializer implements JsonSerializer<ContributionQuestion> {

    public static ContributionQuestionJsonSerializer instance = new ContributionQuestionJsonSerializer();

    private final JsonParser jsonParser = new JsonParser();

    private ContributionQuestionJsonSerializer() {}

    public static ContributionQuestionJsonSerializer get() {
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
        obj.addProperty("minLevel", question.getMinLevel());
        obj.add("neededQuiz", context.serialize(question.getNeededQuiz()));
        obj.addProperty("validationLevel", question.getValidationLevel());
        obj.addProperty("templateId", question.getTemplateId());
        obj.addProperty("helpHTML", question.getHelpHTML());
        obj.addProperty("editable", question.getEditable());

        obj.addProperty("defaultForMission", question.getDefaultForMission());
        obj.addProperty("mandatoryForMission", question.getMandatoryForMission());

        JsonElement configuration = jsonParser.parse(question.getConfiguration());
        obj.add("configuration", configuration);

        obj.addProperty("usedCount", question.getUsedCount());

        return obj;
    }

}
