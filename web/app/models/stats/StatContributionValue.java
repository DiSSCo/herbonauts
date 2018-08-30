package models.stats;


import models.questions.ContributionQuestion;
import play.db.jpa.GenericModel;

import javax.persistence.*;

@Entity
@Table(name = "VH_MISSION_VALUE_STAT")
public class StatContributionValue extends GenericModel {

    @Id
    @Column(name = "MISSION_ID")
    private Long missionId;

    @Id
    @Column(name = "TYPE")
    private String type;

    @Id
    @Column(name = "TEXT_VALUE")
    private String textValue;

    @Column(name = "ANSWER_COUNT")
    private Long answerCount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public Long getMissionId() {

        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Long getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Long answerCount) {
        this.answerCount = answerCount;
    }
}
