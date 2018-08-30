package libs;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Cart;
import models.MissionCartQuery;
import models.User;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import models.quiz.Quiz;
import models.references.Reference;
import models.references.ReferenceRecord;
import models.references.ReferenceRecordInfo;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Json {

    private static ObjectMapper mapper = new ObjectMapper();


    public static JsonNode toFullJson(ContributionQuestion contributionQuestion) {
        return null;

    }

    public static Cart parseCart(String cartJson) throws IOException {
        Cart cart = cartJson != null ? mapper.readValue(cartJson, Cart.class) : null;
        return cart;
    }

    public static JsonNode parse(String json) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static String toJson(Cart cart) throws JsonProcessingException {
        return mapper.writeValueAsString(cart);
    }

    public static String toJson(User user)  {
        if (user == null) {
            return null;
        }

        ObjectNode node = mapper.createObjectNode();

        node.put("id", user.id);
        node.put("level", user.getLevel());


        return node.toString();
    }

    /*public static ContributionAnswer parseContributionAnswer(JsonNode jsonNode) {
        ContributionAnswer answer = new ContributionAnswer();

        if (jsonNode.has("id")) answer.id = jsonNode.get("id").asLong();

        if (jsonNode.has("userId")) answer.setUserId(jsonNode.get("userId").asLong());
        if (jsonNode.has("specimenId")) answer.setSpecimenId(jsonNode.get("specimenId").asLong());
        if (jsonNode.has("questionId")) answer.setQuestionId(jsonNode.get("questionId").asLong());
        if (jsonNode.has("answerId")) answer.setFromAnswerId(jsonNode.get("fromAnswerId").asLong());

        if (jsonNode.has("jsonValue")) answer.setJsonValue(jsonNode.get("jsonValue").toString());

        return answer;
    }*/


    public static ContributionQuestion parseContributionQuestion(JsonNode jsonNode) {
        ContributionQuestion question = new ContributionQuestion();

        if (jsonNode.has("id")) question.id = jsonNode.get("id").asLong();

        if (jsonNode.has("label")) question.setLabel(jsonNode.get("label").asText());
        if (jsonNode.has("shortLabel")) question.setShortLabel(jsonNode.get("shortLabel").asText());
        if (jsonNode.has("sortIndex")) question.setSortIndex(jsonNode.get("sortIndex").asLong());
        if (jsonNode.has("minLevel")) question.setMinLevel(jsonNode.get("minLevel").asLong());
        if (jsonNode.hasNonNull("neededQuiz") && jsonNode.get("neededQuiz").hasNonNull("id")) {
            question.setNeededQuiz(new Quiz(jsonNode.get("neededQuiz").get("id").asLong()));
        }
        if (jsonNode.has("validationLevel")) question.setValidationLevel(jsonNode.get("validationLevel").asLong());
        if (jsonNode.has("name")) question.setName(jsonNode.get("name").asText());
        if (jsonNode.has("configuration")) question.setConfiguration(jsonNode.get("configuration").toString());
        if (jsonNode.has("templateId")) question.setTemplateId(jsonNode.get("templateId").asLong());
        if (jsonNode.has("helpHTML")) question.setHelpHTML(jsonNode.get("helpHTML").asText());

        if (jsonNode.has("defaultForMission")) question.setDefaultForMission(jsonNode.get("defaultForMission").asBoolean());
        if (jsonNode.has("mandatoryForMission")) question.setMandatoryForMission(jsonNode.get("mandatoryForMission").asBoolean());
        if (jsonNode.has("editable")) question.setEditable(jsonNode.get("editable").asBoolean());


        return question;
    }

    public static MissionCartQuery parseCartQuery(JsonNode jsonNode) {

        MissionCartQuery query = new MissionCartQuery();

        if (jsonNode.has("id"))             query.id = jsonNode.get("id").asLong();
        if (jsonNode.has("missionId"))      query.setMissionId(jsonNode.get("missionId").asLong());
        if (jsonNode.has("noCollectInfo"))  query.setNoCollectInfo(jsonNode.get("noCollectInfo").asBoolean());
        if (jsonNode.has("allSelected"))    query.setAllSelected(jsonNode.get("allSelected").asBoolean());
        if (jsonNode.has("allSelectedDraft"))    query.setAllSelectedDraft(jsonNode.get("allSelectedDraft").asBoolean());
        if (jsonNode.has("hits"))    query.setHits(jsonNode.get("hits").asLong());

        HashMap<String, String> terms = new HashMap<String, String>();
        if (jsonNode.hasNonNull("terms")) {

            Iterator<String> fieldIt = jsonNode.get("terms").fieldNames();
            while (fieldIt.hasNext()) {
                String fieldName = fieldIt.next();
                terms.put(fieldName, jsonNode.get("terms").get(fieldName).asText());
            }
        }
        query.setTerms(terms);
        //if (jsonNode.has("missionId"))      query.setMissionId(jsonNode.get("missionId").asLong());
        //if (jsonNode.has("id"))    query.id = jsonNode.get("id").asLong();

        ArrayList<String> selection = new ArrayList<String>();
        if (jsonNode.has("selection")) {
            for (JsonNode node : jsonNode.get("selection")) {
                selection.add(node.asText());
            }
        }
        query.setSelection(selection);

        ArrayList<String> selectionDraft = new ArrayList<String>();
        if (jsonNode.has("selectionDraft")) {
            for (JsonNode node : jsonNode.get("selectionDraft")) {
                selectionDraft.add(node.asText());
            }
        }
        query.setSelectionDraft(selectionDraft);

        return query;
    }

    public static ReferenceRecord parseReferenceRecord(JsonNode jsonNode) {
        ReferenceRecord record = new ReferenceRecord();

        if (jsonNode.has("id")) record.id = jsonNode.get("id").asLong();
        if (jsonNode.has("label")) record.setLabel(jsonNode.get("label").asText());
        if (jsonNode.has("value")) record.setValue(jsonNode.get("value").asText());

        if (jsonNode.has("parent")) {

            if (jsonNode.get("parent").hasNonNull("id")) {
                ReferenceRecord parent = new ReferenceRecord();
                parent.id = jsonNode.get("parent").get("id").asLong();
                record.setParent(parent);
            }


        }

        if (jsonNode.hasNonNull("info")) {
            List<ReferenceRecordInfo> info = new ArrayList<ReferenceRecordInfo>();
            for (JsonNode i : jsonNode.get("info")) {
                String name = i.get("name").asText();
                String value = i.hasNonNull("value") ? i.get("value").asText() : null;
                Logger.info("add info %s - %s", name, value);
                info.add(new ReferenceRecordInfo(record.id, name, value));
            }
            record.setInfo(info);
        }


        //


        // if (jsonNode.has("info")) record.setInfo(.toString());

        return record;
    }


    public static List<ContributionQuestion> parseContributionQuestions(JsonNode jsonNode) {
        ArrayList<ContributionQuestion> questions = new ArrayList<ContributionQuestion>();

        for (JsonNode jsonQuestion : jsonNode) {
            questions.add(parseContributionQuestion(jsonQuestion));
        }

        return questions;
    }


}
