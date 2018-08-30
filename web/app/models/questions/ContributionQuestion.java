package models.questions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import libs.Json;
import models.Mission;
import models.quiz.Quiz;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import play.Logger;
import play.cache.*;
import play.cache.Cache;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="H_CONTRIBUTION_QUESTION")
public class ContributionQuestion extends Model {

    @Column(name = "NAME")
    private String name;

    @Column(name = "LABEL")
    private String label;

    @Column(name = "SHORT_LABEL")
    private String shortLabel;

    @Column(name = "MISSION_ID")
    private Long missionId;

    @Column(name = "DEFAULT_PARENT_REF")
    private Long defaultParentRef;

    @Column(name = "SORT_INDEX")
    private Long sortIndex;

    @Column(name = "MIN_LEVEL")
    private Long minLevel;

    //@Column(name = "NEEDED_QUIZ_ID")
    //private Long neededQuizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "NEEDED_QUIZ_ID")
    private Quiz neededQuiz;

    @Column(name = "VALIDATION_LEVEL")
    private Long validationLevel;

    @Column(name = "TEMPLATE_ID")
    private Long templateId;

    @Lob
    @Column(name = "CONFIGURATION", columnDefinition="CLOB")
    private String configuration;

    @Lob
    @Column(name = "HELP_HTML", columnDefinition="CLOB")
    private String helpHTML;

    @Column(name = "EDITABLE")
    private Boolean editable;

    @Column(name = "DEFAULT_MISSION")
    private Boolean defaultForMission;

    @Column(name = "mission_mandatory")
    private Boolean mandatoryForMission;

    @Column(name = "deleted")
    private Boolean deleted;

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public String getHelpHTML() {
        return helpHTML;
    }

    public void setHelpHTML(String helpHTML) {
        this.helpHTML = helpHTML;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Long minLevel) {
        this.minLevel = minLevel;
    }

    /*public Long getNeededQuizId() {
        return neededQuizId;
    }

    public void setNeededQuizId(Long neededQuizId) {
        this.neededQuizId = neededQuizId;
    }*/

    public Long getValidationLevel() {
        return validationLevel;
    }

    public void setValidationLevel(Long validationLevel) {
        this.validationLevel = validationLevel;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public Long getDefaultParentRef() {
        return defaultParentRef;
    }

    public void setDefaultParentRef(Long defaultParentRef) {
        this.defaultParentRef = defaultParentRef;
    }

    public Long getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Long sortIndex) {
        this.sortIndex = sortIndex;
    }

    public Boolean getMandatoryForMission() {
        return mandatoryForMission;
    }

    public void setMandatoryForMission(Boolean mandatoryForMission) {
        this.mandatoryForMission = mandatoryForMission;
    }

    //

    public static List<ContributionQuestion> findAllTemplates() {
        return ContributionQuestion.find("missionId is null order by sortIndex asc").fetch();
    }

    public static Long countQuestionsForMission(Long missionId) {
        return ContributionQuestion.count("missionId = ? and deleted = false and name != 'unusable'", missionId);
    }

    public static List<ContributionQuestion> findAllActiveForMission(Long id) {
        return ContributionQuestion.find("missionId = ? and deleted = false order by sortIndex asc", id).fetch();
    }
    public static List<ContributionQuestion> findAllForMission(Long id) {
        return ContributionQuestion.find("missionId = ? order by sortIndex asc", id).fetch();
    }
    public static List<ContributionQuestion> findAllDefaultTemplates() {
        return ContributionQuestion.find("missionId is null and defaultForMission = true order by sortIndex asc").fetch();
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Quiz getNeededQuiz() {
        return neededQuiz;
    }

    public void setNeededQuiz(Quiz neededQuiz) {
        this.neededQuiz = neededQuiz;
    }

    public Boolean getDefaultForMission() {
        return defaultForMission;
    }

    public void setDefaultForMission(Boolean defaultForMission) {
        this.defaultForMission = defaultForMission;
    }

    public static ContributionQuestion createFromTemplate(ContributionQuestion template, Long missionId) {
        ContributionQuestion newQuestion = new ContributionQuestion();

        newQuestion.setName(template.getName());
        newQuestion.setLabel(template.getLabel());
        newQuestion.setShortLabel(template.getShortLabel());
        newQuestion.setMinLevel(template.getMinLevel());
        newQuestion.setValidationLevel(template.getValidationLevel());
        newQuestion.setNeededQuiz(template.getNeededQuiz());
        newQuestion.setHelpHTML(template.getHelpHTML());
        newQuestion.setTemplateId(template.id);
        newQuestion.setMissionId(missionId);
        newQuestion.setSortIndex(template.getSortIndex());
        newQuestion.setDeleted(false);
        newQuestion.setConfiguration(template.getConfiguration());

        return newQuestion;
    }

    public static String getQuestionLabel(Long id) {
        String label = (String) play.cache.Cache.get("question_label_" + id);
        if (label == null) {
            ContributionQuestion q = ContributionQuestion.findById(id);
            label = q.label;
            Cache.add("question_label_" + id, label, "5min");
        }
        return label;
    }

    public static String getQuestionName(Long id) {
        String label = (String) play.cache.Cache.get("question_name_" + id);
        if (label == null) {
            ContributionQuestion q = ContributionQuestion.findById(id);
            label = q.name;
            Cache.add("question_name_" + id, label, "5min");
        }
        return label;
    }

    public static Long getQuestionSortIndex(Long id) {
        Long order = (Long) play.cache.Cache.get("question_order_" + id);
        if (order == null) {
            ContributionQuestion q = ContributionQuestion.findById(id);
            order = q.getSortIndex();
            Cache.add("question_order_" + id, order, "5min");
        }
        return order;
    }

    public static ContributionQuestion getQuestion(Long id) {
        ContributionQuestion question = (ContributionQuestion) play.cache.Cache.get("question_" + id);
        if (question == null) {
            question = ContributionQuestion.findById(id);
            Cache.add("question_" + id, question, "5min");
        }
        return question;
    }

    public static Map<String, JsonNode> getQuestionConfiguration(Long id) {
        Map<String, JsonNode> conf = ( Map<String, JsonNode>) play.cache.Cache.get("question_conf_" + id);
        if (conf == null) {
            ContributionQuestion question = ContributionQuestion.findById(id);
            JsonNode confraw = Json.parse(question.getConfiguration());
            conf = new HashMap<String, JsonNode>();
            for (JsonNode confline : confraw) {
                String name = confline.get("name").asText();
                conf.put(name, confline);
            }


            Cache.add("question_conf_" + id, conf, "5min");
        }
        return conf;
    }

    public static String getQuestionLineType(Long questionId, String questionLineName) {
        //Logger.info("question type for %d, %s", questionId, questionLineName);
        String questionLineType = (String) play.cache.Cache.get("questionline_type_" + questionId + "_" + questionLineName);
        if (questionLineType == null) {
            Map<String, JsonNode> conf = getQuestionConfiguration(questionId);
            if (conf.get(questionLineName) == null) {
                return "unknown";
            }
            questionLineType = conf.get(questionLineName).get("type").asText();
            play.cache.Cache.add( "questionline_type_" + questionId + "_" + questionLineName, questionLineType, "5min");

        }
        return questionLineType;
    }

    public static String getQuestionLineLabel(Long questionId, String questionLineName) {
        String questionLineLabel = (String) play.cache.Cache.get("questionline_label_" + questionId + "_" + questionLineName);
        if (questionLineLabel == null) {
            Map<String, JsonNode> conf = getQuestionConfiguration(questionId);
            if (conf.get(questionLineName) == null) {
                return null;
            }
            questionLineLabel = conf.get(questionLineName).get("label").asText();
            play.cache.Cache.add( "questionline_label_" + questionId + "_" + questionLineName, questionLineLabel, "5min");

        }
        return questionLineLabel;
    }

    public static Boolean getQuestionLineMultiple(Long questionId, String questionLineName) {
        Boolean questionLineMultiple = (Boolean) play.cache.Cache.get("questionline_multiple_" + questionId + "_" + questionLineName);
        if (questionLineMultiple == null) {
            Map<String, JsonNode> conf = getQuestionConfiguration(questionId);
            if (conf.get(questionLineName) == null) {
                return false;
            }
            JsonNode options = conf.get(questionLineName).get("options");
            questionLineMultiple = false;
            if (options != null && options.get("multiple") != null) {
                questionLineMultiple = options.get("multiple").asBoolean();
            }
            play.cache.Cache.add( "questionline_multiple_" + questionId + "_" + questionLineName, questionLineMultiple, "5min");

        }
        return questionLineMultiple;
    }

    public static Boolean getQuestionLineInfo(Long questionId, String questionLineName) {
        Boolean questionLineMultiple = (Boolean) play.cache.Cache.get("questionline_info_" + questionId + "_" + questionLineName);
        if (questionLineMultiple == null) {
            Map<String, JsonNode> conf = getQuestionConfiguration(questionId);
            if (conf.get(questionLineName) == null) {
                return false;
            }
            JsonNode options = conf.get(questionLineName).get("options");
            questionLineMultiple = false;
            if (options != null && options.get("noConflict") != null) {
                questionLineMultiple = options.get("noConflict").asBoolean();
            }
            play.cache.Cache.add( "questionline_info_" + questionId + "_" + questionLineName, questionLineMultiple, "5min");

        }
        return questionLineMultiple;
    }


    public static Map<String, List<String>> getAllQuestionLines(Long missionId) {
        HashMap<String, List<String>> questionLines = new LinkedHashMap();
        List<ContributionQuestion> questions = ContributionQuestion.find("missionId = ? order by sortIndex", missionId).fetch();
        for (ContributionQuestion q : questions) {
            List<String> names = new ArrayList();
            for (JsonNode questionLine : Json.parse(q.getConfiguration())) {
                names.add(questionLine.get("name").asText());
            };
            questionLines.put(q.name, names);
        }
        return questionLines;
    }

    public Long getUsedCount() {
        if (this.templateId == null) {
            return ContributionQuestion.count("templateId = ?", this.id);
        } else {
            return 1l;
        }
    }

    public static boolean isUsedInMission(Long questionId) {
        return ContributionQuestion.count("templateId = ?", questionId) > 0;
    }


    public static List<ContributionQuestion> findQuestionsUsingReference(Long referenceId) {

        List<ContributionQuestion> list = new ArrayList<ContributionQuestion>();


        List<ContributionQuestion> all = ContributionQuestion.find("deleted != true").fetch();

        for (ContributionQuestion q : all) {

            Map<String, JsonNode> confMap = getQuestionConfiguration(q.id);

            for (JsonNode conf : confMap.values()) {
                if ("reference".equals(conf.get("type").asText())) {

                    if (conf.hasNonNull("options") && conf.get("options").hasNonNull("reference")) {

                        if (referenceId.equals(conf.get("options").get("reference").asLong())) {
                            list.add(q);
                        }
                    }


                }
            }


        }

        return list;
    }

}
