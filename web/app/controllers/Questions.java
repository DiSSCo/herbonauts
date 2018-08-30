package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import libs.Json;
import models.Botanist;
import models.RegionLevel1;
import models.RegionLevel2;
import models.questions.ContributionQuestion;
import models.quiz.Quiz;
import models.references.Reference;
import models.references.ReferenceRecord;
import models.serializer.contributions.ContributionQuestionJsonSerializer;
import models.serializer.contributions.SimpleQuizJsonSerializer;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.JPA;

import java.io.IOException;
import java.util.List;

public class Questions extends Application {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void adminIndex() throws IOException {
        Security.forbiddenIfNotTeam();
        render();
    }

    public static void adminEdit(Long referenceId) {
        Security.forbiddenIfNotTeam();
        List<ReferenceRecord> records = Reference.findAllRecords(referenceId);
        render(records);
    }


    // ---- API ----

    public static void allTemplates() throws IOException {
        Security.forbiddenIfNotSpecial();
        List<ContributionQuestion> questions = ContributionQuestion.findAllTemplates();
        renderJSON(questions, ContributionQuestionJsonSerializer.get(), SimpleQuizJsonSerializer.get());
    }


    public static void deleteTemplate() throws IOException {
        Security.forbiddenIfNotTeam();
        JsonNode json = mapper.readTree(request.body);

        ContributionQuestion question = Json.parseContributionQuestion(json);

        if (ContributionQuestion.isUsedInMission(question.id)) {
            forbidden();
        }

        //question.id;
        ContributionQuestion.delete("id = ?", question.id);

        ok();

    }

    public static void saveTemplate() throws IOException {
        Security.forbiddenIfNotTeam();
        JsonNode json = mapper.readTree(request.body);

        ContributionQuestion question = Json.parseContributionQuestion(json);

        if (question.id != null) {
            Logger.info("Create template %s", question.getLabel());


            ContributionQuestion existing = ContributionQuestion.findById(question.id);

            if (!Security.isAdmin() && (existing.getEditable() != true)) {
                forbidden("Question is not editable");
            }

            existing.setLabel(question.getLabel());
            existing.setShortLabel(question.getShortLabel());
            existing.setName(question.getName());
            existing.setMinLevel(question.getMinLevel());
            if (question.getNeededQuiz() != null) {
                Logger.info("- Quiz : " + question.getNeededQuiz().id);
            }
            if (question.getNeededQuiz() != null) {
                if (question.getNeededQuiz().id == 0 || question.getNeededQuiz().id == null) {
                    existing.setNeededQuiz(null);
                } else if (Quiz.findById(question.getNeededQuiz().id) != null) {
                    existing.setNeededQuiz(question.getNeededQuiz());
                }
            } else {
                existing.setNeededQuiz(null);
            }
            existing.setValidationLevel(question.getValidationLevel());
            existing.setConfiguration(question.getConfiguration());
            existing.setHelpHTML(question.getHelpHTML());

            Logger.info("Save question " + question.getEditable());
            if (Security.isAdmin()) {
                existing.setDefaultForMission(question.getDefaultForMission());
                existing.setMandatoryForMission(question.getMandatoryForMission());
                existing.setEditable(question.getEditable());
            }

            Logger.info("Save help '%s'", existing.getHelpHTML());


            existing.save();
        } else {
            Logger.info("Create template %s", question.getLabel());

            Boolean existing = ContributionQuestion.count("name = ?", question.getName()) > 0;
            if (existing) {
                error(400, "Already exists");
            }
            question.setEditable(true);

            question.save();

        }

        //Logger.info("Saving template %s", json.toString());
        //+  request.body.toString());
        renderJSON(question, ContributionQuestionJsonSerializer.get(), SimpleQuizJsonSerializer.get());
    }

    public static void saveTemplateOrder() throws IOException {
        Security.forbiddenIfNotTeam();
        JsonNode json = mapper.readTree(request.body);

        List<ContributionQuestion> questions = Json.parseContributionQuestions(json);

        for (ContributionQuestion question : questions) {
            ContributionQuestion q = ContributionQuestion.findById(question.id);
            q.setSortIndex(question.getSortIndex());
            q.save();
            Logger.info(q.getName() + " -> " + q.getSortIndex());
        }

        Logger.info("Change order");

        renderJSON(questions);
    }

    public static void removeTemplate() {

    }

}
