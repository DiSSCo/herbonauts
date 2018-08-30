package models.questions;

import models.Mission;
import models.Specimen;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import services.JPAUtils;
import services.Page;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="H_CONTRIBUTION_QUESTION_STAT")
public class ContributionQuestionStat extends Model {

    @ManyToOne
    @JoinColumn(name = "QUESTION_ID")
    private ContributionQuestion question;

    @Column(name = "MISSION_ID")
    private Long missionId;

    @Column(name = "SPECIMEN_ID")
    private Long specimenId;

    @Column(name = "ANSWER_COUNT")
    private Long answerCount;

    @Column(name = "VALIDATED")
    private Boolean validated = false;

    @Column(name = "CONFLICTS")
    private Boolean conflicts = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VALID_ANSWER_ID")
    private ContributionAnswer validAnswer;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED_AT")
    private Date lastModifiedAt;

    public static Page<ContributionQuestionStat> getLastContributionStats(Long missionId, Integer page, Integer pageSize) {
        return JPAUtils.getPage(ContributionQuestionStat.class, page, pageSize);
    }

    public ContributionQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ContributionQuestion question) {
        this.question = question;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }

    public Long getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Long answerCount) {
        this.answerCount = answerCount;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public Boolean getConflicts() {
        return conflicts;
    }

    public void setConflicts(Boolean conflicts) {
        this.conflicts = conflicts;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public ContributionAnswer getValidAnswer() {
        return validAnswer;
    }

    public void setValidAnswer(ContributionAnswer validAnswer) {
        this.validAnswer = validAnswer;
    }

    public static void deleteStatsWithoutQuestion(Long missionId) {
        String query =
                "DELETE from H_CONTRIBUTION_QUESTION_STAT where id in (select s.id from H_CONTRIBUTION_QUESTION_STAT s  " +
                        "LEFT OUTER JOIN H_CONTRIBUTION_QUESTION c on s.question_id = c.id " +
                        "where s.MISSION_ID = :missionId and c.id is null)";
        JPA.em().createNativeQuery(query).setParameter("missionId", missionId).executeUpdate();
    }
}
