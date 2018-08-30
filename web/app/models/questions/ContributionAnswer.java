package models.questions;

import com.fasterxml.jackson.databind.JsonNode;
import conf.Herbonautes;
import helpers.DateUtils;
import libs.Json;
import models.User;
import models.contributions.Contribution;
import org.joda.time.LocalDateTime;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name="H_CONTRIBUTION_ANSWER")
public class ContributionAnswer extends Model {

    public static SimpleDateFormat exportSDF = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat dateSDF = new SimpleDateFormat("dd MMMM yyyy");
    public static SimpleDateFormat monthSDF = new SimpleDateFormat("MMMM yyyy");
    public static SimpleDateFormat yearDF = new SimpleDateFormat("yyyy");

    @Column(name = "QUESTION_ID")
    private Long questionId;
    @Column(name = "USER_ID")
    private Long userId;
    @Column(name = "SPECIMEN_ID")
    private Long specimenId;
    @Column(name = "MISSION_ID")
    private Long missionId;
    @Column(name = "FROM_ANSWER_ID")
    private Long fromAnswerId;
    @Lob
    @Column(name = "JSON_VALUE", columnDefinition="CLOB")
    private String jsonValue;
    @Column(name = "DELETED")
    private Boolean deleted = false;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT")
    private Date createdAt;
    @Column(name = "VALIDATED")
    private Boolean validated;

    public static Long countForUser(Long userId) {
        return ContributionAnswer.count("deleted != true and userId = ?", userId);
    }

    public static ContributionAnswer findUserAnswer(Long userId, Long missionId, Long specimenId, Long questionId) {
        return ContributionAnswer.find("specimenId = ? and questionId = ? and userId = ?",
                                       specimenId,
                                       questionId,
                                       userId).first();
    }

    public static List<ContributionAnswer> findUserAnswers(Long userId, Long missionId, Long specimenId) {
        return ContributionAnswer.find("specimenId = ? and userId = ? and deleted != true",
                specimenId,
                userId).fetch();
    }

    public static Long countUserAnswers(Long userId, Long missionId, Long specimenId) {
        return ContributionAnswer.count("specimenId = ? and userId = ? and deleted != true",
                specimenId,
                userId);
    }

    public static List<ContributionAnswer> findAllAnswersForSpecimen(Long missionId, Long specimenId, Long questionId) {
        return ContributionAnswer.find("specimenId = ? and questionId = ? and deleted != true and userId is not null",
                specimenId,
                questionId).fetch();
    }

    public static List<ContributionAnswer> findAllValidAnswersForSpecimen(Long specimenId) {
        /*
        return ContributionAnswer.find("specimenId = ? and deleted != true and userId is null",
                specimenId).fetch();
        */

        return
            JPA.em().createNativeQuery("SELECT * FROM H_CONTRIBUTION_ANSWER WHERE ID IN ( " +
                    "SELECT S.VALID_ANSWER_ID FROM H_CONTRIBUTION_QUESTION_STAT S " +
                    "WHERE S.SPECIMEN_ID = :specimenId AND S.VALIDATED = 1 AND " +
                    "S.VALID_ANSWER_ID IS NOT NULL)", ContributionAnswer.class)
                    .setParameter("specimenId", specimenId)
                    .getResultList();

    }


    public static List<ContributionAnswer> findAllNoConflictsAnswersForSpecimen(Long specimenId) {
        return
                JPA.em().createNativeQuery("select * from H_CONTRIBUTION_ANSWER WHERE QUESTION_ID IN ( " +
                        "select DISTINCT QUESTION_ID from H_CONTRIBUTION_QUESTION_STAT S  " +
                        "where S.SPECIMEN_ID = :specimenId AND VALIDATED = 0 and CONFLICTS = 0 " +
                        ") AND SPECIMEN_ID = :specimenId AND DELETED = 0", ContributionAnswer.class)
                .setParameter("specimenId", specimenId)
                .getResultList();
    }

    public static List<ContributionAnswer> findAllUserAnswersForSpecimen(Long specimenId) {
        return ContributionAnswer.find("specimenId = ? and deleted != true and userId is not null",
                specimenId).fetch();
    }

    public static Map<String, ContributionAnswer> findAllValidAnswersForSpecimenByName(Long specimenId) {
        List<ContributionAnswer> answers = findAllValidAnswersForSpecimen(specimenId);
        Map<String, ContributionAnswer> byName = new HashMap();
        for (ContributionAnswer a : answers) {
            String questionName = ContributionQuestion.getQuestionName(a.getQuestionId());
            byName.put(questionName, a);
        }
        return byName;
    }

    public static Map<String, ContributionAnswer> findAllTransferableAnswersForSpecimenByName(Long specimenId) {

        List<ContributionAnswer> answers = findAllValidAnswersForSpecimen(specimenId);
        Map<String, ContributionAnswer> byName = new HashMap();
        for (ContributionAnswer a : answers) {
            String questionName = ContributionQuestion.getQuestionName(a.getQuestionId());
            byName.put(questionName, a);
        }

        List<ContributionAnswer> pendingAnswers = findAllNoConflictsAnswersForSpecimen(specimenId);
        for (ContributionAnswer a : pendingAnswers) {
            String questionName = ContributionQuestion.getQuestionName(a.getQuestionId());
            byName.put(questionName, a);
        }

        return byName;
    }

    public static Map<String, Map<String, ContributionAnswer>> findAllUserAnswersForSpecimenByLoginAndName(Long specimenId) {
        List<ContributionAnswer> answers = findAllUserAnswersForSpecimen(specimenId);

        Map<String, Map<String, ContributionAnswer>> byLogin = new HashMap();



        for (ContributionAnswer a : answers) {
            User u = User.findById(a.userId);
            String questionName = ContributionQuestion.getQuestionName(a.getQuestionId());

            Map<String, ContributionAnswer> byName = byLogin.get(u.getLogin());

            if (byName == null) {
                byName = new HashMap();
                byLogin.put(u.getLogin(), byName);
            }

            byName.put(questionName, a);
        }

        return byLogin;
    }

    public static ContributionAnswer findValidAnswerForSpecimen(Long missionId, Long specimenId, Long questionId) {
        return ContributionAnswer.find("specimenId = ? and questionId = ? and userId is null and validated = true",
                specimenId,
                questionId).first();
    }

    public static QuestionLineHumanValue toQuestionLineHumanValue(JsonNode rawValue, String fieldName, Long questionId) {

        String type = ContributionQuestion.getQuestionLineType(questionId, fieldName);
        String label = ContributionQuestion.getQuestionLineLabel(questionId, fieldName);
        Boolean multiple = ContributionQuestion.getQuestionLineMultiple(questionId, fieldName);
        Boolean info = ContributionQuestion.getQuestionLineInfo(questionId, fieldName);

        QuestionLineHumanValue value = new QuestionLineHumanValue();

        value.isBoolean = "checkbox".equals(type);
        value.isInfo = info;
        value.textValue = toQuestionLineTextValue(type, multiple, rawValue);
        value.exportValue = toQuestionLineExportValue(type, multiple, rawValue);
        value.booleanValue = value.isBoolean && "true".equals(value.textValue);
        value.label = label;
        value.name = fieldName;

        return value;
    }

    public static String toQuestionLineTextValue(String type, boolean multiple, JsonNode rawValue) {

        if ("reference".equals(type)) {

            if (!multiple) {
            	 if (rawValue == null || rawValue.isNull() || !rawValue.hasNonNull("label")) {
                     return "";
                 }
                return rawValue.get("label").asText();
            } else {
                StringBuilder sb = new StringBuilder();
                for (JsonNode ref : rawValue.get("values")) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(ref.get("label").asText());
                }
                return sb.toString();
            }
        } else if ("text".equals(type)) {

            if (!multiple) {
                return rawValue.isNull() ? "" : rawValue.asText();
            } else {
                StringBuilder sb = new StringBuilder();
                for (JsonNode ref : rawValue.get("values")) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(ref.asText());
                }
                return sb.toString();
            }

        } else if ("checkbox".equals(type)) {

            return rawValue.toString();

        } else if ("period".equals(type)) {

            long startTs = rawValue.get("start").get("ts").asLong();
            long endTs = rawValue.get("end").get("ts").asLong();

            return smartPeriodFormat(new Date(startTs), new Date(endTs));

        } else if ("geo".equals(type)) {

            DecimalFormat df = new DecimalFormat("#.######");
            String lat = df.format(rawValue.get("lat").asDouble());
            String lng = df.format(rawValue.get("lng").asDouble());

            return lat + " , " + lng;
        } else if ("list".equals(type)) {
            return rawValue.asText();
        }


        return "[" + type + " : " + rawValue.toString() + "]";
    }

    public static String toQuestionLineExportValue(String type, boolean multiple, JsonNode rawValue) {

        if ("reference".equals(type)) {

            if (!multiple) {
                if (!rawValue.hasNonNull("label")) {
                    return "";
                }
                return rawValue.get("label").asText();
            } else {
                StringBuilder sb = new StringBuilder();
                for (JsonNode ref : rawValue.get("values")) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(ref.get("label").asText());
                }
                return sb.toString();
            }
        } else if ("text".equals(type)) {

            if (!multiple) {
                return rawValue.asText();
            } else {
                StringBuilder sb = new StringBuilder();
                for (JsonNode ref : rawValue.get("values")) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(ref.asText());
                }
                return sb.toString();
            }

        } else if ("checkbox".equals(type)) {

            return rawValue.toString();

        } else if ("period".equals(type)) {

            long startTs = rawValue.get("start").get("ts").asLong();
            long endTs = rawValue.get("end").get("ts").asLong();

            return exportPeriodFormat(new Date(startTs), new Date(endTs));

        } else if ("geo".equals(type)) {

            double lat = rawValue.get("lat").asDouble();
            double lng = rawValue.get("lng").asDouble();

            return lat + " , " + lng;

        } else if ("list".equals(type)) {
            return rawValue.toString();
        }


        return "[" + type + " : " + rawValue.toString() + "]";
    }

    private static Date offsetDate(Date date) {
        long offset = Herbonautes.get().timezoneOffset.longValue() * 60 * 1000;
        date.setTime(date.getTime() - offset);

        return date;
    }



    public static String exportPeriodFormat(Date startDate, Date endDate) {
        //return start.toString("dd/MM/yyyy") + " -> " + end.toString("dd/MM/yyyy");

        startDate.setTime(DateUtils.correctUTCShift(startDate.getTime()));
        endDate.setTime(DateUtils.correctUTCShift(endDate.getTime()));

        return exportSDF.format(startDate) + " - " + exportSDF.format(endDate);
    }

    public static String smartPeriodFormat(Date startDate, Date endDate) {

        //long offset = Herbonautes.get().timezoneOffset.longValue() * 60 * 1000;
//
        ////System.out.println("PERIOD RAW   : " + startDate + " - " + endDate);
//
        //startDate.setTime(startDate.getTime() - offset);
        //endDate.setTime(endDate.getTime() - offset);
//
        //System.out.println("PERIOD RAW 2 : " + startDate + " - " + endDate);


        startDate.setTime(DateUtils.correctUTCShift(startDate.getTime()));
        endDate.setTime(DateUtils.correctUTCShift(endDate.getTime()));

        //DateTime start = new DateTime(startDate).withTimeAtStartOfDay();
        //DateTime end = new DateTime(endDate).withTimeAtStartOfDay();
//
//
        //System.out.println("PERIOD RAW 3 : " + start + " - " + end);


        LocalDateTime startLocal = LocalDateTime.fromDateFields(startDate).withTime(0, 0, 0, 0);
        LocalDateTime endLocal = LocalDateTime.fromDateFields(endDate).withTime(0, 0, 0, 0);
        //DateTime.parse(dateSDF.format(endDate), DateTimeFormatter.ofPattern());

        //System.out.println("PERIOD RAW 4 : " + startLocal + " - " + endLocal);
//
        ////LocalDate
//
        //System.out.println(start + " - end of month -> " + start.plusMonths(1).minusDays(1) + " <-- " + end);
        //System.out.println(start + " - end of year -> " + start.plusYears(1).minusDays(1) + " <-- " + end);
//
        //DateTime oneMonth = start.withDayOfMonth(startLocal.dayOfMonth().getMaximumValue());
//
        //System.out.println(start + " - last day - " + oneMonth + " <-- " + end);
//
        //System.out.println("Local month : " + startLocal + " --- " + endLocal + " --- " + (startLocal.plusMonths(1).minusDays(1)));

        if (startLocal.equals(endLocal)) {
            return dateSDF.format(startDate);
        } else if (startLocal.getDayOfMonth() == 1 && startLocal.plusMonths(1).minusDays(1).equals(endLocal)) {
            return monthSDF.format(startDate);
        } else if (startLocal.getDayOfMonth() == 1 && startLocal.getMonthOfYear() == 1 && startLocal.plusYears(1).minusDays(1).equals(endLocal)) {
            return yearDF.format(startDate);
        }

        return dateSDF.format(startDate) + " - " + dateSDF.format(endDate);

        //return start.toString() + " - " + end.toString();
        //return start.toString("dd MMMM yyyy") + " - " + end.toString("dd MMMM yyyy");
    }

    public static Boolean equalsDate(long startTS, long endTS) {

        long offset = Herbonautes.get().timezoneOffset.longValue() * 60 * 1000;

        Date startDate = new Date(startTS);
        Date endDate = new Date(endTS);

        startDate.setTime(startDate.getTime() - offset);
        endDate.setTime(endDate.getTime() - offset);

        LocalDateTime startLocal = LocalDateTime.fromDateFields(startDate).withTime(0, 0, 0, 0);
        LocalDateTime endLocal = LocalDateTime.fromDateFields(endDate).withTime(0, 0, 0, 0);

        return startLocal.equals(endLocal);
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }

    public Long getFromAnswerId() {
        return fromAnswerId;
    }

    public void setFromAnswerId(Long fromAnswerId) {
        this.fromAnswerId = fromAnswerId;
    }

    public String getJsonValue() {
        return jsonValue;
    }

    public void setJsonValue(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String getUserLogin() {
        if (this.userId != null) {
            User user = User.findById(this.userId);
            return user.getLogin();
        }
        return null;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public List<QuestionLineHumanValue> toHumanValue() {
        JsonNode node = Json.parse(this.jsonValue);
        List<QuestionLineHumanValue> values = new ArrayList();

        if (node != null) {
            Iterator<String> it = node.fieldNames();
            while (it.hasNext()) {

                String fieldName = it.next();

                JsonNode questionLineValue = node.get(fieldName);

                //String type = ContributionQuestion.getQuestionLineType(this.questionId, fieldName);

                //sb.append(String.format("%s (%s)", fieldName, type));

                values.add(toQuestionLineHumanValue(questionLineValue, fieldName, this.questionId));

            }
            return values;

        }
        return null;
    }

    public Map<String, String> toExportableValue() {

        Map<String, String> export = new HashMap();

        JsonNode node = Json.parse(this.jsonValue);

        if (node != null) {
            Iterator<String> it = node.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                JsonNode questionLineValue = node.get(fieldName);
                //String type = ContributionQuestion.getQuestionLineType(this.questionId, fieldName);
                //sb.append(String.format("%s (%s)", fieldName, type));
                QuestionLineHumanValue hvl = toQuestionLineHumanValue(questionLineValue, fieldName, this.questionId);
                export.put(fieldName, hvl.exportValue);
            }
        }
        return export;
    }

    public static class QuestionLineHumanValue {
        public boolean isBoolean;
        public boolean isInfo;
        public String label;
        public String textValue;
        public boolean booleanValue;
        public String exportValue;
        public String name;

        @Override
        public String toString() {
            return isBoolean + "/" + isInfo + "/" + label + "/" + textValue + "/" + booleanValue;
        }
    }


}
