package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import inspectors.Event;
import inspectors.InspectorChain;
import libs.Json;
import models.Announcement;
import models.Mission;
import models.Specimen;
import models.User;
import models.alerts.Alert;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestion;
import models.questions.ContributionQuestionStat;
import models.quiz.Question;
import models.serializer.SpecimenSimpleJsonSerializer;
import models.serializer.contributions.ContributionAnswerJsonSerializer;
import models.serializer.contributions.ContributionQuestionSimpleJsonSerializer;
import models.serializer.contributions.SimpleQuizJsonSerializer;
import play.Logger;
import play.data.validation.Valid;
import play.db.jpa.Transactional;
import services.ContributionConflictService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static controllers.Security.connectedLogin;

/**
 * Controleur gérant les contributions
 */
public class ContributionBoard extends Application {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void showBoard(Long missionId, Long specimenId) {
        Mission mission = Mission.findById(missionId);
        notFoundIfNull(mission);
        Specimen specimen = Specimen.findById(specimenId);
        notFoundIfNull(specimen);


        if (mission.closed) {
            // Ticket #204 http://localhost:9020/missions/3400691/specimens/3425466
            Specimens.show(specimen.getInstitute(), specimen.getCollection(), specimen.getCode());
            return;
        }

        if (!missionId.equals(specimen.getMission().id)) {
            notFound();
        }
        String userJson = Json.toJson(Security.connected());
        render(userJson, mission, specimenId);
    }


    public static void randomBoard(Long missionId) {
        Mission mission = Mission.findById(missionId);
        String userJson = Json.toJson(Security.connected());

        renderTemplate("ContributionBoard/showBoard.html", userJson, mission);
    }


    @Transactional(readOnly = true)
    public static void getSpecimen(Long missionId, Long specimenId) {
        Logger.info("Find specimen " + specimenId);
        Specimen specimen = Specimen.findById(specimenId);
        notFoundIfNull(specimen);
        if (!missionId.equals(specimen.getMission().id)) {
            notFound();
        }
        renderJSON(specimen, SpecimenSimpleJsonSerializer.get());
    }

    @Transactional
    public static void markSeen(Long missionId, Long specimenId) {

        session.put(Missions.CURRENT_MISSION_KEY, missionId);
        session.put(Missions.CURRENT_SPECIMEN_KEY, specimenId);

        Logger.info("Mark specimen seen " + specimenId + " (mission " + missionId + ")");
        User connected = Security.connected();
        if (connected != null) {
            Logger.info("MARK");
            Mission.markSeenV2(missionId, specimenId, connected.id);
        }

        ok();
    }

    @Transactional
    public static void getRandomSpecimen(Long missionId) {
        Logger.info("Random specimen " + missionId);
        User connected = Security.connected();

        Specimen specimen = null;
        if (connected != null) {
            specimen = Mission.getRandomSpecimenV2ForUser(missionId, connected);

            if (specimen == null) {
                Logger.info("No specimen for user, try reset seen specimens");
                Mission.resetSeenSpecimenV2ForUser(missionId, connected);
                specimen = Mission.getRandomSpecimenV2ForUser(missionId, connected);
            }
        } else {
            specimen = Mission.getRandomSpecimenV2ForAnonymous(missionId);
        }

       /* notFoundIfNull(specimen);
        if (!missionId.equals(specimen.getMission().id)) {
            notFound();
        }  */
        renderJSON(specimen, SpecimenSimpleJsonSerializer.get());
    }

    private static void ensureMember(Mission mission) {
        if (mission == null) {
            notFound();
        }
        if (mission != null && !mission.isMember(connectedLogin())) {
            Logger.info("User not member of " + mission.getTitle());
            forbidden("not.member");
        }
    }

    public static void answerReport(Long missionId, Long specimenId, Long questionId) throws IOException {
        User connected = Security.connected();
        if (connected == null) {
            forbidden("not.connected");
        }

        ContributionQuestion question = ContributionQuestion.findById(questionId);

        ContributionAnswer answer = ContributionAnswer.findUserAnswer(connected.id, missionId, specimenId, questionId);
        notFoundIfNull(answer);

        ContributionConflictService.ConflictReport report = ContributionConflictService.userConflictReport(specimenId, question, answer);

        renderJSON(report, ContributionAnswerJsonSerializer.get(), SimpleQuizJsonSerializer.get());
    }

    public static void keepAnswer(Long missionId, Long specimenId, Long questionId) throws IOException {
        User connected = Security.connected();
        if (connected == null) {
            forbidden("not.connected");
        }

        ContributionAnswer answerToSave = ContributionAnswer.findUserAnswer(connected.id, missionId, specimenId, questionId);
        ContributionQuestion question = ContributionQuestion.findById(questionId);

        ContributionConflictService.ConflictReport report = ContributionConflictService.userConflictReport(specimenId, question, answerToSave);
        report.sendAlert = true;

        Mission mission = Mission.findById(missionId);
        ensureMember(mission);

        Specimen specimen = Specimen.findById(specimenId);

        InspectorChain.get().inspect(Event.CONTRIBUTION_V2_ADDED, mission, specimen, connected, question, answerToSave, report);
        ok();
    }

    public static void submitAnswer(Long missionId, Long specimenId, Long questionId) throws IOException {
        User connected = Security.connected();
        if (connected == null) {
            forbidden("not.connected");
        }
        Mission mission = Mission.findById(missionId);
        ensureMember(mission);

        Specimen specimen = Specimen.findById(specimenId);

        JsonNode json = mapper.readTree(request.body);
        ContributionAnswer answer = new ContributionAnswer();
        answer.setQuestionId(questionId);
        answer.setSpecimenId(specimenId);
        answer.setUserId(connected.id);
        answer.setMissionId(missionId);
        answer.setJsonValue(json.toString());

        ContributionAnswer answerToSave = ContributionAnswer.findUserAnswer(connected.id, missionId, specimenId, questionId);
        if (answerToSave == null) {
            answerToSave = answer;
        } else {
            answerToSave.setJsonValue(answer.getJsonValue());
        }
        answerToSave.setMissionId(missionId);
        answerToSave.setDeleted(false);
        answerToSave.setCreatedAt(new Date());
        answerToSave.save();

        Logger.info("Submit answer (mission %s)", answerToSave.getMissionId());

        ContributionQuestion question = ContributionQuestion.findById(answer.getQuestionId());

        // Check conflicts

        //ContributionConflictService.hasConflicts()


        // Specimen last modification
        specimen.setLastModified(new Date());
        specimen.save();

        ContributionConflictService.ConflictReport report = ContributionConflictService.userConflictReport(specimenId, question, answerToSave);
        Logger.info("Submit answer v2 : %s", json.toString());

        boolean noConflicts = "true".equals(request.params.get("noConflicts"));

        if (noConflicts) {
            Logger.info("-----------------------------------");
            Logger.info("Pas de conflit, alerte immédiate");
            Logger.info("-----------------------------------");
            report.sendAlert = true;
        } else {
            Logger.info("-----------------------------------");
            Logger.info("Si conflit, alerte différée");
            Logger.info("-----------------------------------");
            report.sendAlert = false;
        }

        InspectorChain.get().inspect(Event.CONTRIBUTION_V2_ADDED, mission, specimen, connected, question, answerToSave, report);



        // Setting mission ID
        //List<ContributionQuestion> questions = Json.parseContributionQuestions(json);

        renderJSON(report, ContributionAnswerJsonSerializer.get(), SimpleQuizJsonSerializer.get());

        //renderText("ok");
    }

    public static void cancelAnswer(Long missionId, Long specimenId, Long questionId) throws IOException {
        User connected = Security.connected();

        ContributionAnswer answerToSave = ContributionAnswer.findUserAnswer(connected.id, missionId, specimenId, questionId);

        if (answerToSave != null) {
            answerToSave.setDeleted(true);
            answerToSave.save();
        } else {
            notFound();
        }

        ContributionQuestion question = ContributionQuestion.findById(questionId);
        ContributionConflictService.ConflictReport report = ContributionConflictService.userConflictReport(specimenId, question, answerToSave);

        renderJSON(report, ContributionAnswerJsonSerializer.get(), SimpleQuizJsonSerializer.get());
        //renderText("ok");
    }

    public static void getUserAnswers(Long missionId, Long specimenId) {
        User connected = Security.connected();

        if (connected == null) {
            renderJSON(null);
        } else {
            List<ContributionAnswer> answers = ContributionAnswer.findUserAnswers(connected.id, missionId, specimenId);
            renderJSON(answers, ContributionAnswerJsonSerializer.get());
        }

    }

    public static void getStats(Long missionId, Long specimenId) {
        List<ContributionQuestionStat> stats = ContributionQuestionStat.find("specimenId = :specimenId")
                .setParameter("specimenId", specimenId)
                .fetch();

        renderJSON(stats, ContributionAnswerJsonSerializer.get(), ContributionQuestionSimpleJsonSerializer.get());
    }

}
