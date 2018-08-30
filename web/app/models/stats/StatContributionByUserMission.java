package models.stats;

import models.User;
import models.questions.ContributionQuestion;
import play.db.jpa.GenericModel;

import javax.persistence.*;



@Entity
@Table(name = "VH_MISSION_C_USER_STAT")
public class StatContributionByUserMission extends GenericModel {

    @Id
    @Column(name = "MISSION_ID")
    private Long missionId;

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @ManyToOne(optional = true)
    @JoinColumn(name = "QUESTION_ID")
    private ContributionQuestion question;

    @Column(name = "ANSWER_COUNT")
    private Long answerCount;

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Long answerCount) {
        this.answerCount = answerCount;
    }

    public ContributionQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ContributionQuestion question) {
        this.question = question;
    }
}
