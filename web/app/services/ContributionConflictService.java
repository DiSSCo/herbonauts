package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import conf.Herbonautes;
import helpers.DateUtils;
import libs.GPS;
import models.Specimen;
import models.questions.*;
import models.questions.special.StaticAnswer;
import models.references.Reference;
import models.references.ReferenceRecord;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import play.Logger;

import java.io.IOException;
import java.util.*;

public class ContributionConflictService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final ConflictReport userConflictReport(Long specimenId, ContributionQuestion question, ContributionAnswer answer)
    throws IOException {

        ConflictReport report = new ConflictReport();



        //ContributionAnswer userAnswer = ContributionAnswer.findUserAnswer(answer.getUserId(), null, answer.getSpecimenId(), answer.getQuestionId());
        List<ContributionAnswer> allAnswers = ContributionAnswer.findAllAnswersForSpecimen(null,
                            answer.getSpecimenId(),
                            question.getId());

        report.answer = answer;
        report.conflictAnswers = new ArrayList<ContributionAnswer>();
        report.noConflictAnswers = new ArrayList<ContributionAnswer>();

        for (ContributionAnswer other : allAnswers) {
            if (other.getUserId().equals(answer.getUserId())) {
                continue;
            }
            if (hasConflicts(question, other, answer)) {
                report.conflictAnswers.add(other);
            } else {
                report.noConflictAnswers.add(other);
            }
        }



        ContributionQuestionStat stat = updateQuestionStats(specimenId, question, allAnswers);

        //ContributionAnswer validAnswer = buildValidAnswer(question, allAnswers);
        report.stat = stat;

        updateSpecimenStats(question.getMissionId(), answer.getSpecimenId());
        updateSpecimenUserStat(question.getMissionId(), answer.getSpecimenId(), answer.getUserId());


        return report;
    }

    public static final void updateSpecimenUserStat(Long missionId, Long specimenId, Long userId) {
        Logger.info("--------");
        Logger.info("Update SPECIMEN-USER stats");

        List<ContributionQuestion> allQuestions = ContributionQuestion.findAllActiveForMission(missionId);
        final HashMap<Long, ContributionQuestion> questionById = new HashMap<Long, ContributionQuestion>();
        for (ContributionQuestion question : allQuestions) {
            Logger.info("Questions " + question.id + " -> " + question.getName());
            questionById.put(question.id, question);
        }
        List<ContributionAnswer> allUserAnswers = ContributionAnswer.findUserAnswers(userId, missionId, specimenId);

        ContributionSpecimenUserStat stat = ContributionSpecimenUserStat.findByUserAndSpecimen(userId, specimenId);
        if (stat == null) {
            stat = new ContributionSpecimenUserStat();
            stat.setSpecimen(new Specimen(specimenId));
            stat.setMissionId(missionId);
            stat.setUserId(userId);
            stat.setMarkedSeen(true);
        }
        stat.setLastModifiedAt(new Date());

        boolean unusable = false;
        HashSet<Long> answeredQuestions = new HashSet<Long>();
        for (ContributionAnswer answer : allUserAnswers) {

            ContributionQuestion q = questionById.get(answer.getQuestionId());
            //Logger.info("- " + answer.getQuestionId() + " - " + q);
            if (q == null) {
                Logger.info("Question " + answer.getQuestionId() + " was deleted");
                continue;
            }
            if ("unusable".equals(q.getName())) {
                unusable = true;
            }
            answeredQuestions.add(answer.getQuestionId());
        }

        Long minUnansweredLevel = null;
        Logger.info("Answered to questions " + answeredQuestions);
        for (ContributionQuestion q : allQuestions) {
            if (!answeredQuestions.contains(q.getId())) {
                Logger.info("No answer to " + q.getLabel() + " (" + q.getMinLevel() + ")");
                if (!"unusable".equals(q.getName())) {
                   if (minUnansweredLevel == null || minUnansweredLevel > q.getMinLevel()) {
                       minUnansweredLevel = q.getMinLevel();
                   }
                }
            }
        }
        stat.setMinUnansweredLevel(minUnansweredLevel);
        stat.setMarkedUnusable(unusable);

        stat.save();
    }

    public static final void updateSpecimenStats(Long missionId, Long specimenId) {

        Logger.info("Update SPECIMEN stats");

        ContributionSpecimenStat specimenStat = ContributionSpecimenStat.findById(specimenId);

        if (specimenStat == null) {
            specimenStat = new ContributionSpecimenStat();
            specimenStat.setSpecimen(new Specimen(specimenId));
            //new Specimen(specimenId));
            specimenStat.setMissionId(missionId);
        } else {
            Logger.info("Already have stats for specimen %d", specimenId);
        }

        List<ContributionQuestionStat> allStats = ContributionQuestionStat.find("specimenId = ?", specimenId).fetch();
        List<ContributionQuestion> allMissionQuestions = ContributionQuestion.findAllActiveForMission(missionId);

        HashSet<Long> questionsIdWithStat = new HashSet<Long>();

        long answerCount = 0l;
        //boolean validated = false;
        boolean conflicts = false;
        Long minUsefulLevel = null;
        boolean unusableValidated = false;
        boolean validated = true;
        //Long.MAX_VALUE;
        for (ContributionQuestionStat questionStat : allStats) {

            //Logger.info("- question %s : valid %s", questionStat.getQuestion().getName(), questionStat.getValidated());

            // Fix 38 (compte complet si inutilisable non compté)
            if (!"unusable".equals(questionStat.getQuestion().getName()) && !questionStat.getValidated()) {
                validated = false;
            }

            questionsIdWithStat.add(questionStat.getQuestion().getId());
            answerCount += questionStat.getAnswerCount();
            //validated = questionStat.getValidated() || validated;
            conflicts = questionStat.getConflicts() || conflicts;
            if (!questionStat.getValidated() && (minUsefulLevel == null || questionStat.getQuestion().getMinLevel() < minUsefulLevel)) {
                if (questionStat.getQuestion().getMandatoryForMission() == null || !questionStat.getQuestion().getMandatoryForMission()) {
                    minUsefulLevel = questionStat.getQuestion().getMinLevel();
                }
            }
            if (questionStat.getValidated()) {
                if ("unusable".equals(questionStat.getQuestion().getName())) {
                    unusableValidated = true;
                }
            }
        }


        for (ContributionQuestion question : allMissionQuestions) {

            if (!questionsIdWithStat.contains(question.getId())) {
                if (!"unusable".equals(question.getName())) {
                    Logger.info("Question " + question.getLabel() + " has no stat " + question.getMinLevel() + " < "  + minUsefulLevel + " ? ");
                    if (minUsefulLevel == null || question.getMinLevel() < minUsefulLevel) {
                        minUsefulLevel = question.getMinLevel();
                    }
                    if (validated && !unusableValidated) {
                        ContributionQuestionStat questionStat = ContributionQuestionStat.find("specimenId = ? and question.id = ?", specimenId, question.id).first();
                        if (questionStat == null || !Boolean.TRUE.equals(questionStat.getValidated())) {
                            validated = false;
                        }
                    }
                }
            }

        }

        specimenStat.setMinUsefulLevel(minUsefulLevel);
        specimenStat.setAnswerCount(answerCount);
        specimenStat.setConflicts(conflicts);
        specimenStat.setValidated(validated);
        specimenStat.setUnusableValidated(unusableValidated);
        specimenStat.setLastModifiedAt(new Date());


        Logger.info("Updating specimen stat %d", specimenStat.getSpecimen().getId());
        specimenStat.save();
    }

    public static final ContributionQuestionStat updateQuestionStats(Long specimenId, ContributionQuestion question, List<ContributionAnswer> answers)
    throws IOException {

        //Long specimenId = answers.get(0).getSpecimenId();
        ContributionQuestionStat stat = ContributionQuestionStat.find("specimenId = ? and question.id = ?", specimenId, question.id).first();
        if (stat == null) {
            stat = new ContributionQuestionStat();
            stat.setSpecimenId(specimenId);
            stat.setMissionId(question.getMissionId());
            stat.setQuestion(question);
        }

        // Group answers (no conflicts in same bag);

        List<List<ContributionAnswer>> groups = groupAnswers(question, answers);
        List<ContributionAnswer> biggestGroup = null;

        int biggestGroupSize = 0;
        int groupIndex = 1;

        for (List<ContributionAnswer> group : groups) {
            Logger.info("Group " + (groupIndex++) + " : " + group.size());
            if (group.size() > biggestGroupSize) {
                biggestGroup = group;
                biggestGroupSize = group.size();
            }
        }

        // Nombre de groupes avec la taille
        int biggestGroupSizeCount = 0;
        for (List<ContributionAnswer> group : groups) {
            if (group.size() == biggestGroupSize) {
                biggestGroupSizeCount++;
            }
        }

        Logger.info("Nombre de groupes de plus grande taille : " + biggestGroupSizeCount);


        boolean isValidAnswer = false;

        if (question.getValidationLevel() == null || biggestGroupSize >= question.getValidationLevel()) {
            if (biggestGroupSizeCount == 1) {
                isValidAnswer = true;
            }
        }

        if (isValidAnswer) {
            // Create valid answer

            try {
                Collections.sort(biggestGroup, new Comparator<ContributionAnswer>() {
                    @Override
                    public int compare(ContributionAnswer o1, ContributionAnswer o2) {
                        if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) {
                            return 0;
                        }
                        return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                    }
                });
            } catch (Exception e) {
                Logger.error(e, "impossible de comparer");
            }

            ContributionAnswer validAnswer = buildValidAnswerFrom(stat, question, biggestGroup.get(0));
            stat.setValidAnswer(validAnswer);
        } else {
            // Remove valid answer
            if (stat.getValidAnswer() != null) {
                stat.getValidAnswer().delete();
            }
            stat.setValidAnswer(null);
        }

        stat.setValidated(stat.getValidAnswer() != null);
        stat.setConflicts(stat.getValidAnswer() == null && groups.size() > 1);
        stat.setAnswerCount(Long.valueOf(answers.size()));
        stat.setLastModifiedAt(new Date());
        stat.save();

        if (stat.getValidAnswer() != null) {
            createReferenceRecords(question, stat.getValidAnswer());
        }


        String staticType = StaticAnswer.getTypeByQuestionName(question.getName());
        if (staticType != null) {
            StaticAnswer.deleteBySpecimenId(staticType, specimenId);
        }

        // Création de la valeur statique
        if (stat.getValidAnswer() != null) {
            Logger.info("Valid answer for " + question.getName());
            StaticAnswer answer = StaticAnswer.buildStaticAnswer(question, stat.getValidAnswer());
            if (answer != null) {

                answer.setSpecimenId(stat.getValidAnswer().getSpecimenId());
                answer.setMissionId(question.getMissionId());
                answer.save();
                Specimen specimen = Specimen.findById(specimenId);
                specimen.save();
                Logger.info("STATIC ANSWER : " + answer);
            }
        }



        return stat;

    }

    private static final void createReferenceRecords(ContributionQuestion question, ContributionAnswer answer)
    throws IOException {

        JsonNode json = mapper.readTree(answer.getJsonValue());
        JsonNode configuration = mapper.readTree(question.getConfiguration());

        Logger.info("Create references for valid answer");
        Logger.info(" " + question.getConfiguration());

        for (JsonNode questionField : configuration) {

            Logger.info("- Conf " + questionField.get("name").asText());

            ContributionQuestionFieldType type = ContributionQuestionFieldType.valueOf(questionField.get("type").asText().toUpperCase());

            Boolean multiple = false;
            if (questionField.hasNonNull("options") && questionField.get("options").hasNonNull("multiple")) {
               multiple = questionField.get("options").get("multiple").asBoolean();
            }

            if (type == ContributionQuestionFieldType.REFERENCE) {
                String field = questionField.get("name").asText();
                if (!json.hasNonNull(field)) {
                    Logger.info("- reference null");
                    continue;
                } else {

                    if (!multiple) {

                        ReferenceValue val = mapper.treeToValue(json.get(field), ReferenceValue.class);

                        createReferenceRecordIfNotExists(answer, json, questionField, field, val);

                    } else {
                        Logger.info("Reference creation for multiple values");


                        for (JsonNode jsonVal : json.get(field).get("values")) {

                            ReferenceValue val = mapper.treeToValue(jsonVal, ReferenceValue.class);

                            createReferenceRecordIfNotExists(answer, json, questionField, field, val);


                        }

                    }


                }


            }

        }

    }

    private static void createReferenceRecordIfNotExists(ContributionAnswer answer, JsonNode json, JsonNode questionField, String field, ReferenceValue val) {
        if (val.id == null) {

            Long id = questionField.get("options").get("reference").asLong();

            Reference ref = Reference.findById(id);

            ReferenceRecord record = ReferenceRecord.findByLabelAndRef(val.label.trim(), ref.id);

            if (record != null) {
                Logger.info("- Reference exists, mapping to previous");
            } else {

                record = new ReferenceRecord();
                record.setLabel(val.label.trim());
                record.setReference(ref);
                record.save();

                Logger.info("- Create ref " + record.id + " - " + record.getLabel());
            }

            ((ObjectNode) json.get(field)).put("id", record.id);
            answer.setJsonValue(json.toString());

        }
    }

    private static ContributionAnswer buildValidAnswerFrom(ContributionQuestionStat stat, ContributionQuestion question, ContributionAnswer answer) throws IOException {
        ContributionAnswer valid = stat.getValidAnswer();
        if (valid == null) {
            valid = new ContributionAnswer();
            stat.setValidAnswer(valid);
        }

        valid.setQuestionId(answer.getQuestionId());
        valid.setSpecimenId(answer.getSpecimenId());
        valid.setMissionId(answer.getMissionId());
        valid.setDeleted(false);
        valid.setValidated(true);

        JsonNode answerJson = mapper.readTree(answer.getJsonValue());
        JsonNode answerUtilJson = filterInformative(question, answerJson);
        valid.setJsonValue(answerUtilJson.toString());
        valid.save();

        return valid;
    }

    /*public static final boolean hasConflicts(ContributionQuestion question, List<ContributionAnswer> answers) {

        return true;
    }*/

    /*private static final ContributionAnswer buildValidAnswer(ContributionQuestion question, List<ContributionAnswer> answers) {
        ContributionAnswer validAnswer = null;



        Logger.info("Answers count ok for valid answer (%d >= %d) ?", answers.size(), question.getValidationLevel());

        if (answers.size() >= question.getValidationLevel()) {

            try {
                validAnswer = buildValidAnswerFrom(question, answers.get(0));
            } catch (Exception e) {
                Logger.error(e, "Error building valid answer");
            }
        }

        // Update report


        return validAnswer;
    }
    */

    public static final JsonNode filterInformative(ContributionQuestion question, JsonNode answer) throws IOException {

        JsonNode configuration = mapper.readTree(question.getConfiguration());
        ObjectNode filteredAnswer = mapper.createObjectNode();
        for (JsonNode questionField : configuration) {
            boolean noConflictField = false;
            if (questionField.hasNonNull("noConflict")) {
                noConflictField = questionField.get("noConflict").asBoolean(false);
            }
            if (!noConflictField) {
                String field = questionField.get("name").asText();
                if (answer.has(field)) {
                    filteredAnswer.set(field, answer.get(field));
                }
            }

        }
        return filteredAnswer;
    }

    public static final boolean hasConflicts(ContributionQuestion question, ContributionAnswer answerA, ContributionAnswer answerB) throws IOException {

        JsonNode jsonA = mapper.readTree(answerA.getJsonValue());
        JsonNode jsonB = mapper.readTree(answerB.getJsonValue());

        JsonNode configuration = mapper.readTree(question.getConfiguration());

        Logger.info("Conflicts between " + answerA.getUserId() + " " + answerB.getUserId() + " ?");

        boolean conflicts = false;

        for (JsonNode questionField : configuration) {

            boolean noConflictField = false;
            if (questionField.hasNonNull("noConflict")) {
                noConflictField = questionField.get("noConflict").asBoolean(false);
            }

            String field = questionField.get("name").asText();

            if (noConflictField) {
                Logger.info("- " + field + " ignored");
                continue;
            }

            ContributionQuestionFieldType type = ContributionQuestionFieldType.valueOf(questionField.get("type").asText().toUpperCase());
            boolean multiple = false;
            if (questionField.hasNonNull("options") && questionField.get("options").hasNonNull("multiple")) {
                multiple = questionField.get("options").get("multiple").asBoolean();
            }
            boolean sortable = false;
            if (questionField.hasNonNull("options") && questionField.get("options").hasNonNull("sortable")) {
                sortable = questionField.get("options").get("sortable").asBoolean();
            }
            boolean approx = false;
            if (questionField.hasNonNull("options") && questionField.get("options").hasNonNull("approx")) {
                approx = questionField.get("options").get("approx").asBoolean();
            }

            boolean equals = areEqualsAnswerField(field, type, multiple, sortable, approx, jsonA, jsonB);

            if (!equals) {
                conflicts = true;
            }

            Logger.info("- " + field + " (" + type + ") : " + (equals ? "equals" : "conflict") + " (approx : " + approx + ")");

        }

        Logger.info("---> conflicts ? " + conflicts);

        return conflicts;

    }

    // TODO Ajouter configuration pour specific ?
    private static final boolean areEqualsAnswerField(String field, ContributionQuestionFieldType type, boolean multiple, boolean sortable, boolean approx, JsonNode answerA, JsonNode answerB)
    throws JsonProcessingException {

        Logger.info("---");
        Logger.info("Compare field '" + field + "' of type '" + type + "' (multiple = " + multiple + ")");

        switch (type) {

            case REFERENCE: return areEqualsAnswerFieldReference(field, multiple, sortable, answerA, answerB);
            case CHECKBOX:  return areEqualsAnswerFieldCheckbox(field, answerA, answerB);
            case TEXT:  return areEqualsAnswerFieldText(field, multiple, sortable, approx, answerA, answerB);
            case DATE:  return areEqualsAnswerFieldDate(field, answerA, answerB);
            case PERIOD:  return areEqualsAnswerFieldPeriod(field, answerA, answerB);
            case LIST:  return areEqualsAnswerFieldList(field, answerA, answerB);
            case GEO:  return areEqualsAnswerFieldGeo(field, answerA, answerB);

        }

        return false;
    }

    // LISTE
    private static final boolean areEqualsAnswerFieldList(String field, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {
        if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
            Logger.info("- no values on both -> equals");
            return true;
        }
        if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {
            Logger.info("- one null -> conflict");
            return false;
        }

        String ansA = answerA.get(field).asText();
        String ansB = answerB.get(field).asText();

        return ansA.equals(ansB);
    }

    private static final boolean areEqualsAnswerFieldGeo(String field, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {
        if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
            Logger.info("- no values on both -> equals");
            return true;
        }
        if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {
            Logger.info("- one null -> conflict");
            return false;
        }

        Double latA = answerA.get(field).get("lat").asDouble();
        Double lngA = answerA.get(field).get("lng").asDouble();
        Double latB = answerB.get(field).get("lat").asDouble();
        Double lngB = answerB.get(field).get("lng").asDouble();

        return GPS.distance(latA, lngA, latB, lngB) < Herbonautes.get().geolocalisationConflictDistance;
    }

    // TEXT
    private static final boolean areEqualsAnswerFieldText(String field, boolean multiple, boolean sortable, boolean approx, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {

        if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
            Logger.info("- no values on both -> equals");
            return true;
        }

        if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {
            Logger.info("- one null -> conflict");
            return false;
        }

        if (!multiple) {

            String valA = answerA.get(field).asText();
            String valB = answerB.get(field).asText();

            Logger.info(valA + " = " + valB + " ? (approx=" + approx + ")");

            if (!approx) {
                return valA.trim().equals(valB.trim());
            } else {
                return TextUtils.approxEquals(valA, valB, ReferenceUtils.getSynonyms());
            }

        } else {

            boolean unknownOrderA = answerA.get(field).hasNonNull("unknownOrder") && answerA.get(field).get("unknownOrder").asBoolean();
            boolean unknownOrderB = answerB.get(field).hasNonNull("unknownOrder") && answerB.get(field).get("unknownOrder").asBoolean();

            Logger.info("Sortable %s, o1 %s, o2 %s", sortable, unknownOrderA, unknownOrderB);

            if (sortable) {
                if (unknownOrderA != unknownOrderB) {
                    return false;
                }
            }

            List<String> labelsA = new ArrayList<String>();
            for (JsonNode node : answerA.get(field).get("values")) {
                if (approx) {
                    labelsA.add(TextUtils.simplify(node.asText(), ReferenceUtils.getSynonyms()));
                } else {
                    labelsA.add(node.asText());
                }
            }
            List<String> labelsB = new ArrayList<String>();
            for (JsonNode node : answerB.get(field).get("values")) {
                if (approx) {
                    labelsB.add(TextUtils.simplify(node.asText(), ReferenceUtils.getSynonyms()));
                } else {
                    labelsB.add(node.asText());
                }
            }

            if (sortable && !unknownOrderA) {
                return ListUtils.isEqualList(labelsA, labelsB);
            } else {
                return CollectionUtils.isEqualCollection(labelsA, labelsB);
            }

        }
    }

    // DATE
    private static final boolean areEqualsAnswerFieldDate(String field, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {
        return false;
    }

    // PERIOD
    private static final boolean areEqualsAnswerFieldPeriod(String field, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {
        Logger.info("Equals period ?");

        if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
            Logger.info("- no values on both -> equals");
            return true;
        }

        if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {
            Logger.info("- one null -> conflict");
            return false;
        }

        Long startA = answerA.get(field).get("start").get("ts").asLong();
        Long endA = answerA.get(field).get("end").get("ts").asLong();
        Long startB = answerB.get(field).get("start").get("ts").asLong();
        Long endB = answerB.get(field).get("end").get("ts").asLong();

        Logger.info("start : %s = %s ? %s ", startA, startB, (startB - startA));
        Logger.info("end   : %s = %s ? %s ", startA, startB, (endB - endA));


        startA = DateUtils.correctUTCShift(startA);
        endA = DateUtils.correctUTCShift(endA);
        startB = DateUtils.correctUTCShift(startB);
        endB = DateUtils.correctUTCShift(endB);

        return ContributionAnswer.equalsDate(startA, startB) && ContributionAnswer.equalsDate(endA, endB);
        //return startA.equals(startB) && endA.equals(endB);
    }

    private static final boolean areEqualsAnswerFieldReference(String field, boolean multiple, boolean sortable, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {

        if (!multiple) {

            Logger.info("Compare reference '%s'", field);

            if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
                Logger.info("- no values on both -> equals");
                return true;
            }

            if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {
                Logger.info("- one null -> conflict");
                return false;
            }

            ReferenceValue valA = mapper.treeToValue(answerA.get(field), ReferenceValue.class);
            ReferenceValue valB = mapper.treeToValue(answerB.get(field), ReferenceValue.class);

            Logger.info(valA + " = " + valB + " ?");

            if (valA.id != null && valB.id != null) {
                return valA.id.equals(valB.id);
            } else {
                return valA.label.equals(valB.label);
            }

        } else {

            Logger.info("Compare multiple references '%s'", field);

            if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
                Logger.info("- no values on both -> equals");
                return true;
            }

            if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {
                Logger.info("- one null -> conflict");
                return false;
            }

            boolean unknownOrderA = answerA.get(field).hasNonNull("unknownOrder") && answerA.get(field).get("unknownOrder").asBoolean();
            boolean unknownOrderB = answerB.get(field).hasNonNull("unknownOrder") && answerB.get(field).get("unknownOrder").asBoolean();

            Logger.info("Sortable %s, o1 %s, o2 %s", sortable, unknownOrderA, unknownOrderB);

            if (sortable) {
                if (unknownOrderA != unknownOrderB) {
                    return false;
                }
            }

            List<String> labelsA = new ArrayList<String>();
            for (JsonNode node : answerA.get(field).get("values")) {
                labelsA.add(node.get("label").asText());
            }
            List<String> labelsB = new ArrayList<String>();
            for (JsonNode node : answerB.get(field).get("values")) {
                labelsB.add(node.get("label").asText());
            }

            if (sortable && !unknownOrderA) {
                return ListUtils.isEqualList(labelsA, labelsB);
            } else {
                return CollectionUtils.isEqualCollection(labelsA, labelsB);
            }
        }

        //return false;

    }

    // REFERENCE

    private static final boolean areEqualsAnswerFieldCheckbox(String field, JsonNode answerA, JsonNode answerB) throws JsonProcessingException {
        Logger.debug("Compare checkbox '%s'", field);

        if (!answerA.hasNonNull(field) && !answerB.hasNonNull(field)) {
            Logger.info("- no values on both -> equals");
            return true;
        }

        if (answerA.hasNonNull(field) && answerB.hasNonNull(field)) {
            Logger.info("- 2 values -> check");
            return answerA.get(field).asBoolean() == answerB.get(field).asBoolean();
        }

        if (!answerA.hasNonNull(field) || !answerB.hasNonNull(field)) {



            if (answerA.hasNonNull(field) && answerA.get(field).asBoolean()) {
                Logger.info("- one null / one true -> conflict");
                return false;
            }

            if (answerB.hasNonNull(field) && answerB.get(field).asBoolean()) {
                Logger.info("- one null / one true -> conflict");
                return false;
            }

            Logger.info("- one null / one true -> equals");
            return true;
        }



        return false;

    }

    /**
     * Groupe les contributions
     */
    private static List<List<ContributionAnswer>> groupAnswers(ContributionQuestion question, List<ContributionAnswer> answers)
    throws IOException {
        List<List<ContributionAnswer>> groups = new ArrayList<List<ContributionAnswer>>();

        if (answers == null) {
            return groups;
        }

        Logger.info("Group answers");


        LinkedList<ContributionAnswer> ungrouped = new LinkedList<ContributionAnswer>(answers);

        while(ungrouped.size() > 0) {
            Logger.info("");
            Logger.info("- build group " + (groups.size()));

            List<ContributionAnswer> newGroup = new ArrayList<ContributionAnswer>();

            // On prend la premiere pour rechercher sa classe d'équivalence
            ContributionAnswer c = ungrouped.pop();
            newGroup.add(c);

            // On ajoute dans le groupe toutes celles qui corresepondent
            for (ContributionAnswer ctrb : ungrouped) {
                if (!hasConflicts(question, c, ctrb)) {
                    newGroup.add(ctrb);
                }
            }
            // On enleve les contribution fraichement groupées
            for (ContributionAnswer moved : newGroup) {
                ungrouped.remove(moved);
            }

            Logger.info("- group size : " + newGroup.size() + ", ungrouped : " + ungrouped.size());


            // On ajoute la classe à la liste
            groups.add(newGroup);
        }

        return groups;
    }

    // CHECKBOX

    public static class ConflictReport {
        public ContributionAnswer answer;
        public ContributionQuestionStat stat;
        public List<ContributionAnswer> conflictAnswers;
        public List<ContributionAnswer> noConflictAnswers;
        public Map<String, String> attributes = new HashMap<String, String>();
        public boolean sendAlert = false;
    }

    public static class ContributionChanges {
        public boolean changedAnswer = false;
        public boolean previousConflict = false;
        public boolean nextValidated = false;
    }

    public static class ReferenceValue {
        public String id;
        public String label;
        public String toString() {
            return "[id: "+ id + ", label:" + label + "]";
        }
    }


}
