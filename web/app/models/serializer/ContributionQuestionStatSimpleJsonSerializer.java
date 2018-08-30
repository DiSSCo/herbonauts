package models.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import models.Specimen;
import models.questions.ContributionQuestion;
import models.questions.ContributionQuestionStat;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.lang.reflect.Type;

public class ContributionQuestionStatSimpleJsonSerializer implements JsonSerializer<ContributionQuestionStat> {

	public static ContributionQuestionStatSimpleJsonSerializer instance = new ContributionQuestionStatSimpleJsonSerializer();

    private ContributionQuestionStatSimpleJsonSerializer() {}
 
    public static ContributionQuestionStatSimpleJsonSerializer get() {
        return instance;
    }
	
	@Override
	public JsonElement serialize(ContributionQuestionStat stat,
			Type type,
			JsonSerializationContext context) {
		
		JsonObject obj = new JsonObject();

        obj.addProperty("answerCount", stat.getAnswerCount());
        obj.addProperty("validated", stat.getValidated());
        obj.addProperty("conflicts", stat.getConflicts());
        obj.add("question", context.serialize(stat.getQuestion()));

		
		return obj;
	}

}
